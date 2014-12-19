package org.tdar.core.bean.integration;

import org.tdar.core.bean.Persistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by jim on 12/8/14.
 */
@Entity
@Table(name = "data_integration_workflow")
public class DataIntegrationWorkflow extends Persistable.Base{

    private static final long serialVersionUID = 0xBADFACE;

    @Column(nullable=false)
    private String title;

    @Column(nullable=false)
    private String description;

    @Column(name="json_data")
    private String jsonData;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }
}
