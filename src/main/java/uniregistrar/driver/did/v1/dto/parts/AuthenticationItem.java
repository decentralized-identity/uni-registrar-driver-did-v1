package uniregistrar.driver.did.v1.dto.parts;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthenticationItem{

	@JsonProperty("publicKey")
	private PublicKey publicKey;

	@JsonProperty("type")
	private String type;

	public void setPublicKey(PublicKey publicKey){
		this.publicKey = publicKey;
	}

	public PublicKey getPublicKey(){
		return publicKey;
	}

	public void setType(String type){
		this.type = type;
	}

	public String getType(){
		return type;
	}

	@Override
 	public String toString(){
		return 
			"AuthenticationItem{" + 
			"publicKey = '" + publicKey + '\'' + 
			",type = '" + type + '\'' + 
			"}";
		}
}