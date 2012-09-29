package org.tdar.core.bean.resource;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.bean.resource.dataTable.DataTableColumn;
import org.tdar.core.bean.resource.dataTable.DataTableRelationship;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * $Id$
 * 
 * A Dataset information resource can currently be an Excel file, Access MDB file, or plaintext CSV file.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Entity
@Indexed
@Table(name = "dataset")
@XStreamAlias("dataset")
@XmlRootElement(name = "dataset")
public class Dataset extends InformationResource {

    private static final long serialVersionUID = -5796154884019127904L;

    @XStreamOmitField
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dataset")
    @IndexedEmbedded
    private Set<DataTable> dataTables = new LinkedHashSet<DataTable>();

    @OneToMany(mappedBy = "dataset")
    private Set<DataTableRelationship> relationships = new HashSet<DataTableRelationship>();

    public Dataset() {
        setResourceType(ResourceType.DATASET);
    }

    @XmlElementWrapper(name = "dataTables")
    @XmlElement(name = "dataTable")
    public Set<DataTable> getDataTables() {
        return dataTables;
    }

    public void setDataTables(Set<DataTable> dataTables) {
        this.dataTables = dataTables;
    }

    @Field
    public Boolean isIntegratable() {
        for (DataTable dt : getDataTables()) {
            for (DataTableColumn dtc : dt.getDataTableColumns()) {
                if (dtc.getDefaultOntology() != null)
                    return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    /**
     * @param string
     * @return
     */
    @Transient
    public DataTable getDataTableByName(String name) {
        for (DataTable dt : getDataTables()) {
            if (dt.getName().equals(name))
                return dt;
        }
        return null;
    }

    @Transient
    public DataTable getDataTableByGenericName(String name) {
        String tempname = "(\\w+)_(\\d+)_" + name;
        for (DataTable dt : getDataTables()) {
            logger.info(dt.getName() + " - " + tempname);
            if (dt.getName().matches(tempname))
                return dt;
        }
        return null;
    }

    public void setRelationships(Set<DataTableRelationship> relationships) {
        this.relationships = relationships;
    }

    public Set<DataTableRelationship> getRelationships() {
        return relationships;
    }

}
