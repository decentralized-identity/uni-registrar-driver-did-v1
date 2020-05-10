package uniregistrar.driver.did.v1.dto.parts;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SpamCost{

	@JsonProperty("amount")
	private String amount;

	@JsonProperty("currency")
	private String currency;

	public void setAmount(String amount){
		this.amount = amount;
	}

	public String getAmount(){
		return amount;
	}

	public void setCurrency(String currency){
		this.currency = currency;
	}

	public String getCurrency(){
		return currency;
	}
}