package uniregistrar.driver.did.v1.dto.parts;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Record{

	@JsonProperty("grantCapability")
	private List<GrantCapabilityItem> grantCapability;

	@JsonProperty("id")
	private String id;

	@JsonProperty("@context")
	private String context;

	@JsonProperty("invokeCapability")
	private List<InvokeCapabilityItem> invokeCapability;

	@JsonProperty("authentication")
	private List<AuthenticationItem> authentication;

	public void setGrantCapability(List<GrantCapabilityItem> grantCapability){
		this.grantCapability = grantCapability;
	}

	public List<GrantCapabilityItem> getGrantCapability(){
		return grantCapability;
	}

	public void setId(String id){
		this.id = id;
	}

	public String getId(){
		return id;
	}

	public void setContext(String context){
		this.context = context;
	}

	public String getContext(){
		return context;
	}

	public void setInvokeCapability(List<InvokeCapabilityItem> invokeCapability){
		this.invokeCapability = invokeCapability;
	}

	public List<InvokeCapabilityItem> getInvokeCapability(){
		return invokeCapability;
	}

	public void setAuthentication(List<AuthenticationItem> authentication){
		this.authentication = authentication;
	}

	public List<AuthenticationItem> getAuthentication(){
		return authentication;
	}

	@Override
 	public String toString(){
		return 
			"Record{" + 
			"grantCapability = '" + grantCapability + '\'' + 
			",id = '" + id + '\'' + 
			",@context = '" + context + '\'' + 
			",invokeCapability = '" + invokeCapability + '\'' + 
			",authentication = '" + authentication + '\'' + 
			"}";
		}
}