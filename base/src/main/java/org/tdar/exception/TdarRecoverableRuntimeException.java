package org.tdar.exception;

import java.util.List;

/**
 * $Id$
 * 
 * RuntimeException signifying that something bad happened but we (think)
 * we can recover from it. I.e., bad user input or an error in the processing
 * or parsing of an uploaded data file, coding sheet, etc.
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public class TdarRecoverableRuntimeException extends I18nRuntimeException {

    private static final long serialVersionUID = 6246686753761896569L;

    private String moreInfoUrlKey;

    public TdarRecoverableRuntimeException() {
        super();
    }

    public TdarRecoverableRuntimeException(String message) {
        super(message);
    }

    public TdarRecoverableRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public TdarRecoverableRuntimeException(String message, List<?> values) {
        super(message, values);
    }

    public TdarRecoverableRuntimeException(String message, Throwable cause, List<?> values) {
        super(message, cause, values);
    }

    public TdarRecoverableRuntimeException(Throwable cause) {
        super(cause.getLocalizedMessage(), cause);
    }

    public TdarRecoverableRuntimeException(String message, String moreInfoUrlKey, List<?> values) {
        this(message, values);
        this.moreInfoUrlKey = moreInfoUrlKey;
    }

    public String getMoreInfoUrlKey() {
        return moreInfoUrlKey;
    }

    public void setMoreInfoUrlKey(String moreInfoUrlKey) {
        this.moreInfoUrlKey = moreInfoUrlKey;
    }

}
