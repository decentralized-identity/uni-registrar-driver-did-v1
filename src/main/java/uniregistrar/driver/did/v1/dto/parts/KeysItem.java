package uniregistrar.driver.did.v1.dto.parts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"id", "type", "controller"})
public class KeysItem {

    @JsonProperty("publicKeyBase58")
    private String publicKeyBase58;

    @JsonProperty("controller")
    private String controller;
//    @JsonProperty("owner")
//    private String owner;

    @JsonProperty("privateKeyJwk")
    private PrivateKeyJwk privateKeyJwk;
    @JsonProperty("id")
    private String id;
    @JsonProperty("publicKeyDIDURL")
    private String publicKeyDIDURL;
    @JsonProperty("type")
    private String type;
    @JsonProperty("privateKeyBase58")
    private String privateKeyBase58;


    public String getPublicKeyBase58() {
        return publicKeyBase58;
    }

    public void setPublicKeyBase58(String publicKeyBase58) {
        this.publicKeyBase58 = publicKeyBase58;
    }

    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    public PrivateKeyJwk getPrivateKeyJwk() {
        return privateKeyJwk;
    }

    public void setPrivateKeyJwk(PrivateKeyJwk privateKeyJwk) {
        this.privateKeyJwk = privateKeyJwk;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPublicKeyDIDURL() {
        return publicKeyDIDURL;
    }

    public void setPublicKeyDIDURL(String publicKeyDIDURL) {
        this.publicKeyDIDURL = publicKeyDIDURL;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPrivateKeyBase58() {
        return privateKeyBase58;
    }

    public void setPrivateKeyBase58(String privateKeyBase58) {
        this.privateKeyBase58 = privateKeyBase58;
    }
}