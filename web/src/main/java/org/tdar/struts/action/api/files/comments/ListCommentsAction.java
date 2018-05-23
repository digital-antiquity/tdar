package org.tdar.struts.action.api.files.comments;

import java.io.IOException;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.file.AbstractFile;
import org.tdar.struts.action.api.files.AbstractHasFileAction;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/api/file/comment")
public class ListCommentsAction extends AbstractHasFileAction<AbstractFile> {

    private static final long serialVersionUID = 127836598074774223L;

    @Action(value = "list")
    public String execute() throws IOException {
        setResultObject(getFile().getComments());
        return SUCCESS;
    }

}
