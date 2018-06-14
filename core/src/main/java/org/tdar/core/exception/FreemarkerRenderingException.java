package org.tdar.core.exception;

import org.tdar.exception.TdarRecoverableRuntimeException;

public class FreemarkerRenderingException extends TdarRecoverableRuntimeException {

    public FreemarkerRenderingException(Exception e) {
        super(e);
    }

    private static final long serialVersionUID = -4517697851838436415L;

}
