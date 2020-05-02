package uniregistrar.driver.did.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import uniregistrar.driver.did.v1.dto.parts.PatchItem;
import uniregistrar.driver.did.v1.dto.parts.ProofItem;

import java.util.List;

public class V1UpdateRequest {

	@JsonProperty("patch")
	private List<PatchItem> patch;

	@JsonProperty("proof")
	private List<ProofItem> proof;

	@JsonProperty("type")
	private String type;

	@JsonProperty("@context")
	private String context;

	public void setPatch(List<PatchItem> patch){
		this.patch = patch;
	}

	public List<PatchItem> getPatch(){
		return patch;
	}

	public void setProof(List<ProofItem> proof){
		this.proof = proof;
	}

	public List<ProofItem> getProof(){
		return proof;
	}

	public void setType(String type){
		this.type = type;
	}

	public String getType(){
		return type;
	}

	public void setContext(String context){
		this.context = context;
	}

	public String getContext(){
		return context;
	}

	@Override
 	public String toString(){
		return 
			"UpdatePost{" + 
			"patch = '" + patch + '\'' + 
			",proof = '" + proof + '\'' + 
			",type = '" + type + '\'' + 
			",@context = '" + context + '\'' + 
			"}";
		}
}