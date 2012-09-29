package org.tdar.struts.action;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.CategoryVariable;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.InformationResourceFileVersion.VersionType;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.parser.CodingSheetParserException;
import org.tdar.struts.data.FileProxy;
import org.tdar.transform.DcTransformer;
import org.tdar.transform.ModsTransformer;

/**
 * $Id$
 * 
 * <p>
 * Manages requests to create/delete/edit an CodingSheet and its associated metadata.
 * </p>
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@ParentPackage("secured")
@Component
@Scope("prototype")
@Namespace("/coding-sheet")
public class CodingSheetController extends AbstractInformationResourceController<CodingSheet> {

    private static final long serialVersionUID = 377533801938016848L;

    private Long categoryId;
    private Long subcategoryId;

    private List<CodingSheet> allSubmittedCodingSheets;

    private List<CategoryVariable> subcategories;

    private ArrayList<Resource> relatedResources;
    
    @Autowired
    private transient ModsTransformer.CodingSheetTransformer codingSheetModsTransformer;
    @Autowired
    private transient DcTransformer.CodingSheetTransformer codingSheetDcTransformer;

    @Override
    protected void loadCustomMetadata() {
        loadInformationResourceProperties();
        CategoryVariable categoryVariable = resource.getCategoryVariable();
        if (categoryVariable != null) {
            if (categoryVariable.getParent() == null) {
                setCategoryId(categoryVariable.getId());
            } else {
                setCategoryId(categoryVariable.getParent().getId());
                setSubcategoryId(categoryVariable.getId());
                loadSubcategories();
            }
        }
        setFileTextInput(getLatestUploadedTextVersionText());
    }

    /**
     * Save basic metadata of the registering concept.
     * 
     * @param concept
     */
    @Override
    protected String save(CodingSheet codingSheet) {
        super.saveBasicResourceMetadata();
        super.saveInformationResourceProperties();
        saveCategories();
        getCodingSheetService().saveOrUpdate(codingSheet);
        handleUploadedFiles();
        // datatables associated with this coding sheet need to be updated
        refreshAssociatedData(codingSheet);
        return SUCCESS;
    }

    // retranslate associated datatables, and recreate translated files
    private void refreshAssociatedData(CodingSheet codingSheet) {
        if (codingSheet.getAssociatedDataTableColumns() != null && codingSheet.getAssociatedDataTableColumns().size() > 0) {
            getDatasetService().translate(codingSheet.getAssociatedDataTableColumns(), codingSheet);
            List<DataTable> dataTables = getDataTableService().findDataTablesUsingResource(resource);
            for (DataTable dataTable : dataTables) {
                getDatasetService().createTranslatedFile(dataTable.getDataset());
            }
        }
    }

    private void saveCategories() {
        if (subcategoryId == null || subcategoryId == -1L) {
            resource.setCategoryVariable(getCategoryVariableService().find(categoryId));
        } else {
            resource.setCategoryVariable(getCategoryVariableService().find(subcategoryId));
        }
    }

    @Override
    protected CodingSheet loadResourceFromId(Long resourceId) {
        CodingSheet codingSheet = getCodingSheetService().find(resourceId);
        if (codingSheet != null) {
            setProject(codingSheet.getProject());
        }
        return codingSheet;
    }

    @Override
    protected FileProxy createUploadedFileProxy(String fileTextInput) throws UnsupportedEncodingException {
        String filename = resource.getTitle() + ".csv";
        // ensure csv conversion
        return new FileProxy(filename, new ByteArrayInputStream(fileTextInput.getBytes("UTF-8")), VersionType.UPLOADED);
    }


    @Override
    protected void processUploadedFile() throws IOException {
        // 1. save metadata for coding sheet file
        // 1.1 Create CodingSheet object, and save the metadata
        Collection<InformationResourceFileVersion> files = resource.getLatestVersions(VersionType.UPLOADED);
        getLogger().debug("processing uploaded coding sheet files: {}", files);

        if (files.size() != 1) {
            getLogger().warn("Unexpected number of files associated with this coding sheet, expected 1 got " + files.size());
            return;
        }

        /*
         * two cases, either:
         * 1) 1 file uploaded (csv | tab | xls)
         * 2) tab entry into form (2 files uploaded 1 archival, 2 not)
         */

        InformationResourceFileVersion toProcess = files.iterator().next();
        if (files.size() > 1) {
            for (InformationResourceFileVersion file : files) {
                if (file.isArchival())
                    toProcess = file;
            }
        }
        // should always be 1 based on the check above
        getLogger().debug("adding coding rules");
        addCodingRules(toProcess.getFilename(), new FileInputStream(toProcess.getFile()));

        getCodingSheetService().saveOrUpdate(resource);
    }

    /**
     * The uploading file stream of coding rules is converted and saved into the
     * database.
     * 
     * Errors caused during parsing are rethrown up to the controller method that
     * can best handle it and return a useful error message to the user.
     * 
     * @param filename
     * @param inputCodingRulesStream
     */
    private void addCodingRules(final String filename, final InputStream inputCodingRulesStream) throws IOException, CodingSheetParserException {
        getCodingSheetService().parseUpload(resource, filename, inputCodingRulesStream);
    }

    /**
     * Returns all coding sheets submitted by the currently authenticated user.
     * 
     * @return all coding sheets submitted by the currently authenticated user.
     */
    public List<CodingSheet> getAllSubmittedCodingSheets() {
        if (allSubmittedCodingSheets == null) {
            allSubmittedCodingSheets = getCodingSheetService().findBySubmitter(getAuthenticatedUser());
        }
        return allSubmittedCodingSheets;
    }

    public List<CategoryVariable> getSubcategories() {
        if (subcategories == null) {
            loadSubcategories();
        }
        return subcategories;
    }

    private void loadSubcategories() {
        if (categoryId == null) {
            subcategories = Collections.emptyList();
        }
        subcategories = getCategoryVariableService().findAllSubcategories(categoryId);
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * Injected by the form.
     * 
     * @param subcategoryId
     */
    public void setSubcategoryId(Long subcategoryId) {
        this.subcategoryId = subcategoryId;
    }

    @Override
    protected CodingSheet createResource() {
        return new CodingSheet();
    }

    /**
     * Get the current concept.
     * 
     * @return
     */
    public CodingSheet getCodingSheet() {
        return resource;
    }

    public void setCodingSheet(CodingSheet codingSheet) {
        this.resource = codingSheet;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Long getSubcategoryId() {
        return subcategoryId;
    }

    public List<Resource> getRelatedResources() {
        relatedResources = new ArrayList<Resource>();
        for (DataTable table : getDataTableService().findDataTablesUsingResource(resource)) {
            relatedResources.add(table.getDataset());
        }
        return relatedResources;
    }

    @Override
    public String deleteCustom() {
        List<Resource> related = getRelatedResources();
        if (related.size() > 0) {
            String titles = StringUtils.join(related, ',');
            String message = "please remove the mappings before deleting: " + titles;
            addActionErrorWithException("this resource is still mapped to the following datasets", new TdarRecoverableRuntimeException(message));
            return ERROR;
        }
        return SUCCESS;
    }

    @Override
    public DcTransformer<CodingSheet> getDcTransformer() {
        return codingSheetDcTransformer;
    }

    @Override
    public ModsTransformer<CodingSheet> getModsTransformer() {
        return codingSheetModsTransformer;
    }

    @Override
    public Set<String> getValidFileExtensions() {
        return analyzer.getExtensionsForType(ResourceType.CODING_SHEET);
    }

}
