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

import com.danubetech.keyformats.PrivateKeyToJWK;
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
	public RegisterState register(RegisterRequest registerRequest) throws RegistrationException {

		// read options

		String keytype = registerRequest.getOptions() == null ? null : (String) registerRequest.getOptions().get("keytype");
		if (keytype == null || keytype.trim().isEmpty()) keytype = null;

		String ledger = registerRequest.getOptions() == null ? null : (String) registerRequest.getOptions().get("ledger");
		if (ledger == null || ledger.trim().isEmpty()) ledger = null;

		// register

		int exitCode;
		BufferedReader stdOutReader = null;
		BufferedReader stdErrReader = null;

		try {

			StringBuffer command = new StringBuffer("/opt/did-client/did generate");
			if (keytype != null) command.append(" -t " + keytype);
			if (ledger != null) command.append(" -m " + ledger);
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

		try {

			String line;

			while ((line = stdOutReader.readLine()) != null) {

				if (log.isDebugEnabled()) log.debug("OUT: " + line);

				if (line.startsWith("[Veres One][test] DID: ")) {

					newDid = line.substring("[Veres One][test] DID: ".length());
					didDocumentLocation = "/root/.dids/veres/test/"  + newDid.replace(":", "%3A") + ".json";
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

		if (newDid == null) throw new RegistrationException("No DID registered.");

		if (log.isDebugEnabled()) log.debug("DID: " + newDid);
		if (log.isDebugEnabled()) log.debug("DID Document location: " + didDocumentLocation);

		// read DID document

		FileReader didDocumentReader = null;
		JsonNode fileJsonKeys = null;
		JsonNode fileJsonAuthentication = null;
		JsonNode fileJsonCapabilityDelegation = null;
		JsonNode fileJsonCapabilityInvocation = null;

		try {

			didDocumentReader = new FileReader(new File(didDocumentLocation));

			JsonNode fileJsonNode = new ObjectMapper().readTree(didDocumentReader);
			if (log.isDebugEnabled()) log.debug("JSON OBJECT: " + fileJsonNode);

			fileJsonKeys = fileJsonNode.get("keys");
			fileJsonAuthentication = fileJsonNode.get("doc").get("authentication");
			fileJsonCapabilityDelegation = fileJsonNode.get("doc").get("capabilityDelegation");
			fileJsonCapabilityInvocation = fileJsonNode.get("doc").get("capabilityInvocation");
		} catch (IOException ex) {

			throw new RegistrationException("Process read error: " + ex.getMessage(), ex);
		} finally {

			try {

				if (didDocumentReader != null) didDocumentReader.close();
			} catch (IOException ex) {

				throw new RegistrationException("Stream problem: " + ex.getMessage(), ex);
			}
		}

		// REGISTRATION STATE FINISHED: IDENTIFIER

		String identifier = newDid;

		// REGISTRATION STATE FINISHED: SECRET

		List<JsonNode> jsonKeys = new ArrayList<JsonNode> ();
		TextNode fileJsonAuthenticationId = (TextNode) fileJsonAuthentication.get(0).get("id");
		if (log.isDebugEnabled()) log.debug("Found authentication: " + fileJsonAuthenticationId.asText());

		for (Iterator<Map.Entry<String, JsonNode>> i = fileJsonKeys.fields(); i.hasNext(); ) {

			ObjectNode fileJsonKey = (ObjectNode) i.next().getValue();
			TextNode fileJsonKeyId = (TextNode) fileJsonKey.get("id");

			if (fileJsonKeyId.asText().equals(fileJsonAuthenticationId.asText())) {

				if (log.isDebugEnabled()) log.debug("Found authentication key: " + fileJsonKeyId.asText());

				JWK jsonWebKey = privateKeyToJWK(fileJsonKey);
				String publicKeyDIDURL = identifierToPublicKeyDIDURL(fileJsonKey);

				fileJsonKey.putPOJO("privateKeyJwk", jsonWebKey.toJSONObject());
				fileJsonKey.put("publicKeyDIDURL", publicKeyDIDURL);
				jsonKeys.add(fileJsonKey);
			} else {

				if (log.isDebugEnabled()) log.debug("Found non-authentication key: " + fileJsonKeyId.asText() + " (skipping)");
			}
		}

		Map<String, Object> secret = new LinkedHashMap<String, Object> ();
		secret.put("keys", jsonKeys);

		// REGISTRATION STATE FINISHED: METHOD METADATA

		Map<String, Object> methodMetadata = new LinkedHashMap<String, Object> ();
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

	@Override
	public DeactivateState deactivate(DeactivateRequest deactivateRequest) throws RegistrationException {

		throw new RuntimeException("Not implemented.");
	}

	@Override
	public Map<String, Object> properties() {

		return this.getProperties();
	}

	/*
	 * Helper methods
	 */

	private static JWK privateKeyToJWK(ObjectNode jsonKey) {

		byte[] publicKeyBytes = Base58.decode(((TextNode) jsonKey.get("publicKeyBase58")).asText());
		byte[] privateKeyBytes = Base58.decode(((TextNode) jsonKey.get("privateKeyBase58")).asText());
		String kid = null;
		String use = null;

		return PrivateKeyToJWK.Ed25519PrivateKeyBytesToJWK(privateKeyBytes, publicKeyBytes, kid, use);
	}

	private static String identifierToPublicKeyDIDURL(ObjectNode jsonKey) {

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