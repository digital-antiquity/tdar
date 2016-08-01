package org.tdar.struts.action.api.lod;

import java.io.ByteArrayInputStream;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.KeywordType;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.GenericService;
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.struts.interceptor.annotation.HttpForbiddenErrorResponseOnly;

import com.opensymphony.xwork2.Preparable;

@Namespace("/api/lod/keyword")
@Component
@Scope("prototype")
@ParentPackage("default")
@HttpForbiddenErrorResponseOnly
public class KeywordLinkedOpenDataAction extends AbstractJsonApiAction implements Preparable  {

    private static final long serialVersionUID = -3861643422732697451L;
    private Long id;
    private KeywordType type;
    @Autowired
    private GenericService genericService;
    @Autowired
    private GenericKeywordService keywordService;
    
    @Override
    public void prepare() throws Exception {
        if (type == null) {
            addActionError("error.object_does_not_exist");
        }
        Keyword resource = genericService.find(type.getKeywordClass(), id);
        
        String message = keywordService.getSchemaOrgJsonLD(resource);
        setJsonInputStream(new ByteArrayInputStream(message.getBytes()));
    }

    @Action(value="{id}")
    @Override
    public String execute() throws Exception {
        return super.execute();
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public KeywordType getType() {
        return type;
    }

    public void setType(KeywordType type) {
        this.type = type;
    }

}
