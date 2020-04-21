package uniregistrar.driver.did.v1;

import did.DID;
import uniregistrar.driver.did.v1.util.V1;
import uniregistrar.request.RegisterRequest;
import uniregistrar.state.RegisterState;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class PlayGround {

    public static void main(String[] args) throws Exception{

//        V1.PROOF_PURPOSES().forEach(System.out::println);

        DidV1Driver driver = new DidV1Driver();

        RegisterRequest request = new RegisterRequest();
        Map<String,Object> optMap = new HashMap<>();
        request.setOptions(optMap);

        request.getOptions().put("ledger","test");
        request.getOptions().put("keytype","ed25519");

        RegisterState state = driver.register(request);

        System.out.println(state);

    }
}
