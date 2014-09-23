package org.tdar.core.dao.external.auth;

import java.util.HashSet;
import java.util.Set;

public enum InternalTdarRights {
    VIEW_AND_DOWNLOAD_CONFIDENTIAL_INFO(TdarGroup.TDAR_EDITOR),
    EDIT_PERSONAL_ENTITES(TdarGroup.TDAR_EDITOR),
    EDIT_INSTITUTIONAL_ENTITES(TdarGroup.TDAR_EDITOR),
    EDIT_ANY_RESOURCE(TdarGroup.TDAR_EDITOR),
    EDIT_RESOURCE_COLLECTIONS(TdarGroup.TDAR_EDITOR),
    DELETE_RESOURCES(TdarGroup.TDAR_EDITOR),

    SEARCH_FOR_DELETED_RECORDS(TdarGroup.TDAR_EDITOR),
    SEARCH_FOR_FLAGGED_RECORDS(TdarGroup.TDAR_EDITOR),
    SEARCH_FOR_DRAFT_RECORDS(TdarGroup.TDAR_EDITOR),
    SEARCH_FOR_DUPLICATE_RECORDS(TdarGroup.TDAR_EDITOR),

    EDIT_ANYTHING(TdarGroup.TDAR_EDITOR),
    VIEW_ANYTHING(TdarGroup.TDAR_EDITOR),
    REPROCESS_DERIVATIVES(TdarGroup.TDAR_EDITOR),
    VIEW_ADMIN_INFO(TdarGroup.TDAR_EDITOR),
    VIEW_BILLING_INFO(TdarGroup.TDAR_EDITOR),
    EDIT_BILLING_INFO(TdarGroup.TDAR_BILLING_MANAGER);

    private TdarGroup[] permittedGroups;

    private InternalTdarRights(TdarGroup... minimumGroupWithRights) {
        Set<TdarGroup> groups = new HashSet<>();
        for (TdarGroup group : minimumGroupWithRights) {
            groups.addAll(group.getGroupsWithGreaterPermissions());
        }
        this.setPermittedGroups(groups.toArray(new TdarGroup[0]));
    }

    public TdarGroup[] getPermittedGroups() {
        return permittedGroups;
    }

    private void setPermittedGroups(TdarGroup[] groups) {
        this.permittedGroups = groups;
    }

}