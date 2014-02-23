package org.tdar.struts.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.service.XmlService;


/**
 * $Id$
 * 
 * <p>
 * Action for the root namespace.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@Namespace("/schema")
@ParentPackage("default")
@Component
@Scope("prototype")
public class SchemaController extends TdarActionSupport {
    private static final long serialVersionUID = -9216882130992021384L;
    private InputStream inputStream;

    @Autowired
    private XmlService xmlService;

    @Override
    @Action(value = "current", results = {
            @Result(name = "success", type = "stream", params = {
                    "contentType", "text/xml",
                    "inputName", "inputStream"
            })
    })
    public String execute() {
        try {
            File file = xmlService.generateSchema();
            setInputStream(new FileInputStream(file));
        } catch (Exception e) {
            getLogger().error("could not create schema", e);
            return ERROR;
        }
        return SUCCESS;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

}
