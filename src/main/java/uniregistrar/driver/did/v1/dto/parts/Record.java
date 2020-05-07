package uniregistrar.driver.did.v1.dto.parts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Record {

    @JsonProperty("grantCapability")
    private List<GrantCapabilityItem> grantCapability;

    @JsonProperty("id")
    private String id;

    @JsonProperty("@context")
    private String context;

    @JsonProperty("invokeCapability")
    private List<InvokeCapabilityItem> invokeCapability;

    @JsonProperty("authentication")
    private List<AuthenticationItem> authentication;

    public List<GrantCapabilityItem> getGrantCapability() {
        return grantCapability;
    }

    public void setGrantCapability(List<GrantCapabilityItem> grantCapability) {
        this.grantCapability = grantCapability;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public List<InvokeCapabilityItem> getInvokeCapability() {
        return invokeCapability;
    }

    public void setInvokeCapability(List<InvokeCapabilityItem> invokeCapability) {
        this.invokeCapability = invokeCapability;
    }

    public List<AuthenticationItem> getAuthentication() {
        return authentication;
    }

    public void setAuthentication(List<AuthenticationItem> authentication) {
        this.authentication = authentication;
    }
}