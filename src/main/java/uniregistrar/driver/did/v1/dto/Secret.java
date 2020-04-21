package uniregistrar.driver.did.v1.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import uniregistrar.driver.did.v1.dto.parts.KeysItem;

public class Secret{

	@JsonProperty("keys")
	private List<KeysItem> keys;

	public void setKeys(List<KeysItem> keys){
		this.keys = keys;
	}

	public List<KeysItem> getKeys(){
		return keys;
	}

	@Override
 	public String toString(){
		return 
			"Secret{" + 
			"keys = '" + keys + '\'' + 
			"}";
		}
}