package org.tdar.core.dao.external.auth;

public enum InternalTdarRights {
    VIEW_AND_DOWNLOAD_CONFIDENTIAL_INFO(TdarGroup.TDAR_ADMIN, TdarGroup.TDAR_EDITOR),
    EDIT_PERSONAL_ENTITES(TdarGroup.TDAR_ADMIN, TdarGroup.TDAR_EDITOR),
    EDIT_INSTITUTIONAL_ENTITES(TdarGroup.TDAR_ADMIN, TdarGroup.TDAR_EDITOR),
    EDIT_ANY_RESOURCE(TdarGroup.TDAR_ADMIN, TdarGroup.TDAR_EDITOR),
    EDIT_RESOURCE_COLLECTIONS(TdarGroup.TDAR_ADMIN, TdarGroup.TDAR_EDITOR),
    DELETE_RESOURCES(TdarGroup.TDAR_ADMIN, TdarGroup.TDAR_EDITOR),

    SEARCH_FOR_DELETED_RECORDS(TdarGroup.TDAR_EDITOR, TdarGroup.TDAR_ADMIN),
    SEARCH_FOR_FLAGGED_RECORDS(TdarGroup.TDAR_EDITOR, TdarGroup.TDAR_ADMIN, TdarGroup.TDAR_BILLING_MANAGER),
    SEARCH_FOR_DRAFT_RECORDS(TdarGroup.TDAR_EDITOR, TdarGroup.TDAR_ADMIN, TdarGroup.TDAR_BILLING_MANAGER),
    SEARCH_FOR_DUPLICATE_RECORDS(TdarGroup.TDAR_EDITOR, TdarGroup.TDAR_ADMIN),

    EDIT_ANYTHING(TdarGroup.TDAR_EDITOR, TdarGroup.TDAR_ADMIN),
    VIEW_ANYTHING(TdarGroup.TDAR_EDITOR, TdarGroup.TDAR_ADMIN),
    REPROCESS_DERIVATIVES(TdarGroup.TDAR_EDITOR, TdarGroup.TDAR_ADMIN),
    VIEW_ADMIN_INFO(TdarGroup.TDAR_ADMIN, TdarGroup.TDAR_EDITOR),
    VIEW_BILLING_INFO(TdarGroup.TDAR_ADMIN, TdarGroup.TDAR_BILLING_MANAGER, TdarGroup.TDAR_EDITOR),
    EDIT_BILLING_INFO(TdarGroup.TDAR_ADMIN, TdarGroup.TDAR_BILLING_MANAGER);

    private TdarGroup[] permittedGroups;

    private InternalTdarRights(TdarGroup... groups) {
        this.setPermittedGroups(groups);
    }

    public TdarGroup[] getPermittedGroups() {
        return permittedGroups;
    }

    public void setPermittedGroups(TdarGroup[] groups) {
        this.permittedGroups = groups;
    }

}