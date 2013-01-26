package org.tdar.db.conversion.analyzers;

import org.tdar.core.bean.resource.datatable.DataTableColumnType;

public class DoubleAnalyzer implements ColumnAnalyzer {

	public DataTableColumnType getType() {
		return DataTableColumnType.DOUBLE;
	}

	public boolean analyze(String value) {
		if (value == null)
			return true;
		if ("".equals(value))
			return true;
		try {
			Double.parseDouble(value);
		} catch (NumberFormatException nfx) {
			return false;
		}
		return true;
	}

	public int getLength() {
		return 0;
	}
}