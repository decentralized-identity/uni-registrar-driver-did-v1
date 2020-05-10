package uniregistrar.driver.did.v1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"created", "ledger", "ledgerMode", "published"})
public class V1MetaData {

    @JsonProperty("ledger")
    private String ledger;

    @JsonProperty("created")
    private String created;

    @JsonProperty("ledgerMode")
    private String ledgerMode;

    @JsonProperty("published")
    private String published;

    @JsonProperty("updated")
    private String updated;

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getLedger() {
        return ledger;
    }

    public void setLedger(String ledger) {
        this.ledger = ledger;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getLedgerMode() {
        return ledgerMode;
    }

    public void setLedgerMode(String ledgerMode) {
        this.ledgerMode = ledgerMode;
    }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
    }
}