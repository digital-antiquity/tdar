package org.tdar.balk.struts.action;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Preparable;

import main.java.org.tdar.balk.bean.AbstractDropboxItem;
import main.java.org.tdar.balk.bean.DropboxUserMapping;
import main.java.org.tdar.balk.service.ItemService;
import main.java.org.tdar.balk.service.Phases;
import main.java.org.tdar.balk.service.UserService;
import main.java.org.tdar.utils.dropbox.DropboxConstants;

@ParentPackage("secured")
@Namespace("/startWorkflow")
@Component
@Scope("prototype")
@WriteableSession
@PostOnly
public class StartWokflowAction extends AbstractAuthenticatedAction implements Preparable {

    private static final long serialVersionUID = -6764214695089406601L;
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;

    private String id;

    private AbstractDropboxItem item;
    private Phases phase;
    private String path;
    private DropboxUserMapping userMapping;
    
    @Override
    public void prepare() throws Exception {
        setItem(itemService.findByDropboxId(id, false));
        userMapping = userService.findUser(getAuthenticatedUser());
    }

    @Action(value="",results={@Result(name=SUCCESS,type=REDIRECT, location="/items?path=${path}")})
    @Override
    public String execute() throws Exception {
        try {
            // FIGURE OUT WHAT PHASE, FIGURE OUT WHAT PATH
            String newPath = phase.getPath();
            newPath += item.getPath().replace(DropboxConstants.CLIENT_DATA, "");
            itemService.copy(item, newPath, userMapping, getAuthenticatedUser());
        } catch (Exception e) {
            getLogger().error("{}", e, e);
            addActionError(e.getMessage() + " " + ExceptionUtils.getFullStackTrace(e));
            return INPUT;
        }
        return SUCCESS;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AbstractDropboxItem getItem() {
        return item;
    }

    public void setItem(AbstractDropboxItem item) {
        this.item = item;
    }

    public Phases getPhase() {
        return phase;
    }

    public void setPhase(Phases phase) {
        this.phase = phase;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
