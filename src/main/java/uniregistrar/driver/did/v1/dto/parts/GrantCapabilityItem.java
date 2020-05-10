package uniregistrar.driver.did.v1.dto.parts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"id", "type", "controller"})
public class GrantCapabilityItem {

    @JsonProperty("publicKey")
    private PublicKeyItem publicKeyItem;

    @JsonProperty("publicKeyBase58")
    private String publicKeyBase58;

    @JsonProperty("controller")
    private String controller;

    @JsonProperty("id")
    private String id;
//    @JsonProperty("owner")
//    private String owner;

    @JsonProperty("type")
    private String type;


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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PublicKeyItem getPublicKeyItem() {
        if(this.publicKeyItem == null && this.publicKeyBase58 != null){
            PublicKeyItem pItem = new PublicKeyItem();
            pItem.setType(this.type);
            pItem.setPublicKeyBase58(this.publicKeyBase58);
            if(this.controller != null) {
                pItem.setController(this.controller);
//                this.owner = controller;
//                pItem.setOwner(controller);
            }
//            else{
//                pItem.setOwner(this.owner);
//                pItem.setController(this.owner);
//                this.controller = owner;
//            }

            pItem.setId(this.id);
        }

        return this.publicKeyItem;
    }

    public void setPublicKeyItem(PublicKeyItem publicKeyItem) {
        this.publicKeyItem = publicKeyItem;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}