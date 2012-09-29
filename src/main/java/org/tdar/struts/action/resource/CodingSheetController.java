package org.tdar.struts.action.resource;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.CodingSheet;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.InformationResourceFileVersion;
import org.tdar.core.bean.resource.InformationResourceFileVersion.VersionType;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.parser.CodingSheetParserException;
import org.tdar.struts.data.FileProxy;

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
public class CodingSheetController extends AbstractSupportingInformationResourceController<CodingSheet> {

    private static final long serialVersionUID = 377533801938016848L;

    private List<CodingSheet> allSubmittedCodingSheets;

    /**
     * Save basic metadata of the registering concept.
     * 
     * @param concept
     */
    @Override
    protected String save(CodingSheet codingSheet) {
        super.saveBasicResourceMetadata();
        super.saveInformationResourceProperties();
        super.saveCategories();
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
            List<DataTable> dataTables = getDataTableService().findDataTablesUsingResource(getPersistable());
            for (DataTable dataTable : dataTables) {
                getDatasetService().createTranslatedFile(dataTable.getDataset());
            }
        }
    }

    @Override
    protected FileProxy createUploadedFileProxy(String fileTextInput) throws UnsupportedEncodingException {
        String filename = getPersistable().getTitle() + ".csv";
        // ensure csv conversion
        return new FileProxy(filename, new ByteArrayInputStream(fileTextInput.getBytes("UTF-8")), VersionType.UPLOADED);
    }

    @Override
    protected void processUploadedFiles(List<InformationResourceFile> uploadedFiles) throws IOException {
        // 1. save metadata for coding sheet file
        // 1.1 Create CodingSheet object, and save the metadata
        Collection<InformationResourceFileVersion> files = getPersistable().getLatestVersions(VersionType.UPLOADED);
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

        getCodingSheetService().saveOrUpdate(getPersistable());
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
        getCodingSheetService().parseUpload(getPersistable(), filename, inputCodingRulesStream);
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

    /**
     * Get the current concept.
     * 
     * @return
     */
    public CodingSheet getCodingSheet() {
        return getPersistable();
    }

    public void setCodingSheet(CodingSheet codingSheet) {
        this.setPersistable(codingSheet);
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
    public Set<String> getValidFileExtensions() {
        return analyzer.getExtensionsForType(ResourceType.CODING_SHEET);
    }

    public Class<CodingSheet> getPersistableClass() {
        return CodingSheet.class;
    }
}
