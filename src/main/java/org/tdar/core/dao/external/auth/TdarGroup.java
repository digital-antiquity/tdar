package org.tdar.core.dao.external.auth;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.tdar.core.bean.HasLabel;

public enum TdarGroup implements Comparable<TdarGroup>, HasLabel {

    TDAR_ADMIN("tdar-admins", 1000),
    TDAR_BILLING_MANAGER("tdar-billing", 600),
    TDAR_EDITOR("tdar-editors", 500),
    TDAR_USERS("tdar-users", 1),
    JIRA_USERS("jira-users", -1),
    CONFLUENCE_USERS("confluence-users", -1),
    UNAUTHORIZED("", -2);

    private final String groupName;
    private final int permissionLevel;

    TdarGroup(String groupName, int permissionLevel) {
        this.groupName = groupName;
        this.permissionLevel = permissionLevel;
    }

    public boolean hasGreaterPermissions(TdarGroup group) {
        if (group == null) {
            return false;
        }
        return ObjectUtils.compare(permissionLevel, group.permissionLevel) >= 0;
    }

    public String getGroupName() {
        return groupName;
    }

    public String toString() {
        return groupName;
    }

    public static TdarGroup fromString(String groupName) {
        for (TdarGroup group : values()) {
            if (group.getGroupName().equalsIgnoreCase(groupName.trim())) {
                return group;
            }
        }
        return TdarGroup.UNAUTHORIZED;
    }

    public static List<TdarGroup> getUserGroups() {
        return Arrays.asList(TDAR_USERS, JIRA_USERS, CONFLUENCE_USERS);
    }

    public String getLabel() {
        return groupName;
    }
}