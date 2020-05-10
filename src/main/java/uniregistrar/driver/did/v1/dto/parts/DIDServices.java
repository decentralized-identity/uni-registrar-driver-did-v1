package uniregistrar.driver.did.v1.dto.parts;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DIDServices{

	@JsonProperty("service")
	private List<ServiceItem> service;

	public void setService(List<ServiceItem> service){
		this.service = service;
	}

	public List<ServiceItem> getService(){
		return service;
	}
}