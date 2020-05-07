package uniregistrar.driver.did.v1;

import com.danubetech.keyformats.PrivateKey_to_JWK;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.nimbusds.jose.jwk.JWK;
import did.DIDDocument;
import info.weboftrust.ldsignatures.LdSignature;
import info.weboftrust.ldsignatures.verifier.Ed25519Signature2018LdVerifier;
import info.weboftrust.ldsignatures.verifier.RsaSignature2018LdVerifier;
import io.leonard.Base58;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.RegistrationException;
import uniregistrar.driver.AbstractDriver;
import uniregistrar.driver.Driver;
import uniregistrar.driver.did.v1.dto.V1DIDDoc;
import uniregistrar.driver.did.v1.dto.V1UpdateRequest;
import uniregistrar.driver.did.v1.dto.parts.InvokeCapabilityItem;
import uniregistrar.driver.did.v1.dto.parts.ProofItem;
import uniregistrar.driver.did.v1.dto.parts.PublicKeyItem;
import uniregistrar.driver.did.v1.util.V1;
import uniregistrar.request.DeactivateRequest;
import uniregistrar.request.RegisterRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.DeactivateState;
import uniregistrar.state.RegisterState;
import uniregistrar.state.SetRegisterStateFinished;
import uniregistrar.state.UpdateState;

import java.io.*;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

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

        //TODO: Ask markus why he creates references to the null objects
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

        String hostname = registerRequest.getOptions() == null ? null : (String) registerRequest.getOptions()
                .get("hostname");
        if (hostname == null || hostname.trim().isEmpty()) hostname = null;

        String keytype = registerRequest.getOptions() == null ? null : (String) registerRequest.getOptions()
                .get("keytype");
        if (keytype == null || keytype.trim().isEmpty()) keytype = null;

        String ledger = registerRequest.getOptions() == null ? null : (String) registerRequest.getOptions()
                .get("ledger");
        if (ledger == null || ledger.trim().isEmpty()) ledger = null;

        // register

        int exitCode;
        BufferedReader stdOutReader;
        BufferedReader stdErrReader;

        try {

            StringBuilder command = new StringBuilder("external/did-cli/did generate");
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

                    //for local test
//                    newDid = line.substring("[Veres One][test mode] Generated a new Veres One DID: ".length());
//                    didDocumentLocation = "/home/cn/.dids/veres-test/registered/" + newDid.replace(":", "%3A") + ".json";
//                    keysDocumentLocation = "/home/cn/.dids/keys/" + newDid.replace(":", "%3A") + ".keys.json";
                }
            }

            while ((line = stdErrReader.readLine()) != null) {

                if (log.isWarnEnabled()) log.warn("ERR: " + line);
            }
        } catch (IOException ex) {
            throw new RegistrationException("Process read error: " + ex.getMessage(), ex);
        }
        try {
            stdOutReader.close();
            stdErrReader.close();
        } catch (IOException ex) {
            throw new RegistrationException("Stream problem: " + ex.getMessage(), ex);
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
        }
        try {
            didDocumentReader.close();
        } catch (IOException ex) {
            throw new RegistrationException("Cannot close DID document: " + ex.getMessage(), ex);

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
        }
        try {
            keysDocumentReader.close();
        } catch (IOException ex) {
            throw new RegistrationException("Cannot close keys document: " + ex.getMessage(), ex);
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

    private ProofItem validateUpdateRequest(V1UpdateRequest request) throws RegistrationException {

        // TODO: No idea if their "UpdateWebLedgerRecord" should be the only one to accept, so...
        if (!request.getType().startsWith("Update")) {
            throw new RegistrationException("Wrong request type!");
        }

        if (request.getProof() == null || request.getProof().size() < 1) {
            throw new RegistrationException("Request does not contain any proof key");
        }

        ProofItem toCheck = null;
        final List<String> supportedKeyTypes = V1.SUPPORTED_KEY_TYPES();

        //TODO: Controlling only one proof method. No idea if multiple ones required.
        for (ProofItem p : request.getProof()) {
            if (p.getType() != null && supportedKeyTypes.contains(p.getType())) {
                log.debug("Supported proof method is provided. Key type is: " + p.getType());
                toCheck = p;
                break;
            }
        }

        if (toCheck == null) {
            throw new RegistrationException("No supported proof type is provided!");
        }

        //TODO: I don't know if I should grant update right to the authentication proof type as well
        if (toCheck.getProofPurpose() != null) {
            if (!toCheck.getProofPurpose().equalsIgnoreCase("invokeCapability") ||
                    !toCheck.getProofPurpose().equalsIgnoreCase("capabilityInvocation")) {
                throw new RegistrationException("Wrong proof purpose is given.");
            }
        } else {
            throw new RegistrationException("No proof purpose given");
        }

        return toCheck;

    }

    private boolean checkSignatures(ProofItem proof, DIDDocument toUpdate) throws RegistrationException,
            GeneralSecurityException {

        final ObjectMapper mapper = new ObjectMapper();
        final V1DIDDoc docToUpdate = mapper.convertValue(toUpdate, V1DIDDoc.class);

        List<PublicKeyItem> publicKeyItems = new ArrayList<>();
        final List<InvokeCapabilityItem> capabilityItems = docToUpdate.getInvokeCapability();

        // Get all of the public keys that can invoke any change
        for (InvokeCapabilityItem item : capabilityItems) {
            if (item.getType() != null && item.getType().equalsIgnoreCase("UpdateDid")) {
                if (item.getPublicKeyItem() == null) {
                    publicKeyItems.add(item.getPublicKeyItem());
                }
            }
        }

        if (publicKeyItems.size() < 1) {
            throw new RegistrationException("DID Document is not update-able!");
        }

        LdSignature signature = new LdSignature();

        if (proof.getJws() != null && !proof.getJws().isEmpty()) {
            signature.setJws(proof.getJws());
        } else {
            throw new RegistrationException("JWS is null");
        }

        signature.setType(proof.getType());
        //TODO: Don't know if rest of the fields are required but it verifies with the above extracted fields


//        LdVerifier<? extends SignatureSuite> verifier = LdVerifier.ldVerifierForSignatureSuite()

        boolean verified = false;

        for (PublicKeyItem publicKeyItem : publicKeyItems) {
            if (proof.getType().equals("Ed25519VerificationKey2018")) {
                byte[] pubKeyBytes = Base58.decode(publicKeyItem.getPublicKeyBase58());
                Ed25519Signature2018LdVerifier verifier = new Ed25519Signature2018LdVerifier(pubKeyBytes);
                verified = verifier.verify((LinkedHashMap<String, Object>) toUpdate.getJsonLdObject(), signature);
            } else {
                // FIXME: Rsa Signature suite is not tested.
                RsaSignature2018LdVerifier verifier = new RsaSignature2018LdVerifier((RSAPublicKey) publicKeyItem);
                verified = verifier.verify((LinkedHashMap<String, Object>) toUpdate.getJsonLdObject(), signature);
            }
            if (verified) {
                break;
            }
        }

        return verified;
    }

    @Override
    public UpdateState update(UpdateRequest updateRequest) throws RegistrationException {

        ObjectMapper mapper = new ObjectMapper();

        // Convert request DID to our easy to work DTO
        //TODO: For PoC purpose, I use the DIDDoc in update request as "the" update request
//        final V1UpdateRequest updateDTO = mapper.convertValue(updateRequest.getDidDocument(), V1UpdateRequest.class);
//        final ProofItem proof = validateUpdateRequest(updateDTO);

        final ProofItem proof = mapper.convertValue(updateRequest.getSecret(), ProofItem.class);

        final String didId = updateRequest.getIdentifier();
//        final String didFilePath = "/home/cn/.dids/veres-test/registered/" + didId.replace(":", "%3A") + ".json";
        final String didFilePath = "/root/.dids/veres-test/registered/" + didId.replace(":", "%3A") + ".json";


        DIDDocument toUpdate;

        File didFile = new File(didFilePath);

        try {
            toUpdate = mapper.readValue(didFile, DIDDocument.class);
        } catch (IOException e) {
//            log.error("Cannot locate the did document!");
            throw new RegistrationException("Cannot locate the did document!");
        }


        boolean verified;
        try {
            verified = checkSignatures(proof, toUpdate);
        } catch (GeneralSecurityException e) {
            throw new RegistrationException("Signature error!");
        }

        if (!verified) {
            throw new RegistrationException("Cannot update with the given signature!");
        }

        // FIXME: Easier to navigate for now, using the file format
        byte[] jsonBytes;
        try {
            jsonBytes = Files.readAllBytes(didFile.toPath());
        } catch (IOException e) {
            throw new RegistrationException("Cannot locate the DID Doc!");
        }
        JsonNode rootNode = null;

        try {
            rootNode = mapper.readTree(jsonBytes);
        } catch (IOException e) {
            throw new RegistrationException("Cannot read the DID Doc!");
        }

        //TODO: Final step -> Navigate JsonNode's and add/remove according to the patch
//        for (PatchItem patch : updateDTO.getPatch()) {
//            switch (patch.getOp()) {
//                case "add":
//                    if (patch.getOp().contains("authentication")) {
//                        Map<String, Object> auth = mapper.convertValue(patch.getValue(), Map.class);
//                        toUpdate.getAuthentications().add(Authentication.build(auth));
//                    }
//                case "remove":
//                default:
//            }
//        }

        return null;
    }


    /*
     * Helper methods
     */

    @Override
    public DeactivateState deactivate(DeactivateRequest deactivateRequest) throws RegistrationException {
        //TODO: This method is not supported -> Prepare a suitable response
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
