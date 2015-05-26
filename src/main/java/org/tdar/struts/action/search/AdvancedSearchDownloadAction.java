package org.tdar.struts.action.search;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.DisplayOrientation;
import org.tdar.core.bean.coverage.LatitudeLongitudeBox;
import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.ResourceCreator;
import org.tdar.core.bean.resource.Dataset.IntegratableOptions;
import org.tdar.core.bean.resource.DocumentType;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceAccessType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.exception.StatusCode;
import org.tdar.core.service.ExcelService;
import org.tdar.core.service.UrlService;
import org.tdar.search.query.FacetValue;
import org.tdar.search.query.QueryFieldNames;
import org.tdar.struts.action.TdarActionException;
import org.tdar.struts.data.FacetGroup;
import org.tdar.struts.interceptor.annotation.HttpOnlyIfUnauthenticated;

@Namespace("/search")
@Component
@Scope("prototype")
@ParentPackage("default")
@HttpOnlyIfUnauthenticated
public class AdvancedSearchDownloadAction extends AbstractAdvancedSearchController {

    private static final long serialVersionUID = 7426286742246468225L;

    @Autowired
    private transient ExcelService excelService;

    @Autowired
    private transient UrlService urlService;

    // facet statistics for results.ftl
    private ArrayList<FacetValue> resourceTypeFacets = new ArrayList<>();
    private ArrayList<FacetValue> documentTypeFacets = new ArrayList<>();
    private ArrayList<FacetValue> fileAccessFacets = new ArrayList<>();
    private ArrayList<FacetValue> integratableOptionFacets = new ArrayList<>();

    // contentLength for excel download requests
    private Long contentLength;
    private InputStream inputStream;

    @Action(value = "download", results = { @Result(name = SUCCESS, type = "stream", params = {
            "contentType", "application/vnd.ms-excel", "inputName",
            "inputStream", "contentDisposition",
            "attachment;filename=\"report.xls\"", "contentLength",
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
                Sheet sheet = excelService.createWorkbook("results");

                List<String> fieldNames = new ArrayList<String>(Arrays.asList(
                        "id", "resourcetype", "title", "date", "authors",
                        "project", "description", "number_of_files", "url",
                        "physical_location"));

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
                excelService.addDocumentHeaderRow(sheet, rowNum, 0,
                        Arrays.asList(getText("advancedSearchController.excel_search_results", TdarConfiguration.getInstance().getSiteAcronym(),
                                getSearchPhrase())));
                rowNum++;
                List<String> headerValues = Arrays.asList(getText("advancedSearchController.search_url"), UrlService.getBaseUrl()
                        + getServletRequest().getRequestURI()
                                .replace("/download", "/results") + "?" + getServletRequest().getQueryString());
                excelService.addPairedHeaderRow(sheet, rowNum, 0, headerValues);
                rowNum++;
                excelService.addPairedHeaderRow(sheet, rowNum, 0,
                        Arrays.asList(getText("advancedSearchController.downloaded_by"),
                                getText("advancedSearchController.downloaded_on", getAuthenticatedUser().getProperName(), new Date())));
                rowNum++;
                rowNum++;
                for (int i = 0; i < fieldNames.size(); i++) {
                    fieldNames.set(i, getText("advancedSearchController." + fieldNames.get(i)));
                }

                excelService.addHeaderRow(sheet, rowNum, 0, fieldNames);
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
                        List<String> filenames = new ArrayList<String>();
                        String location = "";
                        String projectName = "";
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
                        List<Creator> authors = new ArrayList<Creator>();

                        for (ResourceCreator creator : r.getPrimaryCreators()) {
                            authors.add(creator.getCreator());
                        }

                        ArrayList<Object> data = new ArrayList<Object>(
                                Arrays.asList(r.getId(), r.getResourceType(), r.getTitle(), dateCreated, authors,
                                        projectName, r.getShortenedDescription(), numFiles,
                                        urlService.absoluteUrl(r), location));

                        if (isEditor()) {
                            data.add(r.getStatus());
                            data.add(StringUtils.join(filenames, ","));
                            data.add(r.getDateCreated());
                            data.add(r.getSubmitter().getProperName());
                            data.add(r.getDateUpdated());
                            data.add(r.getUpdatedBy().getProperName());
                        }

                        excelService.addDataRow(sheet, rowNum, 0, data);
                    }
                    if (startRecord < getTotalRecords()) {
                        performResourceSearch();
                    }
                }

                excelService.setColumnWidth(sheet, 0, 5000);

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

    @Override
    protected void updateDisplayOrientationBasedOnSearchResults() {
        if (getOrientation() != null) {
            getLogger().debug("orientation is set to: {}", getOrientation());
            return;
        }

        if (CollectionUtils.isNotEmpty(getResourceTypeFacets())) {
            boolean allImages = true;
            for (FacetValue val : getResourceTypeFacets()) {
                if (val.getCount() > 0 && !ResourceType.isImageName(val.getValue())) {
                    allImages = false;
                }
            }
            // if we're only dealing with images, and an orientation has not been set
            if (allImages) {
                setOrientation(DisplayOrientation.GRID);
                getLogger().debug("switching to grid orientation");
                return;
            }
        }
        LatitudeLongitudeBox map = null;
        try {
            map = getG().get(0).getLatitudeLongitudeBoxes().get(0);
        } catch (Exception e) {
            // ignore
        }
        if (getMap() != null && getMap().isInitializedAndValid() || map != null && map.isInitializedAndValid()) {
            getLogger().debug("switching to map orientation");
            setOrientation(DisplayOrientation.MAP);
        }
    }

    public List<FacetValue> getResourceTypeFacets() {
        return resourceTypeFacets;
    }

    public List<FacetValue> getIntegratableOptionFacets() {
        return integratableOptionFacets;
    }

    public List<FacetValue> getDocumentTypeFacets() {
        return documentTypeFacets;
    }

    public List<FacetValue> getFileAccessFacets() {
        return fileAccessFacets;
    }

    @Override
    public List<FacetGroup<? extends Enum>> getFacetFields() {
        List<FacetGroup<?>> group = new ArrayList<FacetGroup<?>>();
        group.add(new FacetGroup<ResourceType>(ResourceType.class, QueryFieldNames.RESOURCE_TYPE, resourceTypeFacets, ResourceType.DOCUMENT));
        group.add(new FacetGroup<IntegratableOptions>(IntegratableOptions.class, QueryFieldNames.INTEGRATABLE, integratableOptionFacets,
                IntegratableOptions.YES));
        group.add(new FacetGroup<ResourceAccessType>(ResourceAccessType.class, QueryFieldNames.RESOURCE_ACCESS_TYPE, fileAccessFacets,
                ResourceAccessType.CITATION));
        group.add(new FacetGroup<DocumentType>(DocumentType.class, QueryFieldNames.DOCUMENT_TYPE, documentTypeFacets, DocumentType.BOOK));
        return group;
    }

}
