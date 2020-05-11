package uniregistrar.driver.did.v1.update;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import did.Authentication;
import did.DIDDocument;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.*;
import uniregistrar.RegistrationException;
import uniregistrar.driver.did.v1.DidV1Driver;
import uniregistrar.driver.did.v1.TestUtils;
import uniregistrar.driver.did.v1.dto.parts.PatchItem;
import uniregistrar.driver.did.v1.dto.parts.PatchValue;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.UpdateState;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@DisplayName("Testing V1 DID update with add operations...")
@Tag("UpdateIntegration")
public class AddOperationTests {

    private static final Logger log = LogManager.getLogger(AddOperationTests.class);

    private static DidV1Driver driver;

    private static ObjectMapper mapper;

    @BeforeAll
    static void init() {
        log.info("Init test class");
        driver = new DidV1Driver(TestUtils.getTestProperties());
        mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Test
    @DisplayName("Add new authentication")
    void addAuthenticationTest() throws IOException {
        log.info("Getting a sample update request initialized with secret/jsw");
        UpdateRequest request = TestUtils.getSampleUpdateRequest();

        //Define patch item
        log.info("Creating a patch with op:add and path:/authentication");
        PatchItem patchItem1 = new PatchItem();
        patchItem1.setOp("add");
        patchItem1.setPath("/authentication");

        //Define patch item value
        log.info("Filling patch values id, type, controller, publicKeyBase58");
        PatchValue patchValue1 = new PatchValue();
        patchValue1.setId("did:v1:test:nym:z6MkffYv3Wk2cZBSyy1BmeqiQM9FjPDwL3Fzv9P73CgYKN35#authn-key-2");
        patchValue1.setType("Ed25519SignatureAuthentication2018");
        patchValue1.setController("did:v1:test:nym:z6MkffYv3Wk2cZBSyy1BmeqiQM9FjPDwL3Fzv9P73CgYKN35");
        patchValue1.setPublicKeyBase58("BpJk2oxtkm99ubKqhtLF7fB2fCM1bETgpJLFg8QEuQK2");

        //Set the patch value in patch
        patchItem1.setPatchValue(patchValue1);

        //Give patch information in a map
        log.info("Wrapping created patch into Map<String,Object");
        Map<String, Object> myOptions = new LinkedHashMap<>(); // Hash-map is fine as well
        myOptions.put("patch", Collections.singletonList(patchItem1));

        // Taking the patch in options
        request.setOptions(myOptions);


        // Create an UpdateState object for the response
        UpdateState state = null;

        // Try updating the existent
        log.debug("Executing the update request with:\n" + mapper.writeValueAsString(request));
        try {
            state = driver.update(request);
        } catch (RegistrationException e) {
            e.printStackTrace();
        }

        // Assert we don't receive a null state
        Assertions.assertNotNull(state);
        // Check the state
        try {
            log.debug("Received UpdateState is:\n" + state.toJson());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Assertions.assertTrue(state.getDidState().containsValue("finished"), "State is not finished");
        Assertions.assertTrue(state.getMethodMetadata().containsKey("updated"), "Not updated");

        String didDocPath = (String) state.getMethodMetadata().get("didDocumentLocation");
        Assertions.assertNotNull(didDocPath, "DIDDoc location is null");

        log.info("Trying to verify the update process by reading the updated DIDDOC...");
        DIDDocument ddoc = mapper.readValue(new File(didDocPath), DIDDocument.class);
        log.info("DIDDoc is opened...");

        boolean includesNewAuth = false;

        for (Authentication auth : ddoc.getAuthentications()) {
            if (auth.getJsonLdObject().get("publicKeyBase58").equals(patchValue1.getPublicKeyBase58())) {
                includesNewAuth = true;
            }
        }

        Assertions.assertTrue(includesNewAuth, "Returned state and the actual DIDDoc does not match. DIDDoc does not contain the new key...");
        log.info("The update process is successfully verified...");
    }

}
