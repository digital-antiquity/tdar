package org.tdar.utils.sensorydata.enums;

public enum ImageFields {
	IDENTIFIER_IMAGE_FILE_NAME("Identifier (Image File Name)"),
	TITLE__CAPTION("Title / Caption"),
	DESCRIPTION_OF_IMAGE("Description of Image"),
	CREATOR("Creator"),
	DATE("Date"),
	RIGHTS("Rights"),
	KEYWORDS("Keywords"),
	LOCATION("Location");
	
	private final String label;
	
	private ImageFields(String label) {
		this.label = label;
	}
	
}
