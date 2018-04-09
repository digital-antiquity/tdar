package org.tdar.core.exception;

import org.tdar.core.dao.AccountAdditionStatus;

/**
 * $Id$
 * 
 * 
 * @author Adam Brin
 * @version $Rev$
 */
public class TdarQuotaException extends TdarRecoverableRuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 8148196340196623521L;
    private AccountAdditionStatus code;

    public TdarQuotaException(String msg, Throwable e, AccountAdditionStatus code) {
        super(msg, e);
        this.setCode(code);
    }

    public TdarQuotaException(String msg, AccountAdditionStatus status) {
        super(msg);
        this.setCode(status);
    }

    public void setCode(AccountAdditionStatus code) {
        this.code = code;
    }

    public AccountAdditionStatus getCode() {
        return code;
    }
}
