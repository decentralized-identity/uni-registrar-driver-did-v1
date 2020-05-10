package uniregistrar.driver.did.v1.util;

public enum ErrorMessages {

    REQUEST_IS_NULL("Request object is null", 400),
    NO_PROOF_GIVEN("Request does not contain any proof key", 412),
    PROOF_METHOD_NOT_SUPPORTED("No supported proof type is provided!", 404),
    NO_POOF_PURPOSE_GIVEN("No proof purpose is given", 412),
    FORBIDDEN_UPDATE("Related DID Document cannot be updated!", 403),
    NO_JWS_GIVEN("JWS data is null", 412),
    DIDDOC_NOT_FOUNT("Cannot locate the did document!", 404),
    SIGNATURE_ERROR("Signature error!", 412),
    SIGNATURE_MISMATCH("Cannot update with the given signature!", 412),
    DIDDOC_PARSING_ERROR("Cannot read the DID Doc!", 500),
    PROOF_PURPOSE_NOT_ACCEPTABLE("Given roof purpose is not allowed to invoke this operation.", 401),
    CANNOT_WRITE("Internal error!", 500),
    GENERIC_BAD_REQUEST("Very bad request! Cannot complete the operations.", 400);


    private final String msg;
    private final int errCode;

    private ErrorMessages(String msg, int errCode) {
        this.msg = msg;
        this.errCode = errCode;
    }

    public String getMsg() {
        return msg;
    }

    public int getErrCode() {
        return errCode;
    }

}
