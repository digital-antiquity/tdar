package org.tdar.core.bean.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tdar.core.bean.HasLabel;
import org.tdar.core.bean.Localizable;
import org.tdar.core.bean.entity.Creator.CreatorType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.utils.MessageHelper;

/**
 * $Id$
 * 
 * Enum for the possible roles a ResourceCreator can have and partitioned by CreatorType and ResourceType.
 * 
 * FIXME: refactor this for next release
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */

// FIXME: the logic of these roles, when they are relevant, when they should be accepted for input, and when they should be included for citation, is almost
// totally inscrutable
public enum ResourceCreatorRole implements HasLabel, Localizable {
    CONTACT("Contact", ResourceCreatorRoleType.CREDIT),
    AUTHOR("Author", ResourceCreatorRoleType.AUTHORSHIP, null, ResourceType.DOCUMENT),
    ANALYST("Analyst", ResourceCreatorRoleType.AUTHORSHIP, null, ResourceType.DATASET, ResourceType.SENSORY_DATA, ResourceType.GEOSPATIAL, ResourceType.IMAGE, ResourceType.ONTOLOGY,ResourceType.CODING_SHEET),
    CONTRIBUTOR(
            "Contributor", ResourceCreatorRoleType.CREDIT),
    EDITOR("Editor", ResourceCreatorRoleType.AUTHORSHIP, CreatorType.PERSON,
            ResourceType.DOCUMENT),
    TRANSLATOR("Translator", ResourceCreatorRoleType.CREDIT, CreatorType.PERSON, ResourceType.DOCUMENT),
    FIELD_DIRECTOR(
            "Field Director", ResourceCreatorRoleType.CREDIT,
            CreatorType.PERSON),
    LAB_DIRECTOR("Lab Director", ResourceCreatorRoleType.CREDIT, CreatorType.PERSON),
    PRINCIPAL_INVESTIGATOR(
            "Principal Investigator", ResourceCreatorRoleType.CREDIT, CreatorType.PERSON),
    PROJECT_DIRECTOR("Project Director",
            ResourceCreatorRoleType.CREDIT, CreatorType.PERSON),
    COLLABORATOR("Collaborator", ResourceCreatorRoleType.CREDIT,
            CreatorType.INSTITUTION),
    LANDOWNER("Landowner", ResourceCreatorRoleType.CREDIT),
    SPONSOR("Sponsor",
            ResourceCreatorRoleType.CREDIT),
    PERMITTER("Permitting Agency", ResourceCreatorRoleType.CREDIT,
            CreatorType.INSTITUTION),
    REPOSITORY("Repository", ResourceCreatorRoleType.CREDIT,
            CreatorType.INSTITUTION),
    CREATOR("Creator", ResourceCreatorRoleType.AUTHORSHIP,
            null, ResourceType.CODING_SHEET, ResourceType.ONTOLOGY, ResourceType.IMAGE,
            ResourceType.DATASET,
            ResourceType.SENSORY_DATA, ResourceType.ARCHIVE, ResourceType.GEOSPATIAL,
            ResourceType.VIDEO, ResourceType.AUDIO),
    PREPARER("Prepared By",
            ResourceCreatorRoleType.CREDIT,
            CreatorType.INSTITUTION),
    SUBMITTED_TO("Submitted To",
            ResourceCreatorRoleType.CREDIT,
            CreatorType.INSTITUTION),
    COPYRIGHT_HOLDER(
            "Copyright Holder", ResourceCreatorRoleType.OTHER,
            null, ResourceType.CODING_SHEET,
            ResourceType.ONTOLOGY, ResourceType.IMAGE,
            ResourceType.DATASET, ResourceType.ARCHIVE,
            ResourceType.GEOSPATIAL, ResourceType.VIDEO,
            ResourceType.SENSORY_DATA, ResourceType.DOCUMENT,
            ResourceType.AUDIO),
    SUBMITTER("Submitter",
            ResourceCreatorRoleType.OTHER,
            CreatorType.PERSON),
    UPDATER("Updater",
            ResourceCreatorRoleType.OTHER,
            CreatorType.PERSON),
    RESOURCE_PROVIDER(
            "Resource Provider",
            ResourceCreatorRoleType.OTHER,
            CreatorType.INSTITUTION),
    PUBLISHER(
            "Publisher",
            ResourceCreatorRoleType.OTHER),
    UPLOADER(
            "Uploader",
            ResourceCreatorRoleType.OTHER,
            CreatorType.PERSON);

    private final String label;
    private final CreatorType relevantCreatorType;
    private final Set<ResourceType> relevantResourceTypes;
    private ResourceCreatorRoleType type;

    /**
     * return a list of roles typically associated with the actual creation of a resource.
     * 
     * @return
     */
    public static List<ResourceCreatorRole> getAuthorshipRoles() {
        return getRolesOfType(ResourceCreatorRoleType.AUTHORSHIP);
    }

    /**
     * return a set containing the roles that should be included as a 'creator' when including citations in search results
     * 
     * @param resourceType
     * @return
     */
    // FIXME: can we use our internal data structure to populate this?
    public static Set<ResourceCreatorRole> getPrimaryCreatorRoles(ResourceType resourceType) {
        Map<ResourceType, List<ResourceCreatorRole>> map = new HashMap<>();
        for (ResourceType resourceType_ : ResourceType.values()) {
            map.put(resourceType_, Arrays.asList(CREATOR));
        }
        // overriding for and document
        map.put(ResourceType.DOCUMENT, Arrays.asList(AUTHOR));
        map.put(ResourceType.PROJECT, Arrays.asList(PRINCIPAL_INVESTIGATOR, PROJECT_DIRECTOR, SPONSOR));
        return new HashSet<>(map.get(resourceType));
    }

    public static Set<ResourceCreatorRole> getResourceCreatorRolesForProfilePage(CreatorType creatorType) {
        HashSet<ResourceCreatorRole> roles = new HashSet<ResourceCreatorRole>();
        roles.add(CREATOR);
        roles.add(EDITOR);
        roles.add(PRINCIPAL_INVESTIGATOR);
        roles.add(PROJECT_DIRECTOR);
        roles.add(PUBLISHER);
        roles.add(AUTHOR);
        if (creatorType == CreatorType.INSTITUTION) {
            roles.add(PREPARER);
            roles.add(SUBMITTED_TO);
            roles.add(SPONSOR);
            roles.add(REPOSITORY);
            roles.add(RESOURCE_PROVIDER);
        }
        return roles;
    }

    private ResourceCreatorRole(String label, ResourceCreatorRoleType type) {
        this(label, type, null, new ResourceType[0]);
    }

    private ResourceCreatorRole(String label, ResourceCreatorRoleType type, CreatorType forCreatorType) {
        // FIXME: since there isn't a ResourceType.ALL and I don't think it makes sense
        // to have one in the database, we're using null to signify ResourceType.ALL
        // perhaps it is better to consistent to use null for Creator.Type as well...
        this(label, type, forCreatorType, new ResourceType[0]);
    }

    private ResourceCreatorRole(String label, ResourceCreatorRoleType type, CreatorType creatorType, ResourceType... resourceTypes) {
        this.label = label;
        this.setType(type);
        this.relevantCreatorType = creatorType;
        if (resourceTypes.length == 0) {
            this.relevantResourceTypes = Collections.emptySet();
        } else {
            this.relevantResourceTypes = new HashSet<>(Arrays.asList(resourceTypes));
        }
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getLocaleKey() {
        return MessageHelper.formatLocalizableKey(this);
    }

    public boolean isRelevantFor(CreatorType creatorType) {
        return ((relevantCreatorType == null) || (creatorType == null) || (relevantCreatorType == creatorType));
    }

    public boolean isRelevantFor(ResourceType resourceType) {
        return (relevantResourceTypes.isEmpty() || relevantResourceTypes.contains(resourceType));
    }

    public boolean isRelevantFor(CreatorType creatorType, ResourceType resourceType) {
        return isRelevantFor(creatorType) && isRelevantFor(resourceType);
    }

    public static List<ResourceCreatorRole> getAll() {
        List<ResourceCreatorRole> relevantRoles = new ArrayList<>();
        relevantRoles.addAll(getAuthorshipRoles());
        relevantRoles.addAll(getCreditRoles());
        return relevantRoles;
    }

    public static List<ResourceCreatorRole> getPersonRoles() {
        return getRoles(CreatorType.PERSON);
    }

    public static List<ResourceCreatorRole> getInstitutionRoles() {
        return getRoles(CreatorType.INSTITUTION);
    }

    public static List<ResourceCreatorRole> getRoles(CreatorType creatorType) {
        ArrayList<ResourceCreatorRole> relevantRoles = new ArrayList<>();
        for (ResourceCreatorRole role : values()) {
            if (role.isRelevantFor(creatorType)) {
                relevantRoles.add(role);
            }
        }
        relevantRoles.removeAll(getOtherRoles());
        return relevantRoles;
    }

    public static List<ResourceCreatorRole> getRoles(CreatorType creatorType, ResourceType resourceType) {
        ArrayList<ResourceCreatorRole> resourceCreatorRoles = new ArrayList<>();
        for (ResourceCreatorRole role : values()) {
            if (role.isRelevantFor(creatorType, resourceType)) {
                resourceCreatorRoles.add(role);
            }
        }
        resourceCreatorRoles.removeAll(getOtherRoles());
        return resourceCreatorRoles;
    }

    public static List<ResourceCreatorRole> getCreditRoles() {
        return getRolesOfType(ResourceCreatorRoleType.CREDIT);
    }

    private static List<ResourceCreatorRole> getRolesOfType(ResourceCreatorRoleType type) {
        // get mutable list of all roles, then remove the authorship roles
        ArrayList<ResourceCreatorRole> roles = new ArrayList<>();
        for (ResourceCreatorRole role : values()) {
            if (role.getType() == type) {
                roles.add(role);
            }
        }
        return roles;
    }

    public static List<ResourceCreatorRole> getOtherRoles() {
        return getRolesOfType(ResourceCreatorRoleType.OTHER);
    }

    public static List<ResourceCreatorRole> getAuthorshipRoles(CreatorType creatorType, ResourceType resourceType) {
        HashSet<ResourceCreatorRole> s1 = new HashSet<>(getAuthorshipRoles());
        HashSet<ResourceCreatorRole> s2 = new HashSet<>(getRoles(creatorType, resourceType));
        s1.retainAll(s2);
        List<ResourceCreatorRole> sortedRoles = new ArrayList<>(s1);
        Collections.sort(sortedRoles);
        return sortedRoles;
    }

    public static List<ResourceCreatorRole> getCreditRoles(CreatorType creatorType, ResourceType resourceType) {
        HashSet<ResourceCreatorRole> s1 = new HashSet<>(getCreditRoles());
        HashSet<ResourceCreatorRole> s2 = new HashSet<>(getRoles(creatorType, resourceType));
        s1.retainAll(s2);
        List<ResourceCreatorRole> sortedRoles = new ArrayList<>(s1);
        Collections.sort(sortedRoles);
        return sortedRoles;
    }

    public ResourceCreatorRoleType getType() {
        return type;
    }

    private void setType(ResourceCreatorRoleType type) {
        this.type = type;
    }

    public String getSchemaOrgLabel() {
        switch (this) {
            case COPYRIGHT_HOLDER:
                return "copyrightHolder";
            default:
                return this.name().toLowerCase();
        }
    }

    public boolean isPartOfSchemaOrg() {
        switch (this) {
            case AUTHOR:
            case EDITOR:
            case CREATOR:
            case COPYRIGHT_HOLDER:
            case PUBLISHER:
                return true;
            default:
                return false;
        }
    }
}
