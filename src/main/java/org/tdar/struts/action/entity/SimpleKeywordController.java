package org.tdar.struts.action.entity;

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

import com.opensymphony.xwork2.Preparable;
import com.opensymphony.xwork2.Validateable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/entity/keyword")
public class SimpleKeywordController extends AuthenticationAware.Base implements Preparable, Validateable {

    private static final long serialVersionUID = 5267144668224536569L;

    private Long id;
    private KeywordType keywordType;
    private Keyword keyword;

    @Autowired
    private transient GenericKeywordService genericKeywordService;

    @Action
    public String edit() {
        return SUCCESS;
    }

    @Action(interceptorRefs = { @InterceptorRef("unauthenticatedStack") })
    public String view() {
        return SUCCESS;
    }

    @Override
    public void prepare() throws Exception {
        if (Persistable.Base.isNotNullOrTransient(id)) {
            addActionError(getText("simpleKeywordEditAction.id_required"));
        }
        if (keywordType == null) {
            addActionError(getText("simpleKeywordEditAction.type_required"));
        }

        setKeyword(genericKeywordService.find(keywordType.getKeywordClass(), id));
    }

    public Keyword getKeyword() {
        return keyword;
    }

    public void setKeyword(Keyword keyword) {
        this.keyword = keyword;
    }
}
