package uniregistrar.driver.did.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import uniregistrar.RegistrationException;
import uniregistrar.driver.did.v1.dto.parts.PatchItem;
import uniregistrar.driver.did.v1.dto.parts.PatchValue;
import uniregistrar.request.UpdateRequest;
import uniregistrar.state.UpdateState;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class UpdateTest2 {

    public static void main(String[] args) {
        // Init a map for the secret
        Map<String, Object> mySecret = new LinkedHashMap<>();

        // Set the proof in secret
        mySecret.put("type", "Ed25519Signature2018");
        mySecret.put("created", "2018-02-24T22:23:42Z");
        mySecret.put("creator", "did:v1:test:nym:z6MkffYv3Wk2cZBSyy1BmeqiQM9FjPDwL3Fzv9P73CgYKN35#z6MkwJZbTCwY6JeSP924JrDD6VNxMfVminyFstHaDUTQonvx");
        mySecret.put("capability", "did:v1:test:nym:z6MkffYv3Wk2cZBSyy1BmeqiQM9FjPDwL3Fzv9P73CgYKN35");
        mySecret.put("capabilityAction", "UpdateDid"); // I made up this action name, derived from registerDid they already use
        mySecret.put("jws", "eyJjcml0IjpbImI2NCJdLCJiNjQiOmZhbHNlLCJhbGciOiJFZERTQSJ9..LDqakb-NlgEZ9A1OnjhOglUi5z3lT2QVRY5r9B-mvcadk8jAI2SVnZ76V6uj-ULWxiUiljGAaPchQnRtAd-fCg");
        mySecret.put("proofPurpose", "invokeCapability");

        //Define patch item
        PatchItem patchItem1 = new PatchItem();
        patchItem1.setOp("add");
        patchItem1.setPath("/authentication");


        //Define patch item value
        PatchValue patchValue1 = new PatchValue();
        patchValue1.setId("did:v1:test:nym:z6MkffYv3Wk2cZBSyy1BmeqiQM9FjPDwL3Fzv9P73CgYKN35#authn-key-2");
        patchValue1.setType("Ed25519SignatureAuthentication2018");
        patchValue1.setController("did:v1:test:nym:z6MkffYv3Wk2cZBSyy1BmeqiQM9FjPDwL3Fzv9P73CgYKN35");
        patchValue1.setPublicKeyBase58("BpJk2oxtkm99ubKqhtLF7fB2fCM1bETgpJLFg8QEuQK2");

        //Set the patch value in patch
        patchItem1.setPatchValue(patchValue1);

        //Create an update request
        UpdateRequest request = new UpdateRequest();
        request.setSecret(mySecret);

        //Set identifier for the request
        request.setIdentifier("did:v1:test:nym:z6MkffYv3Wk2cZBSyy1BmeqiQM9FjPDwL3Fzv9P73CgYKN35");

        //Give patch information in a map
        Map<String, Object> myOptions = new LinkedHashMap<>(); // Hash-map is fine as well
        myOptions.put("patch", Collections.singletonList(patchItem1));

        // Taking the patch in options
        request.setOptions(myOptions);

        // Just not to upset driver constructor
        Map<String,Object> props = new HashMap<>();
        props.put("trustAnchorSeed", "none");

        // Init a V1 DID driver
        DidV1Driver driver = new DidV1Driver(props);

        // Create an UpdateState object for the response
        UpdateState state = null;

        // Try updating the existent
        try {
            state = driver.update(request);
        } catch (RegistrationException e) {
            e.printStackTrace();
        }

        // Assert we don't receive a null state
        assert state != null;
        // Check the state
        try {
            System.out.println(state.toJson());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }
}
