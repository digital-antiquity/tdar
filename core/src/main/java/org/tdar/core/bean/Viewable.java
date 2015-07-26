package org.tdar.core.bean;

/**
 * Abstraction for Resources and objects that have permissions and may not be viewable... this allows us to hide them from the controller.
 * 
 * @author abrin
 * 
 */
public interface Viewable {

    boolean isViewable();

    void setViewable(boolean viewable);

}
