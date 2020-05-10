package uniregistrar.driver.did.v1.dto.parts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrivateKeyJwk {

    @JsonProperty("kty")
    private String kty;

    @JsonProperty("d")
    private String D;

    @JsonProperty("crv")
    private String crv;

    @JsonProperty("x")
    private String X;

    public String getKty() {
        return kty;
    }

    public void setKty(String kty) {
        this.kty = kty;
    }

    public String getD() {
        return D;
    }

    public void setD(String D) {
        this.D = D;
    }

    public String getCrv() {
        return crv;
    }

    public void setCrv(String crv) {
        this.crv = crv;
    }

    public String getX() {
        return X;
    }

    public void setX(String X) {
        this.X = X;
    }

    @Override
    public String toString() {
        return
                "PrivateKeyJwk{" +
                        "kty = '" + kty + '\'' +
                        ",d = '" + D + '\'' +
                        ",crv = '" + crv + '\'' +
                        ",x = '" + X + '\'' +
                        "}";
    }
}