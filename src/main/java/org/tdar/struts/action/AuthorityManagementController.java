package org.tdar.struts.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.tdar.core.bean.DedupeableType;
import org.tdar.core.bean.entity.Dedupable;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.AuthorityManagementService;
import org.tdar.core.service.external.auth.TdarGroup;
import org.tdar.struts.RequiresTdarUserGroup;
import org.tdar.struts.WriteableSession;

import com.opensymphony.xwork2.Preparable;

@ParentPackage("secured")
@Namespace("/admin/authority-management")
@Component
@Scope("prototype")
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
public class AuthorityManagementController extends AuthenticationAware.Base implements Preparable{

    private static final long serialVersionUID = -264345419370974992L;
    private DedupeableType entityType;
    private List<Long> selectedDupeIds = new ArrayList<Long>();
    private Long authorityId;
    private List<Dedupable> selectedDuplicates = new ArrayList<Dedupable>();
    
    
    public static String ERROR_NOT_ENOUGH_DUPLICATES = "No Duplicates selected. Please select at least two duplicates.";
    public static String ERROR_NO_DUPLICATES = "Please select at least two duplicates.";
    public static String ERROR_NO_ENTITY_TYPE = "Select an entity type.";
    public static String ERROR_NO_AUTHORITY_RECORD = "Please select an authority record.";
    public static final String ERROR_TOO_MANY_PROTECTED_RECORDS = "Your selection contains too many protected records.  Protected records can serve as an authority record but cannot be deduped";
    public static final String ERROR_CANNOT_DEDUPE_PROTECTED_RECORDS = "At least one of your selected duplicates is a protected record.  Protected records can serve as an authority record but cannot be deduped";
    public static String FMT_TOO_MANY_DUPLICATES = "You may only select up to %s duplicates.";
    
    
    
    @Autowired
    private AuthorityManagementService authorityManagementService;

    @Override
    public void validate() {
        if(entityType == null) {
            addActionError(ERROR_NO_ENTITY_TYPE);
        }
        if(CollectionUtils.isEmpty(selectedDupeIds)) {
            addActionError(ERROR_NO_DUPLICATES);
        } else if(selectedDupeIds.size() < 2) {
            addActionError(ERROR_NOT_ENOUGH_DUPLICATES);
        } else if (selectedDupeIds.size() > getDupeListMaxSize()) {
            String message =  String.format(FMT_TOO_MANY_DUPLICATES, getDupeListMaxSize());
            addActionError(message);
        }
    }
    
    @Override
    public void prepare() {
        inflateSelectedDupes();
    }
    
    
    @Action(value="index")
    @SkipValidation
    public String view() {
        return SUCCESS;
    }
    
    
    /**
     * Show the user all of the entities that will be effected as a result of merging the selected duplicates.
     * @return
     */
    @Action(value="select-authority", results={ @Result(name=SUCCESS, location="select-authority.ftl"), @Result(name=INPUT, location="index.ftl")})
    public String selectAuthority() {
        if(hasActionErrors()) return INPUT;

        if(authorityManagementService.countProtectedRecords(selectedDuplicates) > 1) {
            addActionError(ERROR_TOO_MANY_PROTECTED_RECORDS);
            return INPUT;
        }
        
        return SUCCESS;
    }

    /**
     * 
     */
    private void inflateSelectedDupes() {
        //populate the list of selected items
        for(Long id : selectedDupeIds) {
            Dedupable p = (Dedupable)getGenericService().find(entityType.getType(), id);
            selectedDuplicates.add(p);
        }
    }
    
    
    @Action(value="merge-duplicates", results={@Result(name=SUCCESS, location="success.ftl"), @Result(name=INPUT, location="select-authority.ftl")})
    @WriteableSession
    public String mergeDuplicates() {
        if(authorityId == null) {
            addActionError(ERROR_NO_AUTHORITY_RECORD);
            return INPUT;
        }
        
        //remove the authority record from the selected items
        Dedupable authority = (Dedupable)getGenericService().find(entityType.getType(), authorityId);
        selectedDuplicates.remove(authority);
        selectedDupeIds.remove(authorityId);
        
        if(authorityManagementService.countProtectedRecords(selectedDuplicates) > 0) {
            addActionError(ERROR_CANNOT_DEDUPE_PROTECTED_RECORDS);
            selectedDuplicates.add(authority);
            return INPUT;
        }
        
        //so now we should have everything we need to pass to the service
        try {
            authorityManagementService.updateReferrers(entityType.getType(), selectedDupeIds, authorityId);
        } catch(TdarRecoverableRuntimeException trex) {
            addActionErrorWithException("Could not de-dupe",trex);
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

    public List<Long> getSelectedDupeIds() {
        return selectedDupeIds;
    }

    public void setSelectedDupeIds(List<Long> selectedIds) {
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


    public List<Dedupable> getSelectedDuplicates() {
        return selectedDuplicates;
    }

    public int getDupeListMaxSize() {
        return TdarConfiguration.getInstance().getAuthorityManagementDupeListMaxSize();
    }

    
    
}
