package org.tdar.struts.action.entity;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.dao.external.auth.TdarGroup;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.PostOnly;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/entity/keyword")
public class SimpleKeywordSaveAction extends AbstractKeywordController {

    private static final long serialVersionUID = 5267144668224536569L;

    private String label;
    private String description;

    @Autowired
    private transient GenericKeywordService genericKeywordService;

    @Action(value = "save", interceptorRefs = { @InterceptorRef("editAuthenticatedStack") }, results = {
            @Result(name = INPUT, location = "edit.ftl"),
            @Result(name = SUCCESS, type = REDIRECT, location = "view?id=${id}&keywordType=${keywordType}")
    })
    @PostOnly
    @HttpsOnly
    @RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
    public String save() {
        genericKeywordService.saveKeyword(label, description, getKeyword());
        return SUCCESS;
    }

    @Override
    public void validate() {
        super.validate();
        if (StringUtils.isBlank(label)) {
            addActionError(getText("simpleKeywordSaveAction.label_missing"));
        }
        Keyword byLabel = genericKeywordService.findByLabel(getKeywordType().getKeywordClass(), label);
        if (Persistable.Base.isNotNullOrTransient(byLabel) && !Objects.equals(getKeyword(), byLabel)) {
            addActionError(getText("simpleKeywordAction.label_duplicate"));
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
