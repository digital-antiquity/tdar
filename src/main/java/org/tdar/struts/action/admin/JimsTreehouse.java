package org.tdar.struts.action.admin;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Document;
import org.tdar.struts.action.resource.AbstractInformationResourceController;

/**
 * Jim's treehouse is for experimental stuff that should not go into production. 
 * @author jimdevos
 *
 */
@ParentPackage("default")
@Namespace("/admin/treehouse")
@Component
@Scope("prototype")
public class JimsTreehouse extends AbstractInformationResourceController<Document>{
    private static final long serialVersionUID = -6995338248977016810L;
    
    public Long id = -1L;
    
    
    @SkipValidation
    @Action("file-upload-test")
    public String testFileUpload() {
        return SUCCESS;
    }
    
    @SkipValidation
    @Action("map-test")
    public String testMap() {
        return SUCCESS;
    }
    
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    private void prodcheck() {
        if(getTdarConfiguration().isProductionEnvironment()) {
            logger.error("Jim's treehouse is running in production");
            throw new RuntimeException("This should not run in production EVER EVER EVER");
        }
    }
    
    @Override
    public void prepare()  {
        prodcheck();
        super.prepare();
    }
    
    @Override
    protected String save(Document persistable) {
        return INPUT;
    }
    
    @Override
    public Class<Document> getPersistableClass() {
        return Document.class;
    }

}
