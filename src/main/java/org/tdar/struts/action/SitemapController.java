package org.tdar.struts.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Namespace("/sitemap")
@ParentPackage("default")
@Component
@Scope("prototype")
@Results({
    @Result(name = "success", type = "stream",
            params = {
                    "inputName", "inputStream",
            }
    ),
@Result(name = "error", type = "httpheader", params = { "error", "404" }),
@Result(name = "forbidden", type = "httpheader", params = { "error", "403" })

})
public class SitemapController  extends AuthenticationAware.Base {

    private static final long serialVersionUID = 3087341894996134904L;

    private String filename;
    private InputStream inputStream;
    
    @Override
    @Action("sitemap")
    public String execute() {
        File dir = new File(getTdarConfiguration().getSitemapDir());
        File file = new File(dir, FilenameUtils.getName(filename));
        if (file.exists() && file.isFile()) {
            try {
                setInputStream(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                logger.error("file not found {}", e);
            }
            return SUCCESS;
        }
        return NOT_FOUND;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }
}
