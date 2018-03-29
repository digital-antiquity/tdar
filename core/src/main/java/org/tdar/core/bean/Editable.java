package org.tdar.core.bean;

/**
 * Abstraction for Resources and objects that have permissions and may not be editable... this allows us to hide them from the controller.
 * 
 * @author abrin
 * 
 */
public interface Editable extends Persistable {

    boolean isEditable();

    void setEditable(boolean editable);

}
