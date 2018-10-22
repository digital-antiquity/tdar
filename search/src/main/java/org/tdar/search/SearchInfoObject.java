package org.tdar.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.bean.resource.datatable.DataTableColumn;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonAutoDetect(getterVisibility = Visibility.PUBLIC_ONLY)
@JsonInclude(Include.NON_NULL)
public class SearchInfoObject implements Serializable {

    private static final long serialVersionUID = -2521093985799356344L;

    private List<Status> availableStatuses = new ArrayList<>();
    private List<SiteTypeKeyword> siteTypes = new ArrayList<>();
    private List<InvestigationType> investigationTypes = new ArrayList<>();
    private List<CultureKeyword> cultureKeywords = new ArrayList<>();
    private List<MaterialKeyword> materialTypes = new ArrayList<>();
    private Map<Long, Set<DataTableColumn>> columnMap = new HashMap<>();

    public List<Status> getAvailableStatuses() {
        return availableStatuses;
    }

    public void setAvailableStatuses(List<Status> availableStatuses) {
        this.availableStatuses = availableStatuses;
    }

    public List<SiteTypeKeyword> getSiteTypes() {
        return siteTypes;
    }

    public void setSiteTypes(List<SiteTypeKeyword> siteTypes) {
        this.siteTypes = siteTypes;
    }

    public List<InvestigationType> getInvestigationTypes() {
        return investigationTypes;
    }

    public void setInvestigationTypes(List<InvestigationType> investigationTypes) {
        this.investigationTypes = investigationTypes;
    }

    public Map<Long, Set<DataTableColumn>> getColumnMap() {
        return columnMap;
    }

    public void setColumnMap(Map<Long, Set<DataTableColumn>> map) {
        this.columnMap = map;
    }

    public List<MaterialKeyword> getMaterialTypes() {
        return materialTypes;
    }

    public void setMaterialTypes(List<MaterialKeyword> materialTypes) {
        this.materialTypes = materialTypes;
    }

    public List<CultureKeyword> getCultureKeywords() {
        return cultureKeywords;
    }

    public void setCultureKeywords(List<CultureKeyword> cultureKeywords) {
        this.cultureKeywords = cultureKeywords;
    }

}
