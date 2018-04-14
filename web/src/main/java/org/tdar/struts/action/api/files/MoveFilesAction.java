package org.tdar.struts.action.api.files;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.file.AbstractFile;
import org.tdar.core.bean.file.TdarDir;
import org.tdar.core.bean.file.TdarFile;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.struts.action.api.AbstractJsonApiAction;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/api/file")
public class MoveFilesAction extends AbstractJsonApiAction {

    private static final long serialVersionUID = -338255822186649559L;
    private Long toId;
    private TdarDir dir;
    private List<Long> fileIds = new ArrayList<>();
    private List<AbstractFile> files = new ArrayList<>();
    @Autowired
    private PersonalFilestoreService personalFilestoreService;

    @Override
    public void prepare() throws Exception {
        super.prepare();
        if (toId != null) {
            dir = getGenericService().find(TdarDir.class, toId);
        }
        for (Long id : fileIds) {
            files.add(getGenericService().find(AbstractFile.class, id));
        }
    }

    @Override
    public void validate() {
        super.validate();
        if (files.size() != fileIds.size()) {
            addActionError("moveFileAction.not_all_files_valid");
        }
    }

    @Action(value = "move",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = { @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "jsonInputStream" }),
                    @Result(name = ERROR, type = JSONRESULT, params = { "stream", "jsonInputStream", "statusCode", "400" })
            })
    @PostOnly
    @WriteableSession
    public String execute() throws IOException {
        personalFilestoreService.moveFiles(files, dir, getAuthenticatedUser());
        setResultObject(true);
        return SUCCESS;
    }

}
