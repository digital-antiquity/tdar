package org.tdar.struts.action.workspace;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.filestore.personal.PersonalFilestoreFile;
import org.tdar.struts.action.AuthenticationAware;

import com.opensymphony.xwork2.Preparable;

/**
 * $Id$
 * 
 * Data integration activities in the workspace.
 * 
 * @author Allen Lee, Adam Brin
 * @version $Rev$
 */
@ParentPackage("secured")
@Namespace("/workspace")
@Component
@Scope("prototype")
public class IntegrationDownloadAction extends AuthenticationAware.Base implements Preparable {

    private static final long serialVersionUID = 8375939717549103423L;

    @Autowired
    private transient PersonalFilestoreService filestoreService;

    private Long ticketId;

    private String integrationDataResultsFilename;
    private long integrationDataResultsContentLength;
    private transient InputStream integrationDataResultsInputStream;

    @Action(value = "download", results = {
            @Result(name = SUCCESS, type = "stream",
                    params = {
                            "contentType", "application/vnd.ms-excel",
                            "inputName", "integrationDataResultsInputStream",
                            "contentDisposition", "attachment;filename=\"${integrationDataResultsFilename}\"",
                            "contentLength", "${integrationDataResultsContentLength}"
                    }),
            @Result(name = INPUT, type = "tdar-redirect", location = "select-tables")
    })
    public String downloadIntegrationDataResults() {

        return SUCCESS;
    }

    @Override
    public void prepare() throws Exception {
        try {
            List<PersonalFilestoreFile> files = filestoreService.retrieveAllPersonalFilestoreFiles(getTicketId());
            for (PersonalFilestoreFile target : files) {
                if (target.getFile().getName().endsWith(".xlsx")) {
                    integrationDataResultsInputStream = new FileInputStream(target.getFile());
                    integrationDataResultsContentLength = target.getFile().length();
                    integrationDataResultsFilename = target.getFile().getName();
                }
            }

        } catch (IOException exception) {
            addActionErrorWithException("Unable to access file.", exception);
        }
    }

    public String getIntegrationDataResultsFilename() {
        return integrationDataResultsFilename;
    }

    public InputStream getIntegrationDataResultsInputStream() {
        return integrationDataResultsInputStream;
    }

    public long getIntegrationDataResultsContentLength() {
        return integrationDataResultsContentLength;
    }

    /**
     * @param ticketId
     *            the ticketId to set
     */
    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    /**
     * @return the ticketId
     */
    public Long getTicketId() {
        return ticketId;
    }

}
