package org.tdar.core.bean.resource;

import org.tdar.core.bean.Viewable;

public interface ConfidentialViewable extends Viewable {

    public boolean isConfidentialViewable();

    public void setConfidentialViewable(boolean editable);
}
