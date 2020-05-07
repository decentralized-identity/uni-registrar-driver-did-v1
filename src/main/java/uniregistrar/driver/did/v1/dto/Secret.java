package uniregistrar.driver.did.v1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import uniregistrar.driver.did.v1.dto.parts.KeysItem;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Secret {

    @JsonProperty("keys")
    private List<KeysItem> keys;

    public List<KeysItem> getKeys() {
        return keys;
    }

    public void setKeys(List<KeysItem> keys) {
        this.keys = keys;
    }
}