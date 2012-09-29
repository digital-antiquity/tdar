package org.tdar.core.parser;

import org.tdar.core.exception.TdarRecoverableRuntimeException;

/**
 * $Id$
 * 
 * RuntimeException for any coding sheet parse exceptions (should eventually contain error conditions to
 * distinguish between malformed coding sheets and IO Exceptions).
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class CodingSheetParserException extends TdarRecoverableRuntimeException {
    
    private static final long serialVersionUID = 6246686753761896569L;

    public CodingSheetParserException() {
        super();
    }
    
    public CodingSheetParserException(String message) {
        super(message);
    }
    
    public CodingSheetParserException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public CodingSheetParserException(Throwable cause) {
        super(cause);
    }

}
