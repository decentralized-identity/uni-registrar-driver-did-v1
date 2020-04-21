package uniregistrar.driver.did.v1.dto.parts;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PatchItem{

	@JsonProperty("op")
	private String op;

	@JsonProperty("path")
	private String path;

	@JsonProperty("value")
	private Value value;

	public void setOp(String op){
		this.op = op;
	}

	public String getOp(){
		return op;
	}

	public void setPath(String path){
		this.path = path;
	}

	public String getPath(){
		return path;
	}

	public void setValue(Value value){
		this.value = value;
	}

	public Value getValue(){
		return value;
	}

	@Override
 	public String toString(){
		return 
			"PatchItem{" + 
			"op = '" + op + '\'' + 
			",path = '" + path + '\'' + 
			",value = '" + value + '\'' + 
			"}";
		}
}