package org.tdar.db.model.abstracts;

import java.util.Map;
import java.util.Set;

import org.tdar.core.bean.resource.datatable.DataTable;

public interface AbstractDataRecord {

	Long getId();

	String getTableName();

	DataTable getDataTable();

	Set<String> propertyNames();

	Map<?, ?> asMap();

	Object get(String name);

	void put(String name, Object value);

}