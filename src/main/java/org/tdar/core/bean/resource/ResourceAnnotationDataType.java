package org.tdar.core.bean.resource;

public enum ResourceAnnotationDataType {
	
	NUMERIC,
	STRING,
	FORMAT_STRING;
	
	public boolean isFormatString() {
		return this == FORMAT_STRING;
	}
}
