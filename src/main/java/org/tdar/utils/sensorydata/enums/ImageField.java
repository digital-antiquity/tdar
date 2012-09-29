package org.tdar.utils.sensorydata.enums;

import java.util.HashMap;
import java.util.Map;

public enum ImageField {
	IDENTIFIER_IMAGE_FILE_NAME("Identifier (Image File Name)"),
	TITLE__CAPTION("Title / Caption"),
	DESCRIPTION_OF_IMAGE("Description of Image"),
	CREATOR("Creator"),
	DATE("Date"),
	RIGHTS("Rights"),
	KEYWORDS("Keywords"),
	LOCATION("Location");
	
	private final String label;
	
    private static Map<String, ImageField> labelsToEnums;
    static {
        labelsToEnums = new HashMap<String, ImageField>();
        for(ImageField field : ImageField.values()) {
            labelsToEnums.put(field.label, field);
        }
    }

	private ImageField(String label) {
		this.label = label;
	}
	
	public static ImageField fromLabel(String label) {
		return labelsToEnums.get(label);
	}
	
}
