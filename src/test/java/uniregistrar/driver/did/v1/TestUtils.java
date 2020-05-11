package uniregistrar.driver.did.v1;

import uniregistrar.request.UpdateRequest;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TestUtils {

    private final static Map<String, Object> props = new HashMap<>();

    public static Map<String, Object> getTestProperties() {
        props.put("trustAnchorSeed", "none");
        props.put("basePath", "/home/cn/.dids");
        props.put("overrideOnUpdate", "false");
        props.put("updateExtension", "ver");
        return props;
    }

    public static UpdateRequest getSampleUpdateRequest() {
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

        //Create an update request
        UpdateRequest request = new UpdateRequest();
        request.setSecret(mySecret);

        //Set identifier for the request
        request.setIdentifier("did:v1:test:nym:z6MkffYv3Wk2cZBSyy1BmeqiQM9FjPDwL3Fzv9P73CgYKN35");


        return request;

    }
}
