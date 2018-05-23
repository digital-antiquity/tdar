package org.tdar.struts.action.api.files.comments;

import java.io.IOException;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.file.AbstractFile;
import org.tdar.core.bean.file.FileComment;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.struts.action.api.files.AbstractHasFileAction;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/api/file/comment")
public class AddFileCommentAction extends AbstractHasFileAction<AbstractFile>{

    private static final long serialVersionUID = 3366561187632570405L;

    private String comment;

    @Autowired
    private PersonalFilestoreService personalFilestoreService;

    @Action(value = "add",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") })
    @PostOnly
    @WriteableSession
    public String execute() throws IOException {
        FileComment fileComment = personalFilestoreService.addComment(getFile(), comment, getAuthenticatedUser());
        setResultObject(fileComment );
        return SUCCESS;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
