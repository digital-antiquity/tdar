package org.tdar.core.bean.file;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "DIR")
public class TdarDir extends AbstractFile {

    private static final long serialVersionUID = 4135346326567855165L;
    public static final String UNFILED = "unfiled";


}
