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

    static final String HOME = "/";
    static final String DASHBOARD = "/dashboard";
    static final String WORKSPACE = "/workspace/list";
    static final String ADMIN = "/admin/internal";
    static final String PAGE_NOT_FOUND = "/page-not-found";
    static final String BOOKMARKS = DASHBOARD + "#bookmarks";
    static final String ENTITY_NAMESPACE = "browse/creators";
    static final String CART_ADD = "/cart/add";
    static final String COLUMNS_RESOURCE_ID = "columns?id=${resource.id}";
    static final String VIEW_RESOURCE_ID = "view?id=${resource.id}";
}
