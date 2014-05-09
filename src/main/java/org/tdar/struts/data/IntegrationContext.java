/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.struts.data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.resource.datatable.DataTable;

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
    private Person creator;
    private Date dateCreated = new Date();
    private List<IntegrationColumn> integrationColumns;
    private List<DataTable> dataTables;

    public IntegrationContext() {
    }

    public IntegrationContext(Person creator, List<IntegrationColumn> integrationColumns) {
        this.creator = creator;
        this.integrationColumns = integrationColumns;
    }

    public Person getCreator() {
        return creator;
    }

    public void setCreator(Person creator) {
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

    @Transient
    public String getTempTableName() {
        StringBuilder sb = new StringBuilder();
        for (DataTable dataTable : dataTables) {
            sb.append(dataTable.getInternalName()).append("_");
        }
        sb.append("_").append(System.currentTimeMillis());
        return sb.toString();
    }

    public List<DataTable> getDataTables() {
        return dataTables;
    }

    public void setDataTables(List<DataTable> dataTables) {
        this.dataTables = dataTables;
    }

}
