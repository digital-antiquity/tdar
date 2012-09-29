package org.tdar.core.bean.coverage;

public enum CoverageType {

    CALENDAR_DATE("Calendar Date", "<B>Calendar dates:</B> %d to %d"),
    RADIOCARBON_DATE("Radiocarbon Date", "<B>Radiocarbon date in BP years:</B> %d to %d"),
    NONE("None","");

    private String label;
    private String format;

    CoverageType(String label, String format) {
        setLabel(label);
        setFormat(format);
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean validate(Integer start, Integer end) {
        if (start == null && end == null)
            return false;

        switch (this) {
            case CALENDAR_DATE:
                return (start <= end);
            case RADIOCARBON_DATE:
                return (start >= end && start > 0 && end > 0);
        }
        return false;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

}
