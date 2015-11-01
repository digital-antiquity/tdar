package org.tdar.oai.exception;

public enum OaiErrorCode {
    BAD_ARGUMENT("badArgument"),
    BAD_RESUMPTION_TOKEN("badResumptionToken"),
    BAD_VERB("badVerb"),
    CANNOT_DISSEMINATE_FORMAT("cannotDisseminateFormat"),
    ID_DOES_NOT_EXIST("idDoesNotExist"),
    NO_RECORDS_MATCH("noRecordsMatch"),
    NO_METADATA_FORMATS("noMetadataFormats"),
    NO_SET_HIERARCHY("noSetHierarchy");

    private String code;
    private transient String message;

    private OaiErrorCode(String code) {
        this.setCode(code);
        this.setMessage(code);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
