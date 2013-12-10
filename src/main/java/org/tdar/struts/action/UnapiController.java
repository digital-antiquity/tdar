package org.tdar.struts.action;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Resource;

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
public class UnapiController extends TdarActionSupport {

    private static final long serialVersionUID = -5455659179508107902L;

    private String formatUrl;
    private Long id;
    private String format;

    @Override
    @Action(
            value = "unapi",
            results = {
                    @Result(name = "formats",
                            location = "unapi-formatlist.ftl",
                            type = "freemarker",
                            params = { "contentType", "application/xml", "status", "300" }),
                    @Result(name = "asformat", location = "${formatUrl}", type = "redirect"),
                    @Result(name = "noformat", type = "httpheader", params = { "status", "406" })
            }
            )
            public String execute() {
        if (StringUtils.isNotBlank(format) && id != null) {
            Resource r = getResourceService().find(id);
            if (r == null) {
                return TdarActionSupport.NOT_FOUND;
            }
            formatUrl = constructFormatUrl(r);
            if (StringUtils.isBlank(formatUrl)) {
                return "noformat";
            }
            return "asformat";
        }
        return "formats";
    }

    private String constructFormatUrl(Resource r) {
        String resUrl = getUrlService().relativeUrl(r);
        String formUrl = null;

        // abrin: adding for personal readability, this forwards to eg:
        // /document/{id}/dc
        if (format.equalsIgnoreCase("oai_dc")) {
            formUrl = resUrl + "/dc";
        }
        if (format.equalsIgnoreCase("mods")) {
            formUrl = resUrl + "/mods";
        }
        return formUrl;
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
