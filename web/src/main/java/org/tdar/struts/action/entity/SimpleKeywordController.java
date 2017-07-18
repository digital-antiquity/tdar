package org.tdar.struts.action.entity;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.RelationType;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.keyword.ExternalKeywordMapping;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;
import org.tdar.utils.PersistableUtils;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/entity/keyword")
public class SimpleKeywordController extends AbstractKeywordController {

    private static final long serialVersionUID = -6454678744854024278L;
    private String label;
    private String description;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    
    @Autowired
    private transient GenericKeywordService genericKeywordService;


    @Action("edit")
    @HttpsOnly
    @SkipValidation
    @RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
    public String edit() {
        getMappings().addAll(getKeyword().getAssertions());
        return SUCCESS;
    }
    
    @Action(value = "save", interceptorRefs = { @InterceptorRef("editAuthenticatedStack") }, results = {
            @Result(name = INPUT, location = "edit.ftl"),
            @Result(name = SUCCESS, type = TDAR_REDIRECT, location = "${keyword.detailUrl}")
    })
    @PostOnly
    @HttpsOnly
    @WriteableSession
    @RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
    public String save() {
        genericKeywordService.saveKeyword(label, description, getKeyword(), getMappings());
        return SUCCESS;
    }

    @Override
    public void validate() {
        super.validate();
        logger.debug("{}", getMappings());
        if (StringUtils.isBlank(label)) {
            addActionError(getText("simpleKeywordSaveAction.label_missing"));
        }
        Keyword byLabel = genericKeywordService.findByLabel(getKeywordType().getKeywordClass(), label);
        if (PersistableUtils.isNotNullOrTransient(byLabel) && !Objects.equals(getKeyword(), byLabel)) {
            addActionError(getText("simpleKeywordAction.label_duplicate"));
        }
        
        for (ExternalKeywordMapping map : getMappings()) {
            if (map == null) {
                continue;
            }
            if (StringUtils.isNotBlank(map.getRelation()) && map.getRelationType() == null) {
                addActionError(getText("simpleKeywordAction.relation_missing"));                
            }
            if (StringUtils.isBlank(map.getRelation()) && map.getRelationType() != null) {
                addActionError(getText("simpleKeywordAction.url_missing"));                
            }
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    
    public ExternalKeywordMapping getBlankMapping() {
        return new ExternalKeywordMapping();
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    
    public List<RelationType> getRelationTypes() {
        return Arrays.asList(RelationType.values());
    }
}
