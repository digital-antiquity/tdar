package org.tdar.core.bean.resource;

/**
 * We have a couple of cases where we utilize "Static" classes for unique or specific values -- DataTable, CodingRule for examples... This helps us identify
 * them in the DAO
 * 
 * @author abrin
 *
 */
public interface HasStatic {

    boolean isStatic();
}
