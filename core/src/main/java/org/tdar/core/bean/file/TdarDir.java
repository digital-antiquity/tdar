package org.tdar.core.bean.file;

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.entity.TdarUser;

@Entity
@DiscriminatorValue(value = "DIR")
public class TdarDir extends AbstractFile {

    private static final long serialVersionUID = 4135346326567855165L;
    public static final String UNFILED = "unfiled";

    public TdarDir() {}

    public TdarDir(TdarUser basicUser, BillingAccount act, String name) {
        setAccount(act);
        setFilename(name);
        setUploader(basicUser);
        setDateCreated(new Date());
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", getName(), getId());
    }

}
