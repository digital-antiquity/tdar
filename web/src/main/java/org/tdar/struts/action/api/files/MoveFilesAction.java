package org.tdar.struts.action.api.files;

import java.io.IOException;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.file.AbstractFile;
import org.tdar.core.bean.file.TdarDir;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/api/file")
public class MoveFilesAction extends AbstractHasFilesAction<AbstractFile> {

    private static final long serialVersionUID = -338255822186649559L;
    private Long toId;
    private TdarDir dir;

    @Autowired
    private PersonalFilestoreService personalFilestoreService;

    @Override
    public void prepare() throws Exception {
        super.prepare();
        if (toId != null) {
            dir = getGenericService().find(TdarDir.class, toId);
        }
    }

    @Override
    public void validate() {
        super.validate();
    }

    @Action(value = "move",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") })
    @PostOnly
    @WriteableSession
    public String execute() throws IOException {
        personalFilestoreService.moveFiles(getFiles(), dir, getAuthenticatedUser());
        setResultObject(true);
        return SUCCESS;
    }

}
