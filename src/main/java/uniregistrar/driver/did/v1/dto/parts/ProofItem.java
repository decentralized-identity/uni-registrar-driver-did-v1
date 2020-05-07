package uniregistrar.driver.did.v1.dto.parts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProofItem {

    @JsonProperty("equihashParameterK")
    private int equihashParameterK;

    @JsonProperty("equihashParameterN")
    private int equihashParameterN;

    @JsonProperty("proofValue")
    private String proofValue;

    @JsonProperty("type")
    private String type;

    @JsonProperty("nonce")
    private String nonce;

    @JsonProperty("creator")
    private String creator;

    @JsonProperty("capability")
    private String capability;

    @JsonProperty("created")
    private String created;

    @JsonProperty("jws")
    private String jws;

    @JsonProperty("capabilityAction")
    private String capabilityAction;

    @JsonProperty("proofPurpose")
    private String proofPurpose;

    public int getEquihashParameterK() {
        return equihashParameterK;
    }

    public void setEquihashParameterK(int equihashParameterK) {
        this.equihashParameterK = equihashParameterK;
    }

    public int getEquihashParameterN() {
        return equihashParameterN;
    }

    public void setEquihashParameterN(int equihashParameterN) {
        this.equihashParameterN = equihashParameterN;
    }

    public String getProofValue() {
        return proofValue;
    }

    public void setProofValue(String proofValue) {
        this.proofValue = proofValue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCapability() {
        return capability;
    }

    public void setCapability(String capability) {
        this.capability = capability;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getJws() {
        return jws;
    }

    public void setJws(String jws) {
        this.jws = jws;
    }

    public String getCapabilityAction() {
        return capabilityAction;
    }

    public void setCapabilityAction(String capabilityAction) {
        this.capabilityAction = capabilityAction;
    }

    public String getProofPurpose() {
        return proofPurpose;
    }

    public void setProofPurpose(String proofPurpose) {
        this.proofPurpose = proofPurpose;
    }
}