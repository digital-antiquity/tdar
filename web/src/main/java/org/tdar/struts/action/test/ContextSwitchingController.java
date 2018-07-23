package org.tdar.struts.action.test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.configuration.TdarConfiguration;
import org.tdar.exception.TdarRuntimeException;
import org.tdar.struts.action.AbstractAuthenticatableAction;

@Namespace("/test/switchContext")
@Component
@Scope("prototype")
public class ContextSwitchingController extends AbstractAuthenticatableAction {

    private static final String CONTEXT_WARNING = "COULD NOT SWITCH CONTEXTS BECAUSE ENVIRONMENT VARIABLE WAS NOT SET PROPERLY -DenableContextSwitchingConfig=true";
    /**
     * 
     */
    private static final long serialVersionUID = -7284453639481489970L;
    private String configurationFile;

    @SuppressWarnings("deprecation")
    @Override
    @Actions({
            @Action(value = "denied", results = { @Result(name = SUCCESS, params = { "contentType", "text/plain" }, type = FREEMARKER,
                    location = "../../errors/access-denied.ftl") })
    })
    public String execute() throws URISyntaxException, IOException {
        getLogger().trace(System.getProperty("enableContextSwitchingConfig"));
        if ((getConfigurationFile() != null) && System.getProperty("enableContextSwitchingConfig", "false").equalsIgnoreCase("true")) {
            getLogger().info("switching tDarConfig to:" + getConfigurationFile());
            File requestFile = new File(configurationFile);
            String name = requestFile.getName();
            URL url = getClass().getClassLoader().getResource(name);
            File configFile = new File(url.toURI());
            TdarConfiguration.getInstance().setConfigurationFile(configFile);
        } else {
            getLogger().warn(CONTEXT_WARNING);
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
