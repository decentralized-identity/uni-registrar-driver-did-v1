package uniregistrar.driver.did.v1.dto.parts;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KeysItem{

	@JsonProperty("publicKeyBase58")
	private String publicKeyBase58;

	@JsonProperty("controller")
	private String controller;

	@JsonProperty("privateKeyJwk")
	private PrivateKeyJwk privateKeyJwk;

	@JsonProperty("id")
	private String id;

	@JsonProperty("publicKeyDIDURL")
	private String publicKeyDIDURL;

	@JsonProperty("type")
	private String type;

	@JsonProperty("privateKeyBase58")
	private String privateKeyBase58;

	public void setPublicKeyBase58(String publicKeyBase58){
		this.publicKeyBase58 = publicKeyBase58;
	}

	public String getPublicKeyBase58(){
		return publicKeyBase58;
	}

	public void setController(String controller){
		this.controller = controller;
	}

	public String getController(){
		return controller;
	}

	public void setPrivateKeyJwk(PrivateKeyJwk privateKeyJwk){
		this.privateKeyJwk = privateKeyJwk;
	}

	public PrivateKeyJwk getPrivateKeyJwk(){
		return privateKeyJwk;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}

	public void setPublicKeyDIDURL(String publicKeyDIDURL){
		this.publicKeyDIDURL = publicKeyDIDURL;
	}

	public String getPublicKeyDIDURL(){
		return publicKeyDIDURL;
	}

	public void setType(String type){
		this.type = type;
	}

	public String getType(){
		return type;
	}

	public void setPrivateKeyBase58(String privateKeyBase58){
		this.privateKeyBase58 = privateKeyBase58;
	}

	public String getPrivateKeyBase58(){
		return privateKeyBase58;
	}

	@Override
 	public String toString(){
		return 
			"KeysItem{" + 
			"publicKeyBase58 = '" + publicKeyBase58 + '\'' + 
			",controller = '" + controller + '\'' + 
			",privateKeyJwk = '" + privateKeyJwk + '\'' + 
			",id = '" + id + '\'' + 
			",publicKeyDIDURL = '" + publicKeyDIDURL + '\'' + 
			",type = '" + type + '\'' + 
			",privateKeyBase58 = '" + privateKeyBase58 + '\'' + 
			"}";
		}
}