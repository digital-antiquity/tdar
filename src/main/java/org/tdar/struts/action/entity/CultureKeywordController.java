package org.tdar.struts.action.entity;

import java.util.List;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.struts.data.KeywordNode;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/entity/culture-keyword")
public class CultureKeywordController extends AbstractKeywordController<CultureKeyword> {

    private static final long serialVersionUID = -5919100530284442396L;
    private KeywordNode<CultureKeyword> potentialParents;

    @Override
    public Class<CultureKeyword> getPersistableClass() {
        return CultureKeyword.class;
    }

    public KeywordNode<CultureKeyword> getPotentialParents() {
        if (potentialParents == null) {
            List<CultureKeyword> keywordsToOrganize = getGenericKeywordService().findAllApproved(CultureKeyword.class);
            potentialParents = KeywordNode.organizeKeywords(keywordsToOrganize);
        }
        return potentialParents;
    }
}
