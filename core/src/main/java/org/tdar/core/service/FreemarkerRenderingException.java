package org.tdar.core.service;

import org.tdar.core.exception.TdarRecoverableRuntimeException;

public class FreemarkerRenderingException extends TdarRecoverableRuntimeException {

    public FreemarkerRenderingException(Exception e) {
        super(e);
    }

    private static final long serialVersionUID = -4517697851838436415L;

}
