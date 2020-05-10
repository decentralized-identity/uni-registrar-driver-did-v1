package uniregistrar.driver.did.v1.dto.parts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"op", "path", "value"})
public class PatchItem {

    @JsonProperty("op")
    private String op;

    @JsonProperty("path")
    private String path;

    @JsonProperty("value")
    private PatchValue patchValue;

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public PatchValue getPatchValue() {
        return patchValue;
    }

    public void setPatchValue(PatchValue patchValue) {
        this.patchValue = patchValue;
    }

    @Override
    public String toString() {
        return
                "PatchItem{" +
                        "op = '" + op + '\'' +
                        ",path = '" + path + '\'' +
                        ",value = '" + patchValue + '\'' +
                        "}";
    }
}