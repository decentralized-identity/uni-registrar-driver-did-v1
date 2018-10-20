package uniregistrar.driver.did.v1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uniregistrar.RegistrationException;
import uniregistrar.driver.Driver;
import uniregistrar.request.RegisterRequest;
import uniregistrar.request.RevokeRequest;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.RegisterState;
import uniregistrar.state.RegisterStateFinished;
import uniregistrar.state.RevokeState;
import uniregistrar.state.UpdateState;

public class DidV1Driver implements Driver {

	private static Logger log = LoggerFactory.getLogger(DidV1Driver.class);

	private Map<String, Object> properties;

	private String trustAnchorSeed;

	public DidV1Driver(Map<String, Object> properties) {

		this.setProperties(properties);
	}

	public DidV1Driver() {

		this.setProperties(getPropertiesFromEnvironment());
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

		// read parameters

		// register

		int exitCode;
		BufferedReader stdOutReader = null;
		BufferedReader stdErrReader = null;

		try {

			Process process = Runtime.getRuntime().exec("/opt/driver-did-v1/did-client/did generate -r");
			exitCode = process.waitFor();
			stdOutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			stdErrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		} catch (IOException | InterruptedException ex) {

			throw new RegistrationException("Cannot generate DID: " + ex.getMessage(), ex);
		}

		String newDid = null;
		String didDocumentLocation = null;

		try {

			String line;

			while ((line = stdOutReader.readLine()) != null) {

				if (log.isDebugEnabled()) log.debug("OUT: " + line);

				if (line.startsWith("[Veres One][test] DID: ")) newDid = line.substring("[Veres One][test] DID: ".length());
				if (line.startsWith("[Veres One][test] DID Document stored in: ")) didDocumentLocation = line.substring("[Veres One][test] DID Document stored in: ".length());
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

		BufferedReader didDocumentReader = null;
		List<String> privateKeys = new ArrayList<String> ();;

		try {

			didDocumentReader = new BufferedReader(new FileReader(new File(didDocumentLocation)));

			String line;

			while ((line = didDocumentReader.readLine()) != null) {

				if (line.contains("\"privateKeyBase58\": ")) privateKeys.add(line.substring(line.indexOf("\"privateKeyBase58\": ") + "\"privateKeyBase58\": ".length() + 1, line.length() - 2));
				if (line.contains("\"privateKeyPem\": ")) privateKeys.add(line.substring(line.indexOf("\"privateKeyPem\": ") + "\"privateKeyPem\": ".length() + 1, line.length() - 2));
			}
		} catch (IOException ex) {

			throw new RegistrationException("Process read error: " + ex.getMessage(), ex);
		} finally {

			try {

				if (didDocumentReader != null) didDocumentReader.close();
			} catch (IOException ex) {

				throw new RegistrationException("Stream problem: " + ex.getMessage(), ex);
			}
		}

		// create METHOD METADATA

		Map<String, Object> methodMetadata = new LinkedHashMap<String, Object> ();
		//		methodMetadata.put("network", network);

		// create IDENTIFIER

		String identifier = newDid;

		// create CREDENTIALS

		Map<String, Object> credentials = new LinkedHashMap<String, Object> ();
		credentials.put("privateKeys", privateKeys);

		// create REGISTER STATE

		RegisterState registerState = new RegisterStateFinished(null, null, methodMetadata, identifier, credentials);

		// done

		return registerState;
	}

	@Override
	public UpdateState update(UpdateRequest updateRequest) throws RegistrationException {

		throw new RuntimeException("Not implemented.");
	}

	@Override
	public RevokeState revoke(RevokeRequest revokeRequest) throws RegistrationException {

		throw new RuntimeException("Not implemented.");
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
