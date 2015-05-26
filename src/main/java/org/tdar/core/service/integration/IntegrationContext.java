package org.tdar.core.service.integration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.utils.PersistableUtils;

/**
 * Helper class to corral all of the integration info in one place
 * 
 * @author Adam Brin
 * 
 */
@XmlRootElement(name = "integrationContext")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class IntegrationContext implements Serializable {

    private static final long serialVersionUID = 6206089620213511145L;
    private TdarUser creator;
    private Date dateCreated = new Date();
    private List<IntegrationColumn> integrationColumns = new ArrayList<>();
    private List<DataTable> dataTables;
    private String title;
    private String description;

    private List<String> errorMessages = new ArrayList<>();

    public IntegrationContext() {
    }

    public IntegrationContext(TdarUser creator, List<IntegrationColumn> integrationColumns) {
        this.creator = creator;
        this.integrationColumns = integrationColumns;
    }

    public TdarUser getCreator() {
        return creator;
    }

    public void setCreator(TdarUser creator) {
        this.creator = creator;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    @XmlElement(name = "integrationColumn")
    @XmlElementWrapper(name = "integrationColumns")
    public List<IntegrationColumn> getIntegrationColumns() {
        return integrationColumns;
    }

    public void setIntegrationColumns(List<IntegrationColumn> integrationColumns) {
        this.integrationColumns = integrationColumns;
    }

    private transient String tempTableName;
    private transient DataTable tempTable;

    @Transient
    public String getTempTableName() {
        if (tempTableName != null) {
            return tempTableName;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("t_").append(System.currentTimeMillis());
        if (CollectionUtils.isNotEmpty(dataTables)) {
            for (DataTable dataTable : dataTables) {
                sb.append("_").append(dataTable.getInternalName());
            }
        }
        tempTableName = sb.toString();
        return tempTableName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("creator: %s\n", getCreator()));
        sb.append(String.format("dataTables: %s\n", PersistableUtils.extractIds(dataTables)));
        sb.append(StringUtils.join(getIntegrationColumns(), "\n"));
        return sb.toString();
    }

    @XmlElementWrapper(name = "dataTables")
    @XmlElement(name = "dataTable")
    public List<DataTable> getDataTables() {
        return dataTables;
    }

    public void setDataTables(List<DataTable> dataTables) {
        this.dataTables = dataTables;
    }

    public DataTable getTempTable() {
        return tempTable;
    }

    public void setTempTable(DataTable tempTable) {
        this.tempTable = tempTable;
    }

    public void setErrorMessages(List<String> messages) {
        this.errorMessages = messages;
    }

    public List<String> getErrorMessages() {
        return Collections.unmodifiableList(errorMessages);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean hasCountColumn() {
        if (CollectionUtils.isEmpty(integrationColumns)) {
            return false;
        }

        for (IntegrationColumn col : getIntegrationColumns()) {
            if (col.isCountColumn()) {
                return true;
            }
        }
        return false;
    }

}
