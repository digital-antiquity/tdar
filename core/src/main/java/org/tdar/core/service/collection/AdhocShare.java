package org.tdar.core.service.collection;

import java.io.Serializable;
import java.util.Date;

public class AdhocShare implements Serializable {

    private static final long serialVersionUID = 1871697669066300301L;
    private String email;
    private Long userId;
    
    private Long shareId;
    private Long accountId;
    
    private Date expires;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getShareId() {
        return shareId;
    }

    public void setShareId(Long shareId) {
        this.shareId = shareId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }
    
    
}
