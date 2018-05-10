package org.tdar.struts.action.api.files;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.file.AbstractFile;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.struts.action.api.AbstractJsonApiAction;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/api/file")
public abstract class AbstractHasFileAction<C extends AbstractFile> extends AbstractJsonApiAction {

    private static final long serialVersionUID = -1863500640813008918L;
    private Long id;
    private C file;

    @Override
    public void prepare() throws Exception {
        super.prepare();
        setFile((C)getGenericService().find(AbstractFile.class, getId()));
    }

    @Override
    public void validate() {
        super.validate();
        if (getFile() == null) {
            addActionError("moveFileAction.not_all_files_valid");
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public C getFile() {
        return file;
    }

    public void setFile(C file) {
        this.file = file;
    }

    

}
