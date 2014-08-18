package org.tdar.struts.action.entity;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.KeywordType;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.struts.action.AuthenticationAware;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts.interceptor.annotation.PostOnly;

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/entity/keyword")
public class SimpleKeywordSaveAction extends AuthenticationAware.Base implements Preparable, Validateable {

    private static final long serialVersionUID = 5267144668224536569L;

    private Long id;
    private String label;
    private String description;
    private KeywordType keywordType;
    private Keyword keyword;

    @Autowired
    private transient GenericKeywordService genericKeywordService;

    @Action(interceptorRefs = { @InterceptorRef("editAuthenticatedStack") })
    @PostOnly
    @HttpsOnly
    // @WriteableSession
    public String save() {
        genericKeywordService.saveKeyword(label, description, keyword);
        return SUCCESS;
    }

    @Override
    public void validate() {
        super.validate();
        if (StringUtils.isBlank(label)) {
            addActionError(getText("simpleKeywordSaveAction.label_missing"));
        }
        Keyword byLabel = genericKeywordService.findByLabel(keywordType.getKeywordClass(), label);
        if (Persistable.Base.isNotNullOrTransient(byLabel) && !Objects.equals(keyword, byLabel)) {
            addActionError(getText("simpleKeywordAction.label_duplicate"));
        }
    }

    @Override
    public void prepare() throws Exception {
        if (Persistable.Base.isNotNullOrTransient(id)) {
            addActionError(getText("simpleKeywordAction.id_required"));
        }
        if (keywordType == null) {
            addActionError(getText("simpleKeywordAction.type_required"));
        }

        keyword = genericKeywordService.find(keywordType.getKeywordClass(), id);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public KeywordType getKeywordType() {
        return keywordType;
    }

    public void setKeywordType(KeywordType keywordType) {
        this.keywordType = keywordType;
    }

}
