package org.tdar.struts;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.views.freemarker.FreemarkerManager;
import org.apache.struts2.views.freemarker.ScopesHashModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.opensymphony.xwork2.util.ValueStack;

import freemarker.template.ObjectWrapper;

@Component
public class TdarFreemarkerManager extends FreemarkerManager {

    protected transient Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public ScopesHashModel buildTemplateModel(ValueStack stack, Object action, ServletContext servletContext, HttpServletRequest request,
            HttpServletResponse response, ObjectWrapper wrapper) {
        return super.buildTemplateModel(stack, action, servletContext, request, response, wrapper);
    }

    @Override
    protected void populateContext(ScopesHashModel model, ValueStack stack, Object action, HttpServletRequest request, HttpServletResponse response) {
        logger.debug("action: {} {} {} {}", action, wrapper, stack);
        super.populateContext(model, stack, action, request, response);
    }
    
}
