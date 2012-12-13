package org.tdar.db.conversion.analyzers;

import org.tdar.core.bean.resource.datatable.DataTableColumnType;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

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
		if (value.matches("(.*)(#(REF|NUM|N/A|VALUE|NAME|DIV))(.*)")) {
		    throw new TdarRecoverableRuntimeException("data contains excel translation errors, please check for cells with #REF, #VALUE, #NUM or other Excel Errors");
		}
		if (value.length() > len) {
			len = value.length();
		}
		return true;
	}

	public int getLength() {
		return len;
	}
}