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
import org.tdar.core.exception.FileUploadException;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/api/file")
public class DeleteFileAction extends AbstractHasFileAction<AbstractFile> {


    private static final long serialVersionUID = -7706315527740653556L;

    @Autowired
    private PersonalFilestoreService personalFilestoreService;


    @Action(value = "delete",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") })
    @PostOnly
    @WriteableSession
    public String execute() throws IOException, FileUploadException {
        personalFilestoreService.deleteFile(getFile(), getAuthenticatedUser());
        setResultObject(true);
        return SUCCESS;
    }

}
