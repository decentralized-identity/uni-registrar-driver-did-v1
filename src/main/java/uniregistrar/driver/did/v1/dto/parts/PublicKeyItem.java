package uniregistrar.driver.did.v1.dto.parts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"id", "type", "controller", "publicKeyBase58"})
public class PublicKeyItem {

    @JsonProperty("controller")
    private String controller;

//    @JsonProperty("owner")
//    private String owner;

    @JsonProperty("publicKeyBase58")
    private String publicKeyBase58;
    @JsonProperty("id")
    private String id;
    @JsonProperty("type")
    private String type;


    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    public String getPublicKeyBase58() {
        return publicKeyBase58;
    }

    public void setPublicKeyBase58(String publicKeyBase58) {
        this.publicKeyBase58 = publicKeyBase58;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}