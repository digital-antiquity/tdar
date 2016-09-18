package org.tdar.balk.struts.action;

import java.util.TreeMap;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.balk.service.ItemService;
import org.tdar.balk.service.WorkflowStatusReport;

@ParentPackage("secured")
@Namespace("")
@Component
@Scope("prototype")
public class IndexAction extends AbstractAuthenticatedAction {

    private static final long serialVersionUID = -4366032864518820991L;

    @Autowired
    private ItemService itemService;

    private TreeMap<String, WorkflowStatusReport> itemStatusReport;

    private String path;
    
    @Override
    public String execute() throws Exception {
        setItemStatusReport(itemService.itemStatusReport(path));
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
}
