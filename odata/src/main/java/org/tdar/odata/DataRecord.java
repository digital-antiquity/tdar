package org.tdar.odata;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.tdar.core.bean.resource.datatable.DataTable;
import org.tdar.db.model.abstracts.AbstractDataRecord;

// TODO RR: need some unit tests on this.

// Not abstract in a Java class sense.
// Abstract in the sense that it can be used to represent any entity 
// derived from EntitySet.T_DATA_RECORDS.
public class DataRecord implements AbstractDataRecord {

    private Long id;
    private DataTable dataTable;
    private Map<String, Object> values = new HashMap<String, Object>();

    public DataRecord(final Long id, DataTable dataTable) {
        super();
        this.id = id;
        this.dataTable = dataTable;
    }

    /* (non-Javadoc)
	 * @see org.tdar.db.model.abstracts.ADR#getId()
	 */
    @Override
	public Long getId() {
        return id;
    }

    /* (non-Javadoc)
	 * @see org.tdar.db.model.abstracts.ADR#getTableName()
	 */
    @Override
	public String getTableName() {
        return dataTable.getName();
    }

    /* (non-Javadoc)
	 * @see org.tdar.db.model.abstracts.ADR#getDataTable()
	 */
    @Override
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

    /* (non-Javadoc)
	 * @see org.tdar.db.model.abstracts.ADR#propertyNames()
	 */
    @Override
	public Set<String> propertyNames()
    {
        return values.keySet();
    }

    /* (non-Javadoc)
	 * @see org.tdar.db.model.abstracts.ADR#asMap()
	 */
    @Override
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
        DataRecord other = (DataRecord) obj;
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
