package org.tdar.parser;

import java.io.Serializable;

public class TCodingRule implements Serializable {

    private static final long serialVersionUID = 7111792302631217328L;

    private String code;
    private String term;
    private String description;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
