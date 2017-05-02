package org.tdar.struts.action.admin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserAffiliation;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.ExcelWorkbookWriter;
import org.tdar.struts.action.AbstractAuthenticatableAction;
import org.tdar.struts_base.interceptor.annotation.RequiresTdarUserGroup;
import org.tdar.struts_base.interceptor.annotation.HttpsOnly;

import com.opensymphony.xwork2.Preparable;

/**
 * $Id$
 * 
 * Administrative actions (that shouldn't be available for wide use).
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
@ParentPackage("secured")
@Namespace("/admin/user")
@Component
@Scope("prototype")
@RequiresTdarUserGroup(TdarGroup.TDAR_EDITOR)
@HttpsOnly
public class AdminUserStatsExportAction extends AbstractAuthenticatableAction implements Preparable {

    /**
     * 
     */
    private static final long serialVersionUID = 5483558796403216703L;

    @Autowired
    private EntityService entityService;

    private File tempFile;

    private FileInputStream inputStream;
    private long contentLength;

    @Override
    public void prepare() throws Exception {
        Set<Long> findAllContributorIds = entityService.findAllContributorIds();
        List<TdarUser> findAllRegisteredUsers = entityService.findAllRegisteredUsers();

        ExcelWorkbookWriter excelWriter = new ExcelWorkbookWriter();
        Sheet sheet = excelWriter.createWorkbook("results");

        List<String> fieldNames = new ArrayList<String>(Arrays.asList(
                "id", "username", "emaill", "first name", "last name", "institution", "date created", "contributor (signup)", "contributor (created record)",
                "affiliation"));

        int rowNum = 0;

        excelWriter.addHeaderRow(sheet, rowNum, 0, fieldNames);
        for (TdarUser user : findAllRegisteredUsers) {
            rowNum++;
            List<Object> data = new ArrayList<>();
            data.add(user.getId());
            data.add(user.getUsername());
            data.add(user.getEmail());
            data.add(user.getFirstName());
            data.add(user.getLastName());
            data.add(user.getInstitutionName());
            data.add(user.getDateCreated());
            data.add(user.isContributor());
            data.add(findAllContributorIds.contains(user.getId()));
            UserAffiliation affil = user.getAffiliation();
            String af = "";
            if (affil == null) {
                if (StringUtils.containsIgnoreCase(user.getEmail(), ".edu") ||
                        StringUtils.containsIgnoreCase(user.getInstitutionName(), "university")) {
                    af = "Academic";
                } else if (StringUtils.containsIgnoreCase(user.getEmail(), ".gov") ||
                        StringUtils.containsIgnoreCase(user.getEmail(), ".mil")) {
                    af = "Government";
                }
            } else {
                af = affil.getLabel();
            }
            data.add(af);

            excelWriter.addDataRow(sheet, rowNum, 0, data);
        }
        ;

        tempFile = File.createTempFile("user-results", ".xls");
        FileOutputStream fos = new FileOutputStream(tempFile);
        sheet.getWorkbook().write(fos);
        fos.close();
        setInputStream(new FileInputStream(tempFile));
        setContentLength(tempFile.length());
    }

    @Action(value = "user-mailchimp", results = { @Result(name = SUCCESS, type = "stream", params = {
            "contentType", "application/vnd.ms-excel", "inputName",
            "inputStream", "contentDisposition",
            "attachment;filename=\"user-report.xls\"", "contentLength",
            "${contentLength}" }) })
    @Override
    public String execute() {
        return SUCCESS;
    }

    
    public FileInputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(FileInputStream inputStream) {
        this.inputStream = inputStream;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

}
