package org.tdar.core.exception;

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

}
