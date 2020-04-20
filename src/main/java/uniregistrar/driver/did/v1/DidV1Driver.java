package uniregistrar.driver.did.v1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.danubetech.keyformats.PrivateKey_to_JWK;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.nimbusds.jose.jwk.JWK;

import io.leonard.Base58;
import uniregistrar.RegistrationException;
import uniregistrar.driver.AbstractDriver;
import uniregistrar.driver.Driver;
import uniregistrar.request.DeactivateRequest;
import uniregistrar.request.RegisterRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.DeactivateState;
import uniregistrar.state.RegisterState;
import uniregistrar.state.SetRegisterStateFinished;
import uniregistrar.state.UpdateState;

public class DidV1Driver extends AbstractDriver implements Driver {

    private static final Logger log = LoggerFactory.getLogger(DidV1Driver.class);

    private Map<String, Object> properties;

    private String trustAnchorSeed;

    public DidV1Driver(Map<String, Object> properties) {

        this.setProperties(properties);
    }

    public DidV1Driver() {

        this(getPropertiesFromEnvironment());
    }

    private static Map<String, Object> getPropertiesFromEnvironment() {

        if (log.isDebugEnabled()) log.debug("Loading from environment: " + System.getenv());

        Map<String, Object> properties = new HashMap<>();

        try {

            String env_trustAnchorSeed = System.getenv("uniregistrar_driver_did_v1_trustAnchorSeed");

            if (env_trustAnchorSeed != null) properties.put("trustAnchorSeed", env_trustAnchorSeed);
        } catch (Exception ex) {

            throw new IllegalArgumentException(ex.getMessage(), ex);
        }

        return properties;
    }

    private static JWK privateKeyToJWK(ObjectNode jsonKey) {

        byte[] publicKeyBytes = Base58.decode(jsonKey.get("publicKeyBase58").asText());
        byte[] privateKeyBytes = Base58.decode(jsonKey.get("privateKeyBase58").asText());
        String kid = null;
        String use = null;

        return PrivateKey_to_JWK.Ed25519PrivateKeyBytes_to_JWK(privateKeyBytes, publicKeyBytes, kid, use);
    }

    private static String identifierToPublicKeyDIDURL(ObjectNode jsonKey) {

        return jsonKey.get("id").asText();
    }

    private void configureFromProperties() {

        if (log.isDebugEnabled()) log.debug("Configuring from properties: " + this.getProperties());

        try {

            String prop_trustAnchorSeed = (String) this.getProperties().get("trustAnchorSeed");

            if (prop_trustAnchorSeed != null) this.setTrustAnchorSeed(prop_trustAnchorSeed);
        } catch (Exception ex) {

            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    @Override
    public RegisterState register(RegisterRequest registerRequest) throws RegistrationException {

        // read options

        String hostname = registerRequest.getOptions() == null ? null : (String) registerRequest.getOptions().get("hostname");
        if (hostname == null || hostname.trim().isEmpty()) hostname = null;

        String keytype = registerRequest.getOptions() == null ? null : (String) registerRequest.getOptions().get("keytype");
        if (keytype == null || keytype.trim().isEmpty()) keytype = null;

        String ledger = registerRequest.getOptions() == null ? null : (String) registerRequest.getOptions().get("ledger");
        if (ledger == null || ledger.trim().isEmpty()) ledger = null;

        // register

        int exitCode;
        BufferedReader stdOutReader;
        BufferedReader stdErrReader;

        try {

            StringBuilder command = new StringBuilder("/opt/did-cli/did generate");
            command.append(" -t " + "veres");
            if (hostname != null) command.append(" -H ").append(hostname);
            if (keytype != null) command.append(" -k ").append(keytype);
            if (ledger != null) command.append(" -m ").append(ledger);
            command.append(" -r");

            if (log.isDebugEnabled()) log.debug("Executing command: " + command);

            Process process = Runtime.getRuntime().exec(command.toString());
            exitCode = process.waitFor();
            stdOutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            stdErrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            if (log.isDebugEnabled()) log.debug("Executed command: " + command + " (" + exitCode + ")");
        } catch (IOException | InterruptedException ex) {

            throw new RegistrationException("Cannot generate DID: " + ex.getMessage(), ex);
        }

        String newDid = null;
        String didDocumentLocation = null;
        String keysDocumentLocation = null;

        try {

            String line;

            while ((line = stdOutReader.readLine()) != null) {

                if (log.isDebugEnabled()) log.debug("OUT: " + line);

                if (line.startsWith("[Veres One][test mode] Generated a new Veres One DID: ")) {

                    newDid = line.substring("[Veres One][test mode] Generated a new Veres One DID: ".length());
                    didDocumentLocation = "/root/.dids/veres-test/registered/" + newDid.replace(":", "%3A") + ".json";
                    keysDocumentLocation = "/root/.dids/keys/" + newDid.replace(":", "%3A") + ".keys.json";
                }
            }

            while ((line = stdErrReader.readLine()) != null) {

                if (log.isWarnEnabled()) log.warn("ERR: " + line);
            }
        } catch (IOException ex) {
            throw new RegistrationException("Process read error: " + ex.getMessage(), ex);
        } finally {
            try {
                stdOutReader.close();
                stdErrReader.close();
            } catch (IOException ex) {
                throw new RegistrationException("Stream problem: " + ex.getMessage(), ex);
            }
        }

        if (log.isDebugEnabled()) log.debug("Process exit code: " + exitCode);
        if (exitCode != 0) throw new RegistrationException("Process exit code: " + exitCode);

        if (newDid == null) throw new RegistrationException("No DID registered.");

        if (log.isDebugEnabled()) log.debug("DID: " + newDid);
        if (log.isDebugEnabled()) log.debug("DID document location: " + didDocumentLocation);
        if (log.isDebugEnabled()) log.debug("Keys location: " + keysDocumentLocation);

        // read DID document

        FileReader didDocumentReader = null;
        JsonNode didDocumentJsonAuthentication;
        JsonNode didDocumentJsonAssertionMethod = null;
        JsonNode didDocumentJsonCapabilityInvocation = null;
        JsonNode didDocumentJsonCapabilityDelegation = null;

        try {

            didDocumentReader = new FileReader(new File(didDocumentLocation));

            JsonNode didDocumentJsonRoot = new ObjectMapper().readTree(didDocumentReader);
            if (log.isDebugEnabled()) log.debug("DID DOCUMENT JSON OBJECT: " + didDocumentJsonRoot);

            didDocumentJsonAuthentication = didDocumentJsonRoot.get("authentication");
            didDocumentJsonAssertionMethod = didDocumentJsonRoot.get("assertionMethod");
            didDocumentJsonCapabilityInvocation = didDocumentJsonRoot.get("capabilityInvocation");
            didDocumentJsonCapabilityDelegation = didDocumentJsonRoot.get("capabilityDelegation");
        } catch (IOException ex) {

            throw new RegistrationException("Cannot read DID document: " + ex.getMessage(), ex);
        } finally {

            try {

                if (didDocumentReader != null) didDocumentReader.close();
            } catch (IOException ex) {

                throw new RegistrationException("Cannot close DID document: " + ex.getMessage(), ex);
            }
        }

        // read keys document

        FileReader keysDocumentReader = null;
        Map<String, ObjectNode> keysDocumentJsonKeys = new HashMap<>();

        try {

            keysDocumentReader = new FileReader(new File(keysDocumentLocation));

            JsonNode keysDocumentJsonRoot = new ObjectMapper().readTree(keysDocumentReader);
            if (log.isDebugEnabled()) log.debug("KEYS DOCUMENT JSON OBJECT: " + (keysDocumentJsonRoot != null));

            assert keysDocumentJsonRoot != null;
            Iterator<Map.Entry<String, JsonNode>> keysDocumentJsonFields = keysDocumentJsonRoot.fields();

            while (keysDocumentJsonFields.hasNext()) {

                ObjectNode keysDocumentJsonKey = (ObjectNode) keysDocumentJsonFields.next().getValue();
                TextNode keysDocumentJsonKeyId = (TextNode) keysDocumentJsonKey.get("id");

                if (log.isDebugEnabled()) log.debug("Found key: " + keysDocumentJsonKeyId.asText());

                JWK jsonWebKey = privateKeyToJWK(keysDocumentJsonKey);
                String publicKeyDIDURL = identifierToPublicKeyDIDURL(keysDocumentJsonKey);

                keysDocumentJsonKey.putPOJO("privateKeyJwk", jsonWebKey.toJSONObject());
                keysDocumentJsonKey.put("publicKeyDIDURL", publicKeyDIDURL);

                keysDocumentJsonKeys.put(keysDocumentJsonKeyId.asText(), keysDocumentJsonKey);
            }
        } catch (IOException ex) {

            throw new RegistrationException("Cannot read keys document: " + ex.getMessage(), ex);
        } finally {

            try {

                if (keysDocumentReader != null) keysDocumentReader.close();
            } catch (IOException ex) {

                throw new RegistrationException("Cannot close keys document: " + ex.getMessage(), ex);
            }
        }

        // REGISTRATION STATE FINISHED: IDENTIFIER

        String identifier = newDid;

        // REGISTRATION STATE FINISHED: SECRET

        List<JsonNode> jsonKeys = new ArrayList<>();
        TextNode didDocumentJsonAuthenticationId = (TextNode) didDocumentJsonAuthentication.get(0).get("id");
        if (log.isDebugEnabled()) log.debug("Found authentication: " + didDocumentJsonAuthenticationId.asText());

        ObjectNode keysDocumentJsonAuthenticationKey = keysDocumentJsonKeys.get(didDocumentJsonAuthenticationId.asText());

        if (keysDocumentJsonAuthenticationKey != null) {

            if (log.isDebugEnabled())
                log.debug("Found authentication key: " + didDocumentJsonAuthenticationId.asText());

            jsonKeys.add(keysDocumentJsonAuthenticationKey);
        } else {

            throw new RegistrationException("Found no authentication key: " + didDocumentJsonAuthenticationId.asText());
        }

        Map<String, Object> secret = new LinkedHashMap<>();
        secret.put("keys", jsonKeys);

        // REGISTRATION STATE FINISHED: METHOD METADATA

        Map<String, Object> methodMetadata = new LinkedHashMap<>();
        methodMetadata.put("didDocumentLocation", didDocumentLocation);

        // done

        RegisterState registerState = RegisterState.build();
        SetRegisterStateFinished.setStateFinished(registerState, identifier, secret);
        registerState.setMethodMetadata(methodMetadata);

        return registerState;
    }

    @Override
    public UpdateState update(UpdateRequest updateRequest) throws RegistrationException {

        throw new RuntimeException("Not implemented.");
    }

    /*
     * Helper methods
     */

    @Override
    public DeactivateState deactivate(DeactivateRequest deactivateRequest) throws RegistrationException {

        throw new RegistrationException("This method does not support deactivation.");
    }

    @Override
    public Map<String, Object> properties() {

        return this.getProperties();
    }

    /*
     * Getters and setters
     */

    public Map<String, Object> getProperties() {

        return this.properties;
    }

    public void setProperties(Map<String, Object> properties) {

        this.properties = properties;
        this.configureFromProperties();
    }

    public String getTrustAnchorSeed() {

        return this.trustAnchorSeed;
    }

    public void setTrustAnchorSeed(String trustAnchorSeed) {

        this.trustAnchorSeed = trustAnchorSeed;
    }
}
