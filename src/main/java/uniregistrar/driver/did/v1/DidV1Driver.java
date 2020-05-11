package uniregistrar.driver.did.v1;

import com.danubetech.keyformats.PrivateKey_to_JWK;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.nimbusds.jose.jwk.JWK;
import did.Authentication;
import did.DIDDocument;
import did.Service;
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
import uniregistrar.driver.did.v1.dto.V1MetaData;
import uniregistrar.driver.did.v1.dto.parts.*;
import uniregistrar.driver.did.v1.util.ErrorMessages;
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
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

public class DidV1Driver extends AbstractDriver implements Driver {

    private static final Logger log = LoggerFactory.getLogger(DidV1Driver.class);
    private static DateFormat df;
    private Map<String, Object> properties;
    private String trustAnchorSeed;
    private boolean overrideOnUpdate;
    private String basePath;

    public DidV1Driver() {
        this(getPropertiesFromEnvironment());
    }

    public DidV1Driver(Map<String, Object> properties) {
        this.setProperties(properties);
    }

    private static Map<String, Object> getPropertiesFromEnvironment() {

        df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        if (log.isDebugEnabled()) log.debug("Loading from environment: " + System.getenv());

        Map<String, Object> properties = new HashMap<>();

        try {

            String env_trustAnchorSeed = System.getenv("uniregistrar_driver_did_v1_trustAnchorSeed");
            String env_base_path = System.getenv("uniregistrar_driver_did_v1_base_path");
            String env_override_on_update = System.getenv("uniregistrar_driver_did_v1_override_on_update");
            String env_update_prefix = System.getenv("uniregistrar_driver_did_v1_update_prefix");

            if (env_trustAnchorSeed != null) properties.put("trustAnchorSeed", env_trustAnchorSeed);
            if (env_base_path != null) properties.put("basePath", env_base_path);
            if (env_override_on_update != null) properties.put("overrideOnUpdate", env_override_on_update);
            if (env_update_prefix != null) properties.put("updatePrefix", env_update_prefix);

        } catch (Exception ex) {

            throw new IllegalArgumentException(ex.getMessage(), ex);
        }

        return properties;
    }

    private void configureFromProperties() {

        df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        if (log.isDebugEnabled()) log.debug("Configuring from properties: " + this.getProperties());

        try {

            String prop_trustAnchorSeed = (String) this.getProperties().get("trustAnchorSeed");
            String prop_basePath = (String) this.getProperties().get("basePath");
            String prop_overrideOnUpdate = (String) this.getProperties().get("overrideOnUpdate");

            if (prop_trustAnchorSeed != null) this.setTrustAnchorSeed(prop_trustAnchorSeed);
            if (prop_basePath != null) this.setBasePath(prop_basePath);
            if (prop_overrideOnUpdate != null) {
                this.setOverrideOnUpdate(prop_overrideOnUpdate.equalsIgnoreCase("true"));
            }

        } catch (Exception ex) {

            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    public Map<String, Object> getProperties() {

        return this.properties;
    }

    public void setProperties(Map<String, Object> properties) {

        this.properties = properties;
        this.configureFromProperties();
    }

    public boolean isOverrideOnUpdate() {
        return overrideOnUpdate;
    }

    public void setOverrideOnUpdate(boolean overrideOnUpdate) {
        this.overrideOnUpdate = overrideOnUpdate;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
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

    private static JWK privateKeyToJWK(ObjectNode jsonKey) {

        byte[] publicKeyBytes = Base58.decode(jsonKey.get("publicKeyBase58").asText());
        byte[] privateKeyBytes = Base58.decode(jsonKey.get("privateKeyBase58").asText());


        return PrivateKey_to_JWK.Ed25519PrivateKeyBytes_to_JWK(privateKeyBytes, publicKeyBytes, null, null);
    }

    private static String identifierToPublicKeyDIDURL(ObjectNode jsonKey) {

        return jsonKey.get("id").asText();
    }

    @Override
    public UpdateState update(UpdateRequest updateRequest) throws RegistrationException {

        final ObjectMapper mapper = new ObjectMapper();

        // Try to parse provided proof with the request
        final ProofItem proof = validateUpdateRequest(updateRequest, mapper);

        // Get the didId form the request
        final String didId = updateRequest.getIdentifier();


        final String didFilePath = basePath + "/veres-test/registered/" + didId.replaceAll(":", "%3A") + ".json";
        final String didFileMetaPath = basePath + "/meta/" + didId.replaceAll(":", "%3A") + ".meta.json";
        final String didFileKeysPath = basePath + "/keys/" + didId.replaceAll(":", "%3A") + ".keys.json";

        DIDDocument toUpdate;

        File didFile = new File(didFilePath);

        try {
            toUpdate = mapper.readValue(didFile, DIDDocument.class);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RegistrationException(ErrorMessages.DIDDOC_NOT_FOUNT.getMsg());
        }


        boolean verified;
        try {
            verified = checkSignatures(proof, toUpdate);
        } catch (GeneralSecurityException e) {
            log.error(e.getMessage());
            throw new RegistrationException(ErrorMessages.SIGNATURE_ERROR.getMsg());
        }

        if (!verified) {
            throw new RegistrationException(ErrorMessages.SIGNATURE_MISMATCH.getMsg());
        }

        byte[] jsonBytes;
        try {
            jsonBytes = Files.readAllBytes(didFile.toPath());
        } catch (IOException e) {
            throw new RegistrationException(ErrorMessages.DIDDOC_NOT_FOUNT.getMsg());
        }

        // Just validate that DIDDoc is a valid json file
        try {
            mapper.readTree(jsonBytes);
        } catch (IOException e) {
            throw new RegistrationException(ErrorMessages.DIDDOC_PARSING_ERROR.getMsg());
        }

        List<PatchItem> patchItems = mapper.convertValue(updateRequest.getOptions()
                                                                 .get("patch"), new TypeReference<List<PatchItem>>() {
        });

        for (PatchItem item : patchItems) {
            switch (item.getOp()) {
                case "add":
                    if (item.getPath().contains("authentication")) {
                        PatchValue pVal = item.getPatchValue();
                        Authentication auth = Authentication.build(pVal.getJsonLd());
                        List<Authentication> auths = toUpdate.getAuthentications();
                        auths.add(auth);
                        toUpdate.setJsonLdObjectKeyValue("authentication", auths);

                    } else if (item.getPath().contains("service")) {
                        PatchValue pVal = item.getPatchValue();
                        Service service = Service.build(pVal.getJsonLd());
                        List<Service> services = toUpdate.getServices();
                        services.add(service);
                        toUpdate.setJsonLdObjectKeyValue("service", services);
                    }
                    break;
                case "remove":
                    if (item.getPath().contains("service")) {
                        int index = Integer.parseInt(item.getPath().replaceAll("[\\D]", "")) - 1;
                        if (toUpdate.getServices().size() > index) {
                            toUpdate.getServices().remove(index);
                        } else {
                            throw new RegistrationException(ErrorMessages.GENERIC_BAD_REQUEST.getMsg());
                        }
                    } else if (item.getPath().contains("authentication")) {
                        int index = Integer.parseInt(item.getPath().replaceAll("[\\D]", "")) - 1;
                        if (toUpdate.getAuthentications().size() > index) {
                            toUpdate.getAuthentications().remove(index);
                        } else {
                            throw new RegistrationException(ErrorMessages.GENERIC_BAD_REQUEST.getMsg());
                        }
                    }
                    break;
                default:
            }
        }

        String didDocumentLocation;

        if (overrideOnUpdate) {
            didDocumentLocation = didFilePath;
        } else {
            didDocumentLocation = didFilePath;
            // This is a dangerous op, a spam protection is needed for requests
            for (int i = 1; i < Integer.MAX_VALUE; i++) {
                File tmp = new File(didDocumentLocation.replace(".json", "_ver_" + i + ".json"));
                if (!tmp.exists()) {
                    didDocumentLocation = tmp.getAbsolutePath();
                    break;
                }
            }
        }

        try {
            mapper.writeValue(new File(didDocumentLocation), toUpdate);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RegistrationException(ErrorMessages.CANNOT_WRITE.getMsg());
        }

        // Create the updateState
        UpdateState upState = UpdateState.build();

        Map<String, Object> methodMetadata = new LinkedHashMap<>();


        Timestamp timestamp = Timestamp.from(Instant.now());

        // Try to get creation metadata

        final File didMetaFile = new File(didFileMetaPath);
        V1MetaData metaData = null;

        try {
            metaData = mapper.readValue(didMetaFile, V1MetaData.class);
        } catch (IOException e) {
            log.error(e.getMessage());
            log.debug("Cannot locate the meta file");
        }


        if (metaData != null) {
            Map<String, Object> cMetaInfo = mapper.convertValue(metaData, new TypeReference<LinkedHashMap<String, Object>>() {
            });
            methodMetadata.putAll(cMetaInfo);
            methodMetadata.put("didDocumentLocation", didDocumentLocation);
            methodMetadata.put("updated", df.format(timestamp));
        } else {
            methodMetadata.put("didDocumentLocation", didDocumentLocation);
            methodMetadata.put("updated", df.format(timestamp));
            methodMetadata.put("ledger", "veres");
            methodMetadata.put("ledgerMode", "test");
        }

        SetRegisterStateFinished.setStateFinished(upState, toUpdate.getId(), null);
        upState.setMethodMetadata(methodMetadata);


        return upState;
    }

    private static ProofItem validateUpdateRequest(UpdateRequest request, ObjectMapper mapper) throws RegistrationException {

        // Not checking with our UpdateRequest class, since it is an update request
//        if (!request.getType().startsWith("Update")) {
//            throw new RegistrationException("Wrong request type!");
//        }

        //TODO: Only checking the first proof
        final ProofItem proof = mapper.convertValue(request.getSecret(), ProofItem.class);

        if (proof == null) {
            throw new RegistrationException(ErrorMessages.NO_PROOF_GIVEN.getMsg());
        }

        final List<String> supportedProofTypes = V1.SUPPORTED_PROOF_TYPES();

        if (proof.getType() != null && supportedProofTypes.contains(proof.getType())) {
            log.debug("Supported proof method is provided. Key type is: " + proof.getType());

        } else {
            throw new RegistrationException(ErrorMessages.PROOF_METHOD_NOT_SUPPORTED.getMsg());
        }

        if (proof.getProofPurpose() == null) {
            throw new RegistrationException(ErrorMessages.PROOF_PURPOSE_NOT_ACCEPTABLE.getMsg());
        }

        if (!proof.getProofPurpose().equalsIgnoreCase("invokeCapability") &&
                !proof.getProofPurpose().equalsIgnoreCase("capabilityInvocation")) {
            throw new RegistrationException(ErrorMessages.PROOF_PURPOSE_NOT_ACCEPTABLE.getMsg());
        }

        return proof;

    }

    private static boolean checkSignatures(ProofItem proof, DIDDocument toUpdate) throws RegistrationException,
            GeneralSecurityException {

        final ObjectMapper mapper = new ObjectMapper();
        final V1DIDDoc docToUpdate = mapper.convertValue(toUpdate, V1DIDDoc.class);

        List<PublicKeyItem> publicKeyItems = new LinkedList<>();
        final List<CapabilityInvocationItem> capabilityItems = docToUpdate.getCapabilityInvocation();

        // Get all of the public keys that can invoke any change
        for (CapabilityInvocationItem item : capabilityItems) {
//            if (item.getType() != null && item.getType().equalsIgnoreCase("UpdateDid")) {
            if (item.getPublicKeyItem() != null) {
                publicKeyItems.add(item.getPublicKeyItem());
            } else {
                final PublicKeyItem tmp = new PublicKeyItem();
                tmp.setController(item.getController());
                tmp.setId(item.getId());
                tmp.setPublicKeyBase58(item.getPublicKeyBase58());
                tmp.setType(item.getType());

                publicKeyItems.add(tmp);
            }
        }

        if (publicKeyItems.size() < 1) {
            throw new RegistrationException(ErrorMessages.FORBIDDEN_UPDATE.getMsg());
        }

        LdSignature signature = new LdSignature();

        if (proof.getJws() != null && !proof.getJws().isEmpty()) {
            signature.setJws(proof.getJws());
        } else {
            throw new RegistrationException(ErrorMessages.NO_JWS_GIVEN.getMsg());
        }

        signature.setType(proof.getType());
        //TODO: Don't know if rest of the fields are required but it verifies with the above extracted fields

        boolean verified = false;

        for (PublicKeyItem publicKeyItem : publicKeyItems) {
            if (proof.getType().equals("Ed25519Signature2018")) {
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
    public DeactivateState deactivate(DeactivateRequest deactivateRequest) throws RegistrationException {
        throw new RegistrationException("This method does not support deactivation.");
    }

    @Override
    public Map<String, Object> properties() {

        return this.getProperties();
    }

    public String getTrustAnchorSeed() {

        return this.trustAnchorSeed;
    }

    public void setTrustAnchorSeed(String trustAnchorSeed) {

        this.trustAnchorSeed = trustAnchorSeed;
    }
}
