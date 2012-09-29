package org.tdar.search.query;

public class FieldQueryPart implements QueryPart {

	private String fieldName;
	private String fieldValue;
	private boolean inverse;
	
	public FieldQueryPart() {}
	
	public FieldQueryPart(String fieldName, String fieldValue) {
		this.fieldName = fieldName;
		this.fieldValue = fieldValue;
	}
	
	@Override
	public String generateQueryString() {
		return getInverse() + getFieldName() + ":(" + getFieldValue() + ")";
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldValue(String fieldValue) {
		this.fieldValue = fieldValue;
	}

	public String getFieldValue() {
		return fieldValue;
	}

	/**
	 * @param inverse the inverse to set
	 */
	public void setInverse(boolean inverse) {
		this.inverse = inverse;
	}

	private String getInverse() {
		if (isInverse()) return " NOT ";
		return "";
	}
	/**
	 * @return the inverse
	 */
	public boolean isInverse() {
		return inverse;
	}

}
