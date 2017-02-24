package org.tdar.struts.action.search;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.collection.ResourceCollection;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.file.InformationResourceFileVersion;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.ExcelWorkbookWriter;
import org.tdar.core.service.UrlService;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.interceptor.annotation.HttpOnlyIfUnauthenticated;

@Namespace("/search")
@Component
@Scope("prototype")
@ParentPackage("default")
@HttpOnlyIfUnauthenticated
public class AdvancedSearchDownloadAction extends AbstractAdvancedSearchController {

    private static final String RESOURCETYPE = "resourcetype";
    private static final String PROJECT = "project";
    private static final String DESCRIPTION = "description";
    private static final String TITLE = "title";

    private static final long serialVersionUID = 7426286742246468225L;

    // contentLength for excel download requests
    private Long contentLength;
    private InputStream inputStream;

    @Action(value = "download", results = { @Result(name = SUCCESS, type = "stream", params = {
            "contentType", "application/vnd.ms-excel", "inputName",
            "inputStream", "contentDisposition",
            "attachment;filename=\"report.xlsx\"", "contentLength",
            "${contentLength}" }) })
    public String viewExcelReport() throws ParseException, TdarActionException {
        if (!isAuthenticated()) {
            throw new TdarActionException(StatusCode.UNAUTHORIZED, getText("advancedSearchController.log_in_required"));
        }
        try {
            setMode("excel");
            setRecordsPerPage(200);
            performResourceSearch();
            int rowNum = 0;
            int maxRow = getMaxDownloadRecords();
            if (maxRow > getTotalRecords()) {
                maxRow = getTotalRecords();
            }
            if (getTotalRecords() > 0) {
                ExcelWorkbookWriter excelWriter  = new ExcelWorkbookWriter();
                Sheet sheet = excelWriter.createWorkbook("results",SpreadsheetVersion.EXCEL2007);

                List<String> fieldNames = new ArrayList<String>(Arrays.asList(
                        "id", RESOURCETYPE, TITLE, "date", "authors",
                        PROJECT, DESCRIPTION, "number_of_files", "url",
                        "physical_location","collections"));

                if (isEditor()) {
                    fieldNames.add("status");
                    fieldNames.add("filenames");
                    fieldNames.add("date_added");
                    fieldNames.add("submitted_by");
                    fieldNames.add("date_last_updated");
                    fieldNames.add("updated_by");
                }

                // ADD HEADER ROW THAT SHOWS URL and SEARCH PHRASE
                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, fieldNames.size()));
                excelWriter.addDocumentHeaderRow(sheet, rowNum, 0,
                        Arrays.asList(getText("advancedSearchController.excel_search_results", TdarConfiguration.getInstance().getSiteAcronym(),
                                getSearchPhrase())));
                rowNum++;
                List<String> headerValues = Arrays.asList(getText("advancedSearchController.search_url"), UrlService.getBaseUrl()
                        + getServletRequest().getRequestURI()
                                .replace("/download", "/results") + "?" + getServletRequest().getQueryString());
                excelWriter.addPairedHeaderRow(sheet, rowNum, 0, headerValues);
                rowNum++;
                excelWriter.addPairedHeaderRow(sheet, rowNum, 0,
                        Arrays.asList(getText("advancedSearchController.downloaded_by"),
                                getText("advancedSearchController.downloaded_on", getAuthenticatedUser().getProperName(), new Date())));
                rowNum++;
                rowNum++;
                for (int i = 0; i < fieldNames.size(); i++) {
                    fieldNames.set(i, getText("advancedSearchController." + fieldNames.get(i)));
                }

                excelWriter.addHeaderRow(sheet, rowNum, 0, fieldNames);
                int startRecord = 0;
                int currentRecord = 0;
                while (currentRecord < maxRow) {
                    startRecord = getNextPageStartRecord();
                    setStartRecord(getNextPageStartRecord()); // resetting for
                                                              // next search
                    for (Resource result : getResults()) {
                        rowNum++;
                        if (currentRecord++ > maxRow) {
                            break;
                        }
                        Resource r = result;
                        Integer dateCreated = null;
                        Integer numFiles = 0;
                        List<String> filenames = new ArrayList<>();
                        String location = "";
                        String projectName = "";
                        List<String> collections = new ArrayList<>();
                        r.getResourceCollections().forEach(rc -> {
                            if (rc.isShared() && getAuthorizationService().canView(getAuthenticatedUser(), rc)) {
                                collections.add(rc.getName());
                            }
                        });
                        if (result instanceof InformationResource) {
                            InformationResource ir = (InformationResource) result;
                            dateCreated = ir.getDate();
                            numFiles = ir.getTotalNumberOfFiles();
                            for (InformationResourceFileVersion file : ir.getLatestUploadedVersions()) {
                                filenames.add(file.getFilename());
                            }
                            InformationResource ires = ((InformationResource) r);
                            location = ires.getCopyLocation();
                            projectName = ires.getProjectTitle();

                        }
                        List<Creator<?>> authors = new ArrayList<>();

                        for (ResourceCreator creator : r.getPrimaryCreators()) {
                            authors.add(creator.getCreator());
                        }

                        ArrayList<Object> data = new ArrayList<Object>(
                                Arrays.asList(r.getId(), r.getResourceType(), r.getTitle(), dateCreated, authors,
                                        projectName, r.getShortenedDescription(), numFiles,
                                        UrlService.absoluteUrl(r), location, StringUtils.join(collections, ",")));

                        if (isEditor()) {
                            data.add(r.getStatus());
                            data.add(StringUtils.join(filenames, ","));
                            data.add(r.getDateCreated());
                            data.add(r.getSubmitter().getProperName());
                            data.add(r.getDateUpdated());
                            data.add(r.getUpdatedBy().getProperName());
                        }

                        excelWriter.addDataRow(sheet, rowNum, 0, data);
                    }
                    if (startRecord < getTotalRecords()) {
                        performResourceSearch();
                    }
                }

//                excelWriter.setColumnWidth(sheet, 0, 5000);
                for (int i=0; i < fieldNames.size();i++) {
                    if (StringUtils.containsAny(fieldNames.get(i), TITLE,PROJECT,DESCRIPTION,RESOURCETYPE)) {
                        sheet.setColumnWidth(i, 20);
                    } else {
                        sheet.autoSizeColumn(i, false);
                    }
                }
                File tempFile = File.createTempFile("results", ".xls");
                FileOutputStream fos = new FileOutputStream(tempFile);
                sheet.getWorkbook().write(fos);
                fos.close();
                setInputStream(new FileInputStream(tempFile));
                contentLength = tempFile.length();
            }
        } catch (Exception e) {
            addActionErrorWithException(getText("advancedSearchController.something_happened_with_excel_export"), e);
            return INPUT;
        }

        return SUCCESS;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public Long getContentLength() {
        return contentLength;
    }

}
