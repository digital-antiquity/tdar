package org.tdar.core.exception;

/**
 * $Id$
 * 
 * RuntimeException for unrecoverable errors within tdar.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class TdarRuntimeException extends RuntimeException {
    
    private static final long serialVersionUID = 6246686753761896569L;

    public TdarRuntimeException() {
        super();
    }
    
    public TdarRuntimeException(String message) {
        super(message);
    }
    
    public TdarRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public TdarRuntimeException(Throwable cause) {
        super(cause);
    }

}
