package uniregistrar.driver.did.v1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import uniregistrar.driver.did.v1.dto.parts.*;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"@context", "id", "authentication", "assertionMethod", "grantCapability", "invokeCapability"})
public class V1DIDDoc {

    @JsonProperty("assertionMethod")
    private List<AssertionMethodItem> assertionMethod;
    @JsonProperty("capabilityDelegation")
    private List<CapabilityDelegationItem> capabilityDelegation;
    @JsonProperty("id")
    private String id;
    @JsonProperty("capabilityInvocation")
    private List<CapabilityInvocationItem> capabilityInvocation;
    @JsonProperty("@context")
    private List<String> context;
    @JsonProperty("authentication")
    private List<AuthenticationItem> authentication;
    @JsonProperty("grantCapability")
    private List<GrantCapabilityItem> grantCapability;
    @JsonProperty("invokeCapability")
    private List<InvokeCapabilityItem> invokeCapability;

    @JsonProperty("service")
    private List<ServiceItem> service;

    public List<ServiceItem> getService() {
        return service;
    }

    public void setService(List<ServiceItem> service) {
        this.service = service;
    }

    public List<GrantCapabilityItem> getGrantCapability() {
        return grantCapability;
    }

    public void setGrantCapability(List<GrantCapabilityItem> grantCapability) {
        this.grantCapability = grantCapability;
    }

    public List<InvokeCapabilityItem> getInvokeCapability() {
        return invokeCapability;
    }

    public void setInvokeCapability(List<InvokeCapabilityItem> invokeCapability) {
        this.invokeCapability = invokeCapability;
    }

    public List<AssertionMethodItem> getAssertionMethod() {
        return assertionMethod;
    }

    public void setAssertionMethod(List<AssertionMethodItem> assertionMethod) {
        this.assertionMethod = assertionMethod;
    }

    public List<CapabilityDelegationItem> getCapabilityDelegation() {
        return capabilityDelegation;
    }

    public void setCapabilityDelegation(List<CapabilityDelegationItem> capabilityDelegation) {
        this.capabilityDelegation = capabilityDelegation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<CapabilityInvocationItem> getCapabilityInvocation() {
        return capabilityInvocation;
    }

    public void setCapabilityInvocation(List<CapabilityInvocationItem> capabilityInvocation) {
        this.capabilityInvocation = capabilityInvocation;
    }

    public List<String> getContext() {
        return context;
    }

    public void setContext(List<String> context) {
        this.context = context;
    }

    public List<AuthenticationItem> getAuthentication() {
        return authentication;
    }

    public void setAuthentication(List<AuthenticationItem> authentication) {
        this.authentication = authentication;
    }
}