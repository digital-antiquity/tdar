package org.tdar.balk.struts.action;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.balk.bean.AbstractDropboxItem;
import org.tdar.balk.bean.DropboxUserMapping;
import org.tdar.balk.service.ItemService;
import org.tdar.balk.service.Phases;
import org.tdar.balk.service.UserService;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;

import com.opensymphony.xwork2.Preparable;

@ParentPackage("secured")
@Namespace("/archive")
@Component
@Scope("prototype")
@WriteableSession
@PostOnly
public class ArchiveAction extends AbstractAuthenticatedAction implements Preparable {

    private static final long serialVersionUID = -6850649205111686901L;

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

    @Action(value="",results={@Result(name=SUCCESS,type=REDIRECT, location="/${contextPath}/items/list?path=${path}")})
    @Override
    public String execute() throws Exception {
        try {
            itemService.move(item, phase, userMapping, getAuthenticatedUser());
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
