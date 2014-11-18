package org.tdar.struts.action.search;

import java.util.Arrays;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.keyword.Keyword;
import org.tdar.core.bean.keyword.KeywordType;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.service.BookmarkedResourceService;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.SearchService;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.search.query.SortOption;
import org.tdar.search.query.builder.ResourceQueryBuilder;
import org.tdar.search.query.part.FieldQueryPart;
import org.tdar.search.query.part.HydrateableKeywordQueryPart;

import com.google.common.base.Objects;
import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("default")
@Namespace("/browse")
public class BrowseKeywordController extends AbstractLookupController<Resource> implements Preparable {

    public static final String BAD_SLUG = "bad-slug";

    private static final long serialVersionUID = 5267144668224536569L;

    @Autowired
    private transient SearchService searchService;
    @Autowired
    private transient BookmarkedResourceService bookmarkedResourceService;
    @Autowired
    private transient GenericKeywordService genericKeywordService;

    private Long id;
    private KeywordType keywordType;
    private Keyword keyword;
    private String slug = "";
    private String suffix = "";

    private DisplayOrientation orientation = DisplayOrientation.LIST_FULL;
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
        if (Persistable.Base.isNullOrTransient(getId())) {
            addActionError(getText("simpleKeywordAction.id_required"));
        }
        if (getKeywordType() == null) {
            addActionError(getText("simpleKeywordAction.type_required"));
        }

        setKeyword(genericKeywordService.find(getKeywordType().getKeywordClass(), getId()));
    }

    @Action(value = "keywords",
            results={
            @Result(name=SUCCESS, type=FREEMARKER, location="keywords.ftl"),
            @Result(name=BAD_SLUG, type=REDIRECT, location="/${keywordType.urlNamespace}/${keyword.id}/${keyword.slug}${suffix}")
    })
    public String view() {
        if (Persistable.Base.isNotNullOrTransient(keyword)  && keyword.isDuplicate()) {
            setKeyword(genericKeywordService.findAuthority(keyword));
            return BAD_SLUG;
        }

        if (Persistable.Base.isNullOrTransient(keyword) || getKeyword().getStatus() != Status.ACTIVE && !isEditor()) {
            return NOT_FOUND;
        }
        
        if (!Objects.equal(keyword.getSlug(), slug)) {
            if (getStartRecord() != DEFAULT_START || getRecordsPerPage() != DEFAULT_RESULT_SIZE) {
                setSuffix(String.format("?startRecord=%s&recordsPerPage=%s", getStartRecord(), getRecordsPerPage()));
            }
            return BAD_SLUG;
        }
        
        setMode("KeywordBrowse");
        ResourceQueryBuilder rqb = new ResourceQueryBuilder();
        rqb.append(new HydrateableKeywordQueryPart<Keyword>(getKeywordType(), Arrays.asList(getKeyword())));
        rqb.append(new FieldQueryPart<Status>(QueryFieldNames.STATUS, Status.ACTIVE));
        if (keywordType == KeywordType.GEOGRAPHIC_KEYWORD) {
//            setOrientation(DisplayOrientation.MAP);
        }
        try {
            setSortField(SortOption.TITLE);
            searchService.handleSearch(rqb, this, this);
            bookmarkedResourceService.applyTransientBookmarked(getResults(), getAuthenticatedUser());
        } catch (Exception e) {
            addActionErrorWithException(getText("collectionController.error_searching_contents"), e);
        }
        return SUCCESS;
    }

    @Override
    public List getFacetFields() {
        return null;
    }

    public DisplayOrientation getOrientation() {
        return orientation;
    }

    public void setOrientation(DisplayOrientation orientation) {
        this.orientation = orientation;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

}
