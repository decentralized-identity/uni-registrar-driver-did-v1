package uniregistrar.driver.did.v1.dto.parts;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProofItem{

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

	public void setEquihashParameterK(int equihashParameterK){
		this.equihashParameterK = equihashParameterK;
	}

	public int getEquihashParameterK(){
		return equihashParameterK;
	}

	public void setEquihashParameterN(int equihashParameterN){
		this.equihashParameterN = equihashParameterN;
	}

	public int getEquihashParameterN(){
		return equihashParameterN;
	}

	public void setProofValue(String proofValue){
		this.proofValue = proofValue;
	}

	public String getProofValue(){
		return proofValue;
	}

	public void setType(String type){
		this.type = type;
	}

	public String getType(){
		return type;
	}

	public void setNonce(String nonce){
		this.nonce = nonce;
	}

	public String getNonce(){
		return nonce;
	}

	public void setCreator(String creator){
		this.creator = creator;
	}

	public String getCreator(){
		return creator;
	}

	public void setCapability(String capability){
		this.capability = capability;
	}

	public String getCapability(){
		return capability;
	}

	public void setCreated(String created){
		this.created = created;
	}

	public String getCreated(){
		return created;
	}

	public void setJws(String jws){
		this.jws = jws;
	}

	public String getJws(){
		return jws;
	}

	public void setCapabilityAction(String capabilityAction){
		this.capabilityAction = capabilityAction;
	}

	public String getCapabilityAction(){
		return capabilityAction;
	}

	public void setProofPurpose(String proofPurpose){
		this.proofPurpose = proofPurpose;
	}

	public String getProofPurpose(){
		return proofPurpose;
	}

	@Override
 	public String toString(){
		return 
			"ProofItem{" + 
			"equihashParameterK = '" + equihashParameterK + '\'' + 
			",equihashParameterN = '" + equihashParameterN + '\'' + 
			",proofValue = '" + proofValue + '\'' + 
			",type = '" + type + '\'' + 
			",nonce = '" + nonce + '\'' + 
			",creator = '" + creator + '\'' + 
			",capability = '" + capability + '\'' + 
			",created = '" + created + '\'' + 
			",jws = '" + jws + '\'' + 
			",capabilityAction = '" + capabilityAction + '\'' + 
			",proofPurpose = '" + proofPurpose + '\'' + 
			"}";
		}
}