package uniregistrar.driver.did.v1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import did.Authentication;
import did.DIDDocument;
import did.PublicKey;
import uniregistrar.RegistrationException;
import uniregistrar.driver.did.v1.dto.V1DIDDoc;
import uniregistrar.driver.did.v1.dto.parts.PatchItem;
import uniregistrar.driver.did.v1.dto.parts.PatchValue;
import uniregistrar.driver.did.v1.dto.parts.PublicKeyItem;
import uniregistrar.request.UpdateRequest;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class UpdateTest1 {
    public static void main(String[] args) throws RegistrationException, IOException {

        // Set secret
        Map<String, Object> mySecret = new LinkedHashMap<>();

        mySecret.put("type", "Ed25519Signature2018");
        mySecret.put("created", "2018-02-24T22:23:42Z");
        mySecret.put("creator", "did:v1:test:nym:z6MkffYv3Wk2cZBSyy1BmeqiQM9FjPDwL3Fzv9P73CgYKN35#z6MkwJZbTCwY6JeSP924JrDD6VNxMfVminyFstHaDUTQonvx");
        mySecret.put("capability", "did:v1:test:nym:z6MkffYv3Wk2cZBSyy1BmeqiQM9FjPDwL3Fzv9P73CgYKN35");
        mySecret.put("capabilityAction", "UpdateDid"); // I made up this action name, derived from registerDid they already use
        mySecret.put("jws", "eyJjcml0IjpbImI2NCJdLCJiNjQiOmZhbHNlLCJhbGciOiJFZERTQSJ9..LDqakb-NlgEZ9A1OnjhOglUi5z3lT2QVRY5r9B-mvcadk8jAI2SVnZ76V6uj-ULWxiUiljGAaPchQnRtAd-fCg");
        mySecret.put("proofPurpose", "invokeCapability");

        // Create update request
        UpdateRequest request = new UpdateRequest();
        request.setSecret(mySecret);

        //Set identifier

        request.setIdentifier("did:v1:test:nym:z6MkffYv3Wk2cZBSyy1BmeqiQM9FjPDwL3Fzv9P73CgYKN35");

//        Map<String, Object> authMap = new HashMap<>();
//        authMap.put("id", "did:v1:test:nym:z6MkffYv3Wk2cZBSyy1BmeqiQM9FjPDwL3Fzv9P73CgYKN35#authn-key-2");
//        authMap.put("type", "Ed25519SignatureAuthentication2018");
//        authMap.put("controller", "did:v1:test:nym:z6MkffYv3Wk2cZBSyy1BmeqiQM9FjPDwL3Fzv9P73CgYKN35");
//        authMap.put("publicKeyBase58", "BpJk2oxtkm99ubKqhtLF7fB2fCM1bETgpJLFg8QEuQK2");
//
//        Authentication auth1 = Authentication.build(authMap);
//        request.setAddAuthentications(Collections.singletonList(auth1));


        //Define patch items
        PatchItem patchItem1 = new PatchItem();
        PatchValue patchValue1 = new PatchValue();

        patchValue1.setId("did:v1:test:nym:z6MkffYv3Wk2cZBSyy1BmeqiQM9FjPDwL3Fzv9P73CgYKN35#authn-key-2");
        patchValue1.setType("Ed25519SignatureAuthentication2018");
        patchValue1.setController("did:v1:test:nym:z6MkffYv3Wk2cZBSyy1BmeqiQM9FjPDwL3Fzv9P73CgYKN35");
        patchValue1.setPublicKeyBase58("BpJk2oxtkm99ubKqhtLF7fB2fCM1bETgpJLFg8QEuQK2");

        patchItem1.setOp("add");
        patchItem1.setPath("/authentication");

        patchItem1.setPatchValue(patchValue1);

        PatchItem patchItem2 = new PatchItem();
        patchItem2.setOp("remove");
        patchItem2.setPath("/services/3");


        Map<String, Object> myOptions = new HashMap<>();
        myOptions.put("patch", Arrays.asList(patchItem1, patchItem2));

        // Taking the patch in options
        request.setOptions(myOptions);


        // Just not to upset driver constructor
        Map<String,Object> props = new HashMap<>();
        props.put("trustAnchorSeed", "none");

//        DidV1Driver driver = new DidV1Driver(props);
//        driver.update(request);

        request.setOptions(props);
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        V1DIDDoc toUpdate = mapper.readValue(new File("/home/cn/sovrin_driver/uni-registrar-driver-did-v1/src/test/resources/test_dids/did.json")
                , V1DIDDoc.class);

        DIDDocument toUpdate2 = mapper.readValue(new File("/home/cn/sovrin_driver/uni-registrar-driver-did-v1/src/test/resources/test_dids/did.json")
                , DIDDocument.class);

        PublicKeyItem publicKeyItem = mapper.convertValue(patchValue1, PublicKeyItem.class);
//        AuthenticationItem auth = mapper.convertValue(patchValue1, AuthenticationItem.class);
        PublicKey publicKey = PublicKey.build(publicKeyItem.getId(), new String[]{publicKeyItem.getType()},null,publicKeyItem.getPublicKeyBase58(),null,null);
        Authentication auth = mapper.convertValue(patchValue1, Authentication.class);
        auth.setJsonLdObjectKeyValue("publicKey",publicKey);

        toUpdate2.getAuthentications().add(auth);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.writeValue(new File("/home/cn/sovrin_driver/uni-registrar-driver-did-v1/src/test/resources/test_dids/result3.json"), toUpdate2);

        System.out.println();

    }
}
