package uniregistrar.driver.did.v1.dto.parts;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PublicKey{

	@JsonProperty("controller")
	private String controller;

	@JsonProperty("publicKeyBase58")
	private String publicKeyBase58;

	@JsonProperty("id")
	private String id;

	@JsonProperty("type")
	private String type;

	public void setController(String controller){
		this.controller = controller;
	}

	public String getController(){
		return controller;
	}

	public void setPublicKeyBase58(String publicKeyBase58){
		this.publicKeyBase58 = publicKeyBase58;
	}

	public String getPublicKeyBase58(){
		return publicKeyBase58;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
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
			"PublicKey{" + 
			"owner = '" + controller + '\'' +
			",publicKeyBase58 = '" + publicKeyBase58 + '\'' + 
			",id = '" + id + '\'' + 
			",type = '" + type + '\'' + 
			"}";
		}
}