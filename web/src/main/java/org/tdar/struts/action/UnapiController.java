package org.tdar.struts.action;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.struts.interceptor.annotation.HttpForbiddenErrorResponseOnly;

/**
 * Implementation of <a href="http://unapi.info/specs/">Unapi</a> for tDAR
 * resources.
 * 
 * TODO: Currently, the format response is hard coded as we are only dealing
 * with Mods and DC. We can make this configurable in the future if we want
 * to expose arbitrary resources in arbitrary formats. But for now this is
 * sufficient.
 * 
 * $Id$
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 * 
 */
@Namespace("/")
@Component
@Scope("prototype")
@ParentPackage("default")
@HttpForbiddenErrorResponseOnly
public class UnapiController extends TdarActionSupport {

    private static final String FORMATS = "formats";
    private static final String ASFORMAT = "asformat";
    private static final String NOFORMAT = "noformat";

    private static final long serialVersionUID = -5455659179508107902L;

    @Autowired
    private transient ResourceService resourceService;

    @Autowired
    private transient UrlService urlService;

    private String formatUrl;
    private Long id;
    private String format;

    @Override
    @Actions(value = { @Action(
            value = "unapi",
            results = {
                    @Result(name = FORMATS,
                            location = "unapi-formatlist.ftl",
                            type = FREEMARKER,
                            params = { "contentType", "application/xml", "status", "300" }),
                    @Result(name = ASFORMAT, location = "${formatUrl}", type = "tdar-redirect"),
                    @Result(name = NOFORMAT, type = HTTPHEADER, params = { "status", "406" })
            }
            )
    })
    public String execute() {
        if (StringUtils.isNotBlank(format) && (id != null)) {
            Resource r = resourceService.find(id);
            if (r == null) {
                return TdarActionSupport.NOT_FOUND;
            }
            formatUrl = UrlService.constructUnAPIFormatUrl(r, format);
            if (StringUtils.isBlank(formatUrl)) {
                return NOFORMAT;
            }
            return ASFORMAT;
        }
        return FORMATS;
    }

    public String getFormatUrl() {
        return formatUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFormat(String format) {
        this.format = format;
    }

}
