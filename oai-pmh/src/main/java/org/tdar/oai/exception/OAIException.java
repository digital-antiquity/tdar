package org.tdar.oai.exception;

import org.tdar.oai.bean.generated.oai._2_0.OAIPMHerrorcodeType;


/**
 * $Id$
 * 
 * 
 * @author Adam Brin
 * @version $Rev$
 */
public class OAIException extends Exception {

    private static final long serialVersionUID = -5909815912933964223L;

    private OAIPMHerrorcodeType code = null;

    public OAIException(String msg, Throwable e, OAIPMHerrorcodeType code) {
        super(msg, e);
        this.setCode(code);
    }

    public OAIException(String msg, Throwable e) {
        super(msg, e);
    }

    public OAIException(String msg, OAIPMHerrorcodeType code) {
        super(msg);
        this.setCode(code);
    }

    public void setCode(OAIPMHerrorcodeType code) {
        this.code = code;
    }

    public OAIPMHerrorcodeType getCode() {
        return code;
    }
}
