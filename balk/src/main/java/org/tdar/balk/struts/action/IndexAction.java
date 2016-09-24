package org.tdar.balk.struts.action;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.balk.service.ItemService;

@ParentPackage("secured")
@Namespace("/")
@Component
@Scope("prototype")
public class IndexAction extends AbstractAuthenticatedAction {


    private static final long serialVersionUID = 8069489623938108226L;

    @Autowired
    private ItemService itemService;
    
    @Override
    public String execute() throws Exception {
        itemService.listTopLevelPaths();
        return super.execute();
    }
}
