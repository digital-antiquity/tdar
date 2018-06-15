package org.tdar;

/**
 * @author Adam Brin
 * 
 */
public interface UrlConstants {

    String HOME = "/";
    String DASHBOARD = "/dashboard";
    String WORKSPACE = "/workspace/list";
    String ADMIN = "/admin/internal";
    String PAGE_NOT_FOUND = "/page-not-found";
    // String BOOKMARKS = DASHBOARD + "#bookmarks";
    String ENTITY_NAMESPACE = "browse/creators";
    String COLUMNS_RESOURCE_ID = "${resource.id}?startRecord=${startRecord}&recordsPerPage=${recordsPerPage}";
    String VIEW_RESOURCE_ID = "${resource.id}";
    String VIEW_RESOURCE_ID_AS_ID = "${resource.detailUrl}";
    String MY_PROFILE = "/entity/user/myprofile";

    String CART_ADD = "/cart/add";
    String CART_REVIEW_PURCHASE = "/cart/review";
    String CART_REVIEW_UNAUTHENTICATED = "/cart/review-unauthenticated";
    String CART_PROCESS_PAYMENT_REQUEST = "/cart/process-payment-request";

    String TDAR_NAMESPACE_URL = "http://www.tdar.org/namespace";
    String TDAR_NAMESPACE_PREFIX = "tdar";
    String BOOKMARKS = "/dashboard/bookmarks";

    String SEARCH = "/search";

}
