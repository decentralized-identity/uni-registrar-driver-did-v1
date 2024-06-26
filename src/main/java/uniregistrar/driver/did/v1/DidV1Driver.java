package uniregistrar.driver.did.v1;

import com.danubetech.keyformats.PrivateKey_to_JWK;
import com.danubetech.keyformats.jose.JWK;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import io.leonard.Base58;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uniregistrar.RegistrationException;
import uniregistrar.driver.AbstractDriver;
import uniregistrar.driver.Driver;
import uniregistrar.request.CreateRequest;
import uniregistrar.request.DeactivateRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.CreateState;
import uniregistrar.state.DeactivateState;
import uniregistrar.state.SetStateFinished;
import uniregistrar.state.UpdateState;

import java.io.*;
import java.util.*;

public class DidV1Driver extends AbstractDriver implements Driver {

	private static Logger log = LoggerFactory.getLogger(DidV1Driver.class);

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

		Map<String, Object> properties = new HashMap<String, Object> ();

		try {

			String env_trustAnchorSeed = System.getenv("uniregistrar_driver_did_v1_trustAnchorSeed");

			if (env_trustAnchorSeed != null) properties.put("trustAnchorSeed", env_trustAnchorSeed);
		} catch (Exception ex) {

			throw new IllegalArgumentException(ex.getMessage(), ex);
		}

		return properties;
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
	public CreateState create(CreateRequest createRequest) throws RegistrationException {

		// read options

		String hostname = createRequest.getOptions() == null ? null : (String) createRequest.getOptions().get("hostname");
		if (hostname == null || hostname.trim().isEmpty()) hostname = null;

		String ledger = createRequest.getOptions() == null ? null : (String) createRequest.getOptions().get("ledger");
		if (ledger == null || ledger.trim().isEmpty()) ledger = null;

		String keytype = createRequest.getOptions() == null ? null : (String) createRequest.getOptions().get("keytype");
		if (keytype == null || keytype.trim().isEmpty()) keytype = null;

		// create

		int exitCode;
		BufferedReader stdOutReader = null;
		BufferedReader stdErrReader = null;

		try {

			StringBuffer command = new StringBuffer("/opt/node_modules/did-cli/did generate");
			command.append(" -t " + "veres");
			if (hostname != null) command.append(" -H " + hostname);
			if (ledger != null) command.append(" -m " + ledger.toLowerCase());
			if (keytype != null) command.append(" -k " + keytype.toLowerCase());
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
					didDocumentLocation = "/root/.dids/veres-test/registered/"  + newDid.replace(":", "%3A") + ".json";
					keysDocumentLocation = "/root/.dids/keys/"  + newDid.replace(":", "%3A") + ".keys.json";
				}
			}

			while ((line = stdErrReader.readLine()) != null) {

				if (log.isWarnEnabled()) log.warn("ERR: " + line);
			}
		} catch (IOException ex) {

			throw new RegistrationException("Process read error: " + ex.getMessage(), ex);
		} finally {

			try {

				if (stdOutReader != null) stdOutReader.close();
				if (stdErrReader != null) stdErrReader.close();
			} catch (IOException ex) {

				throw new RegistrationException("Stream problem: " + ex.getMessage(), ex);
			}
		}

		if (log.isDebugEnabled()) log.debug("Process exit code: " + exitCode);
		if (exitCode != 0) throw new RegistrationException("Process exit code: " + exitCode);

		if (newDid == null) throw new RegistrationException("No DID created.");

		if (log.isDebugEnabled()) log.debug("DID: " + newDid);
		if (log.isDebugEnabled()) log.debug("DID document location: " + didDocumentLocation);
		if (log.isDebugEnabled()) log.debug("Keys location: " + keysDocumentLocation);

		// read DID document

		FileReader didDocumentReader = null;
		JsonNode didDocumentJsonAuthentication = null;
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
		Map<String, ObjectNode> keysDocumentJsonKeys = new HashMap<String, ObjectNode> ();

		try {

			keysDocumentReader = new FileReader(new File(keysDocumentLocation));

			JsonNode keysDocumentJsonRoot = new ObjectMapper().readTree(keysDocumentReader);
			if (log.isDebugEnabled()) log.debug("KEYS DOCUMENT JSON OBJECT: " + (keysDocumentJsonRoot != null));

			Iterator<Map.Entry<String, JsonNode>> keysDocumentJsonFields = keysDocumentJsonRoot.fields();

			while (keysDocumentJsonFields.hasNext()) {

				ObjectNode keysDocumentJsonKey = (ObjectNode) keysDocumentJsonFields.next().getValue();

				JWK jsonWebKey = privateKeyToJWK(keysDocumentJsonKey);
				String keyUrl = identifierToKeyUrl(keysDocumentJsonKey);

				if (log.isDebugEnabled()) log.debug("Found key: " + keyUrl);

				keysDocumentJsonKey.put("id", keyUrl);
				keysDocumentJsonKey.remove("publicKeyBase58");
				keysDocumentJsonKey.remove("privateKeyBase58");
				keysDocumentJsonKey.putPOJO("privateKeyJwk", jsonWebKey.toMap());

				keysDocumentJsonKeys.put(keyUrl, keysDocumentJsonKey);
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

		// REGISTRATION STATE FINISHED: DID

		String did = newDid;

		// REGISTRATION STATE FINISHED: SECRET

		List<JsonNode> verificationMethod = new ArrayList<JsonNode> ();
		findSecretKeys(keysDocumentJsonKeys, didDocumentJsonAuthentication, "authentication", verificationMethod);
		findSecretKeys(keysDocumentJsonKeys, didDocumentJsonAssertionMethod, "assertionMethod", verificationMethod);
		findSecretKeys(keysDocumentJsonKeys, didDocumentJsonCapabilityInvocation, "capabilityInvocation", verificationMethod);
		findSecretKeys(keysDocumentJsonKeys, didDocumentJsonCapabilityDelegation, "capabilityDelegation", verificationMethod);

		Map<String, Object> secret = new LinkedHashMap<> ();
		secret.put("verificationMethod", verificationMethod);

		// REGISTRATION STATE FINISHED: DID DOCUMENT METADATA

		Map<String, Object> setDidDocumentMetadata = new LinkedHashMap<> ();
		setDidDocumentMetadata.put("didDocumentLocation", didDocumentLocation);

		// done

		CreateState createState = CreateState.build();
		SetStateFinished.setStateFinished(createState, did, secret);
		createState.setDidDocumentMetadata(setDidDocumentMetadata);

		return createState;
	}

	@Override
	public UpdateState update(UpdateRequest updateRequest) {

		throw new RuntimeException("Not implemented.");
	}

	@Override
	public DeactivateState deactivate(DeactivateRequest deactivateRequest) {

		throw new RuntimeException("Not implemented.");
	}

	@Override
	public Map<String, Object> properties() {

		return this.getProperties();
	}

	/*
	 * Helper methods
	 */

	private static void findSecretKeys(Map<String, ObjectNode> keysDocumentJsonKeys, JsonNode didDocumentJsonProofPurpose, String proofPurpose, List<JsonNode> jsonKeys) throws RegistrationException {

		TextNode didDocumentJsonProofPurposeId = (TextNode) didDocumentJsonProofPurpose.get(0).get("id");
		if (log.isDebugEnabled()) log.debug("Found proof purpose " + proofPurpose + ": " + didDocumentJsonProofPurposeId.asText());

		ObjectNode keysDocumentJsonProofPurposeKey = keysDocumentJsonKeys.get(didDocumentJsonProofPurposeId.asText());

		if (keysDocumentJsonProofPurposeKey != null) {

			if (log.isDebugEnabled()) log.debug("Found proof purpose key " + proofPurpose + ": " + didDocumentJsonProofPurposeId.asText());

			keysDocumentJsonProofPurposeKey.putArray("purpose").add(proofPurpose);
			jsonKeys.add(keysDocumentJsonProofPurposeKey);
		} else {

			throw new RegistrationException("Found no proof purpose key " + proofPurpose + ": " + didDocumentJsonProofPurposeId.asText());
		}
	}

	private static JWK privateKeyToJWK(ObjectNode jsonKey) {

		byte[] privateKeyBytes = Base58.decode((jsonKey.get("privateKeyBase58")).asText());
		byte[] publicKeyBytes = Base58.decode((jsonKey.get("publicKeyBase58")).asText());
		String kid = null;
		String use = null;

		return PrivateKey_to_JWK.Ed25519PrivateKeyBytes_to_JWK(privateKeyBytes, kid, use);
	}

	private static String identifierToKeyUrl(ObjectNode jsonKey) {

		return ((TextNode) jsonKey.get("id")).asText();
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
