/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.filestore;

/**
 * @author Adam Brin
 * 
 */
public class TaintedFileException extends Exception {

    /**
     * @param string
     */
    public TaintedFileException(String message) {
        super(message);
    }

    /**
     *
     */
    private static final long serialVersionUID = 2614025298404673325L;

}
