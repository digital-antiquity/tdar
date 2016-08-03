package org.tdar.core.bean.resource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.core.bean.resource.datatable.DataTableColumn;
import org.tdar.core.bean.resource.datatable.DataTableRelationship;
import org.tdar.utils.PersistableUtils;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * A Dataset information resource can currently be an Excel file, Access MDB file, or plaintext CSV file.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Entity
//@Indexed
@Table(name = "dataset")
@XmlRootElement(name = "dataset")
public class Dataset extends InformationResource {

    private static final long serialVersionUID = -5796154884019127904L;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "dataset", orphanRemoval = true)
    //@IndexedEmbedded
    private Set<DataTable> dataTables = new LinkedHashSet<DataTable>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "dataset_id")
    private Set<DataTableRelationship> relationships = new HashSet<DataTableRelationship>();

    public Dataset() {
        setResourceType(ResourceType.DATASET);
    }

    private transient Map<String, DataTable> nameToTableMap;
    private transient Map<String, DataTable> genericNameToTableMap;
    private transient int dataTableHashCode = -1;

    @XmlElementWrapper(name = "dataTables")
    @XmlElement(name = "dataTable")
    public Set<DataTable> getDataTables() {
        return dataTables;
    }

    @XmlTransient
    @Transient
    public List<DataTable> getSortedDataTables() {
        List<DataTable> tables = new ArrayList<>(dataTables);
        Collections.sort(tables, new Comparator<DataTable>() {

            @Override
            public int compare(DataTable o1, DataTable o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return tables;
    }
    
    public void setDataTables(Set<DataTable> dataTables) {
        this.dataTables = dataTables;
    }


    /**
     * @param string
     * @return
     */
    @Transient
    public DataTable getDataTableByName(String name) {
        if ((nameToTableMap == null) || !Objects.equals(dataTableHashCode, getDataTables().hashCode())) {
            initializeNameToTableMap();
        }
        // NOTE: IF the HashCode is not implemented properly, on DataTableColumn, this may get out of sync
        return nameToTableMap.get(name);
    }

    /**
     * @param string
     * @return
     */
    @Transient
    public DataTable getDataTableById(Long id) {
        for (DataTable datatable : getDataTables()) {
            if (Objects.equals(datatable.getId(), id)) {
                return datatable;
            }
        }
        return null;
    }

    private void initializeNameToTableMap() {
        nameToTableMap = new HashMap<String, DataTable>();
        genericNameToTableMap = new HashMap<String, DataTable>();

        for (DataTable dt : getDataTables()) {
            nameToTableMap.put(dt.getName(), dt);
            String simpleName = dt.getName().replaceAll("^((\\w+_)(\\d+)(_?))", "");
            genericNameToTableMap.put(simpleName, dt);
        }

    }

    @Transient
    public DataTable getDataTableByGenericName(String name) {
        if ((genericNameToTableMap == null) || !Objects.equals(dataTableHashCode, getDataTables().hashCode())) {
            initializeNameToTableMap();
        }
        // NOTE: IF the HashCode is not implemented properly, on DataTableColumn, this may get out of sync
        return genericNameToTableMap.get(name);
    }

    public void setRelationships(Set<DataTableRelationship> relationships) {
        this.relationships = relationships;
    }

    public Set<DataTableRelationship> getRelationships() {
        return relationships;
    }

    @Transient
    public boolean hasMappingColumns() {
        if (CollectionUtils.isEmpty(getDataTables())) {
            return false;
        }
        for (DataTable dt : getDataTables()) {
            if (CollectionUtils.isEmpty(dt.getDataTableColumns())) {
                return false;
            }
            for (DataTableColumn col : dt.getDataTableColumns()) {
                if (col.isMappingColumn()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Transient
    public boolean hasCodingColumns() {
        if (CollectionUtils.isEmpty(getDataTables())) {
            return false;
        }
        for (DataTable dt : getDataTables()) {
            if (CollectionUtils.isEmpty(dt.getDataTableColumns())) {
                return false;
            }
            for (DataTableColumn col : dt.getDataTableColumns()) {
                if (PersistableUtils.isNotNullOrTransient(col.getDefaultCodingSheet())) {
                    return true;
                }
            }
        }
        return false;
    }
}
