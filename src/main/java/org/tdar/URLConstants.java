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

    final String HOME = "/";
    final String DASHBOARD = "/dashboard";
    final String WORKSPACE = "/workspace/list";
    final String ADMIN = "/admin/internal";
    final String PAGE_NOT_FOUND = "/page-not-found";
    final String BOOKMARKS = DASHBOARD + "#bookmarks";
    final String ENTITY_NAMESPACE = "browse/creators";
    final String CART_ADD = "/cart/add";
    final String COLUMNS_RESOURCE_ID = "columns?id=${resource.id}";
    final String VIEW_RESOURCE_ID = "view?id=${resource.id}";
}
