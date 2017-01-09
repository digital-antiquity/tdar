package org.tdar.balk.struts.action.items;

import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.balk.bean.DropboxUserMapping;
import org.tdar.balk.service.ItemService;
import org.tdar.balk.service.UserService;
import org.tdar.balk.service.WorkflowStatusReport;
import org.tdar.balk.struts.action.AbstractAuthenticatedAction;

import com.opensymphony.xwork2.Preparable;

@ParentPackage("secured")
@Namespace("/items")
@Component
@Scope("prototype")
public class ItemsAction extends AbstractAuthenticatedAction implements Preparable {

    private static final long serialVersionUID = -4366032864518820991L;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    private TreeMap<String, WorkflowStatusReport> itemStatusReport = new TreeMap<>();
    private DropboxUserMapping userInfo;
    private String path;
    private int page = 0;
    private int size = 100;
    private int total = 0;
    private boolean managed = false;

    private Set<String> children;
    @Override
    public void prepare() throws Exception {
        userInfo = userService.findUser(getAuthenticatedUser());
    }
    
    @Action(value="" , results={@Result(name=SUCCESS, type=FREEMARKER, location="items.ftl")})
    public String execute() throws Exception {
        setTotal(itemService.itemStatusReport(path, page, size,itemStatusReport, isManaged()));
        String path_ = StringUtils.stripEnd(path, "/");
        if (path_.contains("/")) {
            path_ = StringUtils.substringAfterLast(path_, "/");
        }
        setChildren(itemService.listChildPaths(path_));
        return super.execute();
    }

    public TreeMap<String, WorkflowStatusReport> getItemStatusReport() {
        return itemStatusReport;
    }

    public void setItemStatusReport(TreeMap<String, WorkflowStatusReport> itemStatusReport) {
        this.itemStatusReport = itemStatusReport;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public DropboxUserMapping getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(DropboxUserMapping userInfo) {
        this.userInfo = userInfo;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isManaged() {
        return managed;
    }

    public void setManaged(boolean managed) {
        this.managed = managed;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public Set<String> getChildren() {
        return children;
    }

    public void setChildren(Set<String> children) {
        this.children = children;
    }
}
