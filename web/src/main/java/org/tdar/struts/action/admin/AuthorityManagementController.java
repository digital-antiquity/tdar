package org.tdar.struts.action.admin;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.DedupeableType;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.entity.Dedupable;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.AuthorityManagementService;
import org.tdar.core.service.authority.DupeMode;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.struts.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Preparable;

@ParentPackage("secured")
@Namespace("/admin/authority-management")
@Component
@Scope("prototype")
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
public class AuthorityManagementController extends AbstractAuthenticatableAction implements Preparable {

    private static final long serialVersionUID = -264345419370974992L;
    private DedupeableType entityType;
    private Set<Long> selectedDupeIds = new HashSet<Long>();
    private Long authorityId;
    private Map<Long, Dedupable<?>> selectedDuplicates = new HashMap<Long, Dedupable<?>>();

    private DupeMode mode = DupeMode.MARK_DUPS_ONLY;

    @Autowired
    private AuthorityManagementService authorityManagementService;

    @Override
    public void validate() {
        if (entityType == null) {
            addActionError(getText("authorityManagementController.error_no_entity_type"));
        }
        if (CollectionUtils.isEmpty(selectedDupeIds)) {
            addActionError(getText("authorityManagementController.error_no_duplicates"));
        } else if (selectedDupeIds.size() < 2) {
            addActionError(getText("authorityManagementController.error_not_enough_duplicates"));
        } else if (selectedDupeIds.size() > getDupeListMaxSize()) {
            String message = getText("authorityManagementController.fmt_too_many_duplicates", getDupeListMaxSize());
            addActionError(message);
        }
    }

    @Override
    public void prepare() {
        inflateSelectedDupes();
    }

    @Action(value = "index")
    @SkipValidation
    public String view() {
        return SUCCESS;
    }

    /**
     * Show the user all of the entities that will be effected as a result of merging the selected duplicates.
     * 
     * @return
     */
    @Action(value = "select-authority", results = { @Result(name = SUCCESS, location = "select-authority.ftl"), @Result(name = INPUT, location = "index.ftl") })
    @PostOnly
    public String selectAuthority() {
        if (hasActionErrors()) {
            return INPUT;
        }
        if (authorityManagementService.countProtectedRecords(selectedDuplicates.values()) > 1) {
            addActionError(getText("authorityManagementController.error_too_many_protected_records"));
            return INPUT;
        }

        return SUCCESS;
    }

    /**
     * 
     */
    private void inflateSelectedDupes() {
        // populate the list of selected items
        for (Long id : selectedDupeIds) {
            Dedupable<?> p = getGenericService().find(entityType.getType(), id);
            selectedDuplicates.put(id, p);
        }
    }

    @Action(value = "merge-duplicates",
            results = { @Result(name = SUCCESS, location = "success.ftl"), @Result(name = INPUT, location = "select-authority.ftl") })
    @PostOnly
    @WriteableSession
    public String mergeDuplicates() {
        if (authorityId == null) {
            addActionError(getText("authorityManagementController.error_no_authority_record"));
            return INPUT;
        }

        // remove the authority record from the selected items
        Dedupable<?> authority = getGenericService().find(entityType.getType(), authorityId);
        selectedDuplicates.remove(authority.getId());
        selectedDupeIds.remove(authorityId);

        if (authorityManagementService.countProtectedRecords(selectedDuplicates.values()) > 0) {
            addActionError(getText("authorityManagementController.error_cannot_dedupe_protected_records"));
            selectedDuplicates.put(authority.getId(), authority);
            return INPUT;
        }

        // so now we should have everything we need to pass to the service
        try {
            authorityManagementService.updateReferrers(getAuthenticatedUser(), entityType.getType(), selectedDupeIds, authorityId, mode, true);
        } catch (Exception trex) {
            addActionErrorWithException(getText("authorityManagementController.could_not_dedup"), trex);
            return INPUT;
        }

        return SUCCESS;
    }

    public DedupeableType getEntityType() {
        return entityType;
    }

    public void setEntityType(DedupeableType entityType) {
        this.entityType = entityType;
    }

    public Set<Long> getSelectedDupeIds() {
        return selectedDupeIds;
    }

    public void setSelectedDupeIds(Set<Long> selectedIds) {
        this.selectedDupeIds = selectedIds;
    }

    public long getAuthorityId() {
        return authorityId;
    }

    public void setAuthorityId(long authorityId) {
        this.authorityId = authorityId;
    }

    public List<DedupeableType> getDedupeableTypes() {
        return Arrays.asList(DedupeableType.values());
    }

    public Collection<Dedupable<?>> getSelectedDupeValues() {
        return selectedDuplicates.values();
    }

    public int getDupeListMaxSize() {
        return TdarConfiguration.getInstance().getAuthorityManagementDupeListMaxSize();
    }

    public DupeMode getMode() {
        return mode;
    }

    public List<DupeMode> getAllDupModes() {
        return Arrays.asList(DupeMode.values());
    }

    public void setMode(DupeMode mode) {
        this.mode = mode;
    }

}
