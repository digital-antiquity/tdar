package org.tdar.struts.freemarker;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.views.freemarker.FreemarkerManager;
import org.apache.struts2.views.freemarker.ScopesHashModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.tdar.core.configuration.TdarConfiguration;

import com.opensymphony.xwork2.util.ValueStack;

import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateException;

@Component
public class TdarFreemarkerManager extends FreemarkerManager {

    protected transient Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void init(ServletContext servletContext) throws TemplateException {
        super.init(servletContext);
        if (TdarConfiguration.getInstance().isProductionEnvironment()) {
            config.setTemplateExceptionHandler(new TdarFreemarkerTemplateExceptionHandler());
        }
    }

    @Override
    public ScopesHashModel buildTemplateModel(ValueStack stack, Object action, ServletContext servletContext, HttpServletRequest request,
            HttpServletResponse response, ObjectWrapper wrapper) {
        return super.buildTemplateModel(stack, action, servletContext, request, response, wrapper);
    }

    @Override
    protected void populateContext(ScopesHashModel model, ValueStack stack, Object action, HttpServletRequest request, HttpServletResponse response) {
        super.populateContext(model, stack, action, request, response);
    }

}
