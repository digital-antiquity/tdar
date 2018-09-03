package org.tdar.core.exception;

import org.tdar.exception.I18nException;

/**
 * $Id$
 * 
 * 
 * @author Adam Brin
 * @version $Rev$
 */
public class FileUploadException extends I18nException {

    private static final long serialVersionUID = 383064692701743774L;
    private StatusCode code;

    public FileUploadException(String msg, Throwable e) {
        super(msg, e);
    }

    public FileUploadException(String msg) {
        super(msg);
    }

}
