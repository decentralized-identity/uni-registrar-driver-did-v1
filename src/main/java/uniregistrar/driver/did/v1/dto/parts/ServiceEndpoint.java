package uniregistrar.driver.did.v1.dto.parts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"@context", "type"})
public class ServiceEndpoint{

	@JsonProperty("instances")
	private List<String> instances;

	@JsonProperty("type")
	private String type;

	@JsonProperty("@context")
	private String context;

	public void setInstances(List<String> instances){
		this.instances = instances;
	}

	public List<String> getInstances(){
		return instances;
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
}