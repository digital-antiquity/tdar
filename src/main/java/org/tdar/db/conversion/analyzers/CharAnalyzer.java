package org.tdar.db.conversion.analyzers;

import org.tdar.core.bean.resource.dataTable.DataTableColumnType;

public class CharAnalyzer implements ColumnAnalyzer {
	private int len = 0;

	public DataTableColumnType getType() {
		return DataTableColumnType.VARCHAR;
	}

	public boolean analyze(String value) {
		if (value == null)
			return true;
		if ("".equals(value))
			return true;
		if (value.length() > len) {
			len = value.length();
		}
		return true;
	}

	public int getLength() {
		return len;
	}
}