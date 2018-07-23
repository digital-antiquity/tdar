package org.tdar.struts.action.api.files;

import java.io.IOException;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.file.CurationState;
import org.tdar.core.bean.file.TdarFile;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;

@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/api/file")
public class EditFileMetadataAction extends AbstractHasFileAction<TdarFile>{

    private static final long serialVersionUID = -3146064278797351637L;
    private String note;
    private boolean needsOcr;
    private CurationState curate;

    @Autowired
    private PersonalFilestoreService personalFilestoreService;

    @Action(value = "editMetadata",
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") })
    @PostOnly
    @WriteableSession
    public String execute() throws IOException {
        personalFilestoreService.editMetadata(getFile(), note, needsOcr, getCurate(), getAuthenticatedUser());
        setResultObject(getFile());
        return SUCCESS;
    }


    public void setNote(String note) {
        this.note = note;
    }

    public boolean isNeedsOcr() {
        return needsOcr;
    }

    public void setNeedsOcr(boolean needsOcr) {
        this.needsOcr = needsOcr;
    }

    public CurationState getCurate() {
        return curate;
    }


    public void setCurate(CurationState curate) {
        this.curate = curate;
    }

}
