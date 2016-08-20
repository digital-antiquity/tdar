package org.tdar.balk.struts.action;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@ParentPackage("secured")
@Namespace("")
@Component
@Scope("prototype")
public class IndexAction extends AbstractAuthenticatedAction {

    private static final long serialVersionUID = -4366032864518820991L;

}
