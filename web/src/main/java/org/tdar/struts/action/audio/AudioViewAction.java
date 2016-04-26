package org.tdar.struts.action.audio;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Audio;
import org.tdar.struts.action.resource.AbstractResourceViewAction;

@Component
@Scope("prototype")
@ParentPackage("default")
@Namespace("/audio")
public class AudioViewAction extends AbstractResourceViewAction<Audio> {

    private static final long serialVersionUID = -59400140841882295L;

    @Override
    public Class<Audio> getPersistableClass() {
        return Audio.class;
    }

}
