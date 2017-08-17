package org.tdar.struts.action.schema;

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
import org.tdar.core.service.SerializationService;
import org.tdar.struts.action.TdarBaseActionSupport;

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
public class SchemaController extends TdarBaseActionSupport {

    private static final long serialVersionUID = -52006343273049231L;

    private InputStream inputStream;

    @Autowired
    private SerializationService serializationService;

    @Override
    @Action(value = "current", results = {
            @Result(name = SUCCESS, type = "stream", params = {
                    "contentType", "text/xml",
                    "inputName", "inputStream"
            })
    })
    public String execute() {
        try {
            File file = serializationService.generateSchema();
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
