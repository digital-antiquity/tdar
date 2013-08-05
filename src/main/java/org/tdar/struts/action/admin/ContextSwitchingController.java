package org.tdar.struts.action.admin;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.TdarRuntimeException;
import org.tdar.struts.action.AuthenticationAware;

@Namespace("/admin/switchContext")
@Component
@Scope("prototype")
public class ContextSwitchingController extends AuthenticationAware.Base {

    private static final String CONTEXT_WARNING = "COULD NOT SWITCH CONTEXTS BECAUSE ENVIRONMENT VARIABLE WAS NOT SET PROPERLY -DenableContextSwitchingConfig=true";
    /**
     * 
     */
    private static final long serialVersionUID = -7284453639481489970L;
    private String configurationFile;

    @SuppressWarnings("deprecation")
    @Override
    @Actions({
            @Action(value = "denied", results = { @Result(name = SUCCESS, params = { "contentType", "text/plain" }, type = "freemarker",
                    location = "../../errors/access-denied.ftl") })
    })
    public String execute() {
        logger.info(System.getProperty("enableContextSwitchingConfig"));
        if (getConfigurationFile() != null && System.getProperty("enableContextSwitchingConfig", "false").equalsIgnoreCase("true")) {
            logger.info("switching tDarConfig to:" + getConfigurationFile());
            TdarConfiguration.getInstance().setConfigurationFile(configurationFile);
        } else {
            logger.warn(CONTEXT_WARNING);
            if (!TdarConfiguration.getInstance().isProductionEnvironment()) {
                throw new TdarRuntimeException(CONTEXT_WARNING);
            }
        }
        return SUCCESS;
    }

    public String getConfigurationFile() {
        return configurationFile;
    }

    public void setConfigurationFile(String configurationFile) {
        this.configurationFile = configurationFile;
    }

}
