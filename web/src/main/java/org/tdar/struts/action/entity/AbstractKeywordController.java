package org.tdar.struts.action.entity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.keyword.ExternalKeywordMapping;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.KeywordType;
import org.tdar.core.service.GenericService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

public abstract class AbstractKeywordController extends AbstractAuthenticatableAction implements Preparable {

    private static final long serialVersionUID = -7469398370759336245L;

    @Autowired
    private transient GenericService genericService;

    private Long id;
    private KeywordType keywordType;
    private Keyword keyword;
    private List<ExternalKeywordMapping> mappings = new ArrayList<>();

    public Keyword getKeyword() {
        return keyword;
    }

    public void setKeyword(Keyword keyword) {
        this.keyword = keyword;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public KeywordType getKeywordType() {
        return keywordType;
    }

    public void setKeywordType(KeywordType keywordType) {
        this.keywordType = keywordType;
    }

    @Override
    public void prepare() throws Exception {
        if (PersistableUtils.isNullOrTransient(getId())) {
            addActionError(getText("simpleKeywordAction.id_required"));
        }
        if (getKeywordType() == null) {
            addActionError(getText("simpleKeywordAction.type_required"));
        }

        setKeyword(genericService.find(getKeywordType().getKeywordClass(), getId()));
    }

    public List<ExternalKeywordMapping> getMappings() {
        return mappings;
    }

    public void setMappings(List<ExternalKeywordMapping> mappings) {
        this.mappings = mappings;
    }

}
