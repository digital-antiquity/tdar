package org.tdar.odata.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.tdar.core.bean.resource.datatable.DataTable;

// TODO RR: need some unit tests on this.

// Not abstract in a Java class sense.
// Abstract in the sense that it can be used to represent any entity 
// derived from EntitySet.T_DATA_RECORDS.
public class AbstractDataRecord {

    private Long id;
    private DataTable dataTable;
    private Map<String, Object> values = new HashMap<String, Object>();

    public AbstractDataRecord(final Long id, DataTable dataTable) {
        super();
        this.id = id;
        this.dataTable = dataTable;
    }

    public Long getId() {
        return id;
    }

    public String getTableName() {
        return dataTable.getName();
    }

    public DataTable getDataTable() {
        return dataTable;
    }

    public Object get(String name) {
        if ("id".equals(name))
        {
            return id;
        }
        return values.get(name);
    }

    public void put(String name, Object value) {
        if ("id".equals(name))
        {
            if (value instanceof Long)
            {
                this.id = (Long) value;
            }
            else
            {
                throw new IllegalArgumentException("AbstractDataRecord can only use a Long for the id");
            }
        }
        values.put(name, value);
    }

    public Set<String> propertyNames()
    {
        return values.keySet();
    }

    public Map<?, ?> asMap() {
        return values;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((id == null) ? 0 : id.hashCode());
        result = (prime * result) + ((getTableName() == null) ? 0 : getTableName().hashCode());
        result = (prime * result) + ((values == null) ? 0 : values.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        AbstractDataRecord other = (AbstractDataRecord) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (getTableName() == null) {
            if (other.getTableName() != null) {
                return false;
            }
        } else if (!getTableName().equals(other.getTableName())) {
            return false;
        }
        if (values == null) {
            if (other.values != null) {
                return false;
            }
        } else if (!values.equals(other.values)) {
            return false;
        }
        return true;
    }
}
