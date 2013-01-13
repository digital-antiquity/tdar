package org.tdar.struts.action.entity;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.keyword.HierarchicalKeyword;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.SuggestedKeyword;
import org.tdar.struts.action.AbstractPersistableController;
import org.tdar.struts.action.TdarActionException;

//FIXME: There be dragons here!
/**
 * Warning!! The "keyword editing" functionality is incomplete, untested, and generally ill-conceived. It should not be used. Or looked at.
 * However, it remains here because it is inaccessible to the end-user.
 * 
 * Prolonged exposure may induce seizures. LOOK AWAY.
 * 
 */
public abstract class AbstractKeywordController<K extends Keyword> extends AbstractPersistableController<K> {

    private static final long serialVersionUID = 3138828456624831070L;

    @SkipValidation
    @Action(value = "view", interceptorRefs = { @InterceptorRef("unauthenticatedStack") }, results = {
            @Result(name = SUCCESS, location = "/WEB-INF/content/entity/keyword/view.ftl")
    })
    public String view() throws TdarActionException {
        return super.view();
    }

    @SkipValidation
    @Action(value = "edit", results = {
            @Result(name = SUCCESS, location = "/WEB-INF/content/entity/keyword/edit.ftl")
    })
    public String edit() throws TdarActionException {
        return super.edit();
    }

    @Override
    protected String save(K persistable) {
        // since we're not handling new keywords, we don't have to explicitly save
        return SUCCESS;
    }

    @Override
    protected void delete(K persistable) {
        // no deleting of keywords (for now)
        addActionError("Sorry, deleting keywords is not supported");
    }

    @Override
    public String loadViewMetadata() {
        // lets avoid the implications of creating a new keyword (for now)
        if (Persistable.Base.isNullOrTransient(getPersistable()))
            return REDIRECT_HOME;
        return SUCCESS;
    }

    public boolean isSuggestedKeyword() {
        return getPersistable() instanceof SuggestedKeyword;
    }

    public boolean isHierarchicalKeyword() {
        return getPersistable() instanceof HierarchicalKeyword;
    }

    public K getKeyword() {
        return getPersistable();
    }

}
