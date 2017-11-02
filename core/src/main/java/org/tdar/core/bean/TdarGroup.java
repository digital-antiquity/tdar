package org.tdar.core.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

public enum TdarGroup implements HasLabel {

    TDAR_ADMIN("tdar-admins", 1000),
    TDAR_BILLING_MANAGER("tdar-billing", 600),
    TDAR_EDITOR("tdar-editors", 500),
    TDAR_API_USER("tdar-api-users", 3),
    TDAR_USERS("tdar-users", 1),
    JIRA_USERS("jira-users", -1),
    CONFLUENCE_USERS("confluence-users", -1),
    UNAUTHORIZED("", -2), 
    TDAR_BALK("tdar-balk",2);

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

    public List<TdarGroup> getGroupsWithGreaterPermissions() {
        List<TdarGroup> toReturn = new ArrayList<>();
        for (TdarGroup group : TdarGroup.values()) {
            if (group.permissionLevel >= this.permissionLevel) {
                toReturn.add(group);
            }
        }
        return toReturn;
    }

    @Override
    public String toString() {
        return groupName;
    }

    public static TdarGroup fromString(String groupName) {
        if (groupName == null) {
            return UNAUTHORIZED;
        }
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

    @Override
    public String getLabel() {
        return groupName;
    }
}