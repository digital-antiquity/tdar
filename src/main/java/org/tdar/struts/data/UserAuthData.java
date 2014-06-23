package org.tdar.struts.data;

import java.io.Serializable;
import java.util.List;

import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.exception.TdarRecoverableRuntimeException;

import com.opensymphony.xwork2.TextProvider;
import com.opensymphony.xwork2.util.logging.Logger;
import com.opensymphony.xwork2.util.logging.LoggerFactory;

public abstract class UserAuthData implements Serializable {

    private static final long serialVersionUID = 999236299756064301L;
    protected Logger logger = LoggerFactory.getLogger(getClass());
    private AntiSpamHelper h;
    private TdarUser person = new TdarUser();

    
    protected void checkForSpammers(TextProvider textProvider, List<String> errors) {
        // SPAM CHECKING
        // 1 - check for whether the "bogus" comment field has data
        // 2 - check whether someone is adding characters that should not be there
        // 3 - check for known spammer - fname == lname & phone = 123456
        try {
            getH().setPerson(getPerson());
            getH().checkForSpammers();
        } catch (TdarRecoverableRuntimeException tre) {
            errors.add(textProvider.getText(tre.getMessage()));
        }
    }

    protected Logger getLogger() {
        return logger;
    }

    public AntiSpamHelper getH() {
        return h;
    }

    public void setH(AntiSpamHelper h) {
        this.h = h;
    }

    public TdarUser getPerson() {
        return person;
    }

    public void setPerson(TdarUser person) {
        this.person = person;
    }
}
