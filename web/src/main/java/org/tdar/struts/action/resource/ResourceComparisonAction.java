package org.tdar.struts.action.resource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.SetUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.keyword.CultureKeyword;
import org.tdar.core.bean.keyword.GeographicKeyword;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.keyword.OtherKeyword;
import org.tdar.core.bean.keyword.SiteNameKeyword;
import org.tdar.core.bean.keyword.SiteTypeKeyword;
import org.tdar.core.bean.keyword.TemporalKeyword;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.GenericService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts.interceptor.annotation.RequiresTdarUserGroup;

import com.opensymphony.xwork2.Preparable;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/resource")
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
public class ResourceComparisonAction extends AbstractAuthenticatableAction implements Preparable {

    private static final long serialVersionUID = 1996434590439580868L;
    private List<Long> ids;
    private List<Resource> resources;
    
    @Autowired
    private GenericService genericService;
    private Set<CultureKeyword> cultures = new HashSet<>();
    private Set<InvestigationType> investigationTypes = new HashSet<>();
    private Set<TemporalKeyword> temporal = new HashSet<>();
    private Set<MaterialKeyword> material = new HashSet<>();
    private Set<OtherKeyword> other = new HashSet<>();
    private Set<GeographicKeyword> geographic = new HashSet<>();
    private Set<SiteNameKeyword> siteNames = new HashSet<>();
    private Set<SiteTypeKeyword> siteTypes = new HashSet<>();
    
    @Override
    public void prepare() throws Exception {
        resources = genericService.findAll(Resource.class, ids);
        setupSharedKeywords();
    }

    public void setupSharedKeywords() {
        boolean first = true;
        for (Resource r : resources) {
            if (first) {
                cultures.addAll(r.getActiveCultureKeywords());
                investigationTypes.addAll(r.getActiveInvestigationTypes());
                temporal.addAll(r.getActiveTemporalKeywords());
                material.addAll(r.getActiveMaterialKeywords());
                other.addAll(r.getActiveOtherKeywords());
                geographic.addAll(r.getActiveGeographicKeywords());
                siteNames.addAll(r.getActiveSiteNameKeywords());
                siteTypes.addAll(r.getActiveSiteTypeKeywords());
                first = false;
            } else {
                cultures = SetUtils.intersection(cultures, r.getActiveCultureKeywords());
                investigationTypes = SetUtils.intersection(investigationTypes, r.getActiveInvestigationTypes());
                temporal = SetUtils.intersection(temporal, r.getActiveTemporalKeywords());
                material = SetUtils.intersection(material, r.getActiveMaterialKeywords());
                other = SetUtils.intersection(other, r.getActiveOtherKeywords());
                geographic = SetUtils.intersection(geographic, r.getActiveGeographicKeywords());
                siteNames = SetUtils.intersection(siteNames, r.getActiveSiteNameKeywords());
                siteTypes = SetUtils.intersection(siteTypes, r.getActiveSiteTypeKeywords());
            }
        }
    }
    
    @Action(value = "compare", results = {
            @Result(name = SUCCESS, location = "compare.ftl")
    })
    @Override
    public String execute() {
        return SUCCESS;
    }
    
    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public Set<CultureKeyword> getCultures() {
        return cultures;
    }

    public void setCultures(HashSet<CultureKeyword> cultures) {
        this.cultures = cultures;
    }

    public Set<InvestigationType> getInvestigationTypes() {
        return investigationTypes;
    }

    public void setInvestigationTypes(HashSet<InvestigationType> investigationTypes) {
        this.investigationTypes = investigationTypes;
    }

    public Set<TemporalKeyword> getTemporal() {
        return temporal;
    }

    public void setTemporal(HashSet<TemporalKeyword> temporal) {
        this.temporal = temporal;
    }

    public Set<MaterialKeyword> getMaterial() {
        return material;
    }

    public void setMaterial(HashSet<MaterialKeyword> material) {
        this.material = material;
    }

    public Set<OtherKeyword> getOther() {
        return other;
    }

    public void setOther(HashSet<OtherKeyword> other) {
        this.other = other;
    }

    public Set<GeographicKeyword> getGeographic() {
        return geographic;
    }

    public void setGeographic(HashSet<GeographicKeyword> geographic) {
        this.geographic = geographic;
    }

    public Set<SiteNameKeyword> getSiteNames() {
        return siteNames;
    }

    public void setSiteNames(HashSet<SiteNameKeyword> siteNames) {
        this.siteNames = siteNames;
    }

    public Set<SiteTypeKeyword> getSiteTypes() {
        return siteTypes;
    }

    public void setSiteTypes(HashSet<SiteTypeKeyword> siteTypes) {
        this.siteTypes = siteTypes;
    }

}
