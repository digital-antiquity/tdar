package org.tdar.struts.action.resource;

import java.io.IOException;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.keyword.InvestigationType;
import org.tdar.core.bean.keyword.MaterialKeyword;
import org.tdar.core.bean.resource.Addressable;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.GenericKeywordService;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.SerializationService;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.Preparable;

public class ResourceEditAction<P extends Persistable & Addressable> extends AbstractAuthenticatableAction implements Preparable {

    @Autowired
    private SerializationService serializationService;

    @Autowired
    private GenericKeywordService genericKeywordService;

    @Autowired
    private GenericService genericService;

    private static final long serialVersionUID = 1467806719681708555L;

    private Long id;
    private Resource resource = new Resource();
    private String json = "";

    @Override
    public void prepare() throws Exception {
        if (PersistableUtils.isNotNullOrTransient(id)) {
            resource = getGenericService().find(Resource.class, id);
        }
        json = serializationService.convertToJson(resource);
    }

    @Override
    @Action(value = "edit", results = { @Result(name = SUCCESS, location = "edit.ftl") })
    public String execute() throws Exception {
        // TODO Auto-generated method stub
        return super.execute();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String getInvestigationTypes() throws IOException {
        List<InvestigationType> types = genericService.findAll(InvestigationType.class);
        return serializationService.convertToJson(types);
    }

    public String getMaterialTypes() throws IOException {
        List<MaterialKeyword> types = genericKeywordService.findAllApproved(MaterialKeyword.class);
        return serializationService.convertToJson(types);
    }

}
