package org.tdar.core.exception;

import org.tdar.struts.data.oai.OaiErrorCode;

/**
 * $Id$
 * 
 * 
 * @author Adam Brin
 * @version $Rev$
 */
public class OAIException extends Exception {

    private static final long serialVersionUID = -5909815912933964223L;

    private OaiErrorCode code = null;

    public OAIException(String msg, Throwable e, OaiErrorCode code) {
        super(msg, e);
        this.setCode(code);
    }

    public OAIException(String msg, Throwable e) {
        super(msg, e);
    }

    public OAIException(String msg, OaiErrorCode code) {
        super(msg);
        this.setCode(code);
    }

    public void setCode(OaiErrorCode code) {
        this.code = code;
    }

    public OaiErrorCode getCode() {
        return code;
    }
}
