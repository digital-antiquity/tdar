package org.tdar.struts.action.download.hosted;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.struts.interceptor.annotation.HttpOnlyIfUnauthenticated;

import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.io.StringReader;

/**
 * Created by jimdevos on 9/23/14.
 */
@Namespace("/download")
@Component
@Scope("prototype")
@ParentPackage("default")
@HttpOnlyIfUnauthenticated
public class HostedDownloadAction {


    //HACK: this is temporary - eventually it will be handled by an interceptor and/or service.
    private String apiKey = "";

    @Action(
            value="hosted/{apiKey}",
            results = {@Result(name="success", type="streamhttp", params={"inputName", "inputStream", "status", "200"})}
    )
    public String execute() {
        return "success";
    }



    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public InputStream getInputStream() {
        //TODO: file goes here
        StringReader sr = new StringReader("hello world");
        ReaderInputStream ris = new ReaderInputStream(sr);
        return ris;
    }

}
