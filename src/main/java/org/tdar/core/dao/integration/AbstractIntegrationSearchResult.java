package org.tdar.core.dao.integration;

import java.io.Serializable;

import org.tdar.utils.json.JsonIntegrationFilter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonView;

@JsonAutoDetect
public class AbstractIntegrationSearchResult implements Serializable {

    private static final long serialVersionUID = 5113444746580940193L;
    private int totalResults;

    @JsonView(JsonIntegrationFilter.class)
    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }
}
