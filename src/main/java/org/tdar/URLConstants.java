/**
 * $Id$
 * 
 * @author $Author$
 * @version $Revision$
 */
package org.tdar;

/**
 * @author Adam Brin
 * 
 */
public interface URLConstants {

    public static final String HOME = "/";
    public static final String DASHBOARD = "/dashboard";
    public static final String WORKSPACE = "/workspace/list";
    public static final String ADMIN = "/admin/internal";
    public static final String PAGE_NOT_FOUND = "/page-not-found";
    public static final String BOOKMARKS = DASHBOARD + "#bookmarks";
    public static final String ENTITY_NAMESPACE = "browse/creators";
    public static final String CART_ADD = "/cart/add";
    public static final String COLUMNS_RESOURCE_ID = "columns?id=${resource.id}";
    public static final String VIEW_RESOURCE_ID = "view?id=${resource.id}";
}
