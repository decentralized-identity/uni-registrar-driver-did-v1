package uniregistrar.driver.did.v1.util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class V1 {
    public static final String DEFAULT_KEY_TYPE = "Ed25519VerificationKey2018";
    public static final String DEFAULT_MODE = "dev";
    public static final String DEFAULT_DID_TYPE = "nym";

    public static final boolean OVERWRITE_ON_UPDATE = true;

    public static List<String> PROOF_PURPOSES() {
        return Stream.of(PROOF_PURPOSES.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    public static List<String> DID_TYPES() {
        return Stream.of(DID_TYPES.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    public static List<String> SUPPORTED_KEY_TYPES() {
        return Stream.of(SUPPORTED_KEY_TYPES.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    private enum SUPPORTED_KEY_TYPES {
        RsaVerificationKey2018,
        Ed25519VerificationKey2018
    }

    private enum DID_TYPES {
        nym,
        uuid
    }

    private enum PROOF_PURPOSES {
        authentication,
        capabilityDelegation,
        capabilityInvocation,
        assertionMethod,
        keyAgreement,
        contractAgreement
    }
}
