package org.tdar.struts.action.video;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Video;
import org.tdar.struts.action.resource.AbstractResourceViewAction;

@Component
@Scope("prototype")
@ParentPackage("default")
@Namespace("/video")
public class VideoViewAction extends AbstractResourceViewAction<Video> {

    private static final long serialVersionUID = -1500162569254791978L;

    @Override
    public Class<Video> getPersistableClass() {
        return Video.class;
    }
}
