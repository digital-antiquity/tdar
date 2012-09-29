package org.tdar.struts.action.resource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.DegreeType;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.DocumentType;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.InformationResourceFile;
import org.tdar.core.bean.resource.ResourceType;

/**
 * $Id$
 * 
 * <p>
 * Manages requests to create/delete/edit a Document and its associated metadata.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@Component
@Scope("prototype")
@ParentPackage("secured")
@Namespace("/document")
public class DocumentController extends AbstractInformationResourceController<Document> {

    private static final long serialVersionUID = -2757927125067486814L;
    // incoming data
    private DocumentType documentType;
    private DegreeType degree;

    private List<DocumentType> documentTypes = Arrays.asList(DocumentType.values());

    private InformationResource linkedInformationResource;

    // either "source" or "related". kind of hacky. should be a better way to do this.
    private String linkType;
    private List<DegreeType> degrees = Arrays.asList(DegreeType.values());

    @Actions({
            @Action("related"),
            @Action("source")
    })
    @Override
    public String execute() {
        if (isNullOrNew()) {
            logger.warn("trying to link related/source document with null or new resource.");
            return REDIRECT_HOME;
        }
        return SUCCESS;
    }

    @Override
    public boolean isMultipleFileUploadEnabled() {
        return true;
    }

    protected void processUploadedFiles(List<InformationResourceFile> uploadedFiles) throws IOException {
        // XXX: currently nothing needs to be done to process uploaded documents
        // so this is a no-op.
        return;
    }

    @Override
    protected void loadCustomMetadata() {
        super.loadCustomMetadata();
    }

    @Override
    protected String save(Document document) {
        // save basic metadata
        super.saveBasicResourceMetadata();
        super.saveInformationResourceProperties();
        getGenericService().saveOrUpdate(document);
        handleUploadedFiles();
        // handleLinkedInformationResource();
        getGenericService().saveOrUpdate(document);
        return SUCCESS;

    }

    // private void handleLinkedInformationResource() {
    // if (linkedInformationResource == null)
    // return;
    // if (StringUtils.isBlank(linkType))
    // return;
    // logger.debug("linking information resource: " + linkedInformationResource.getTitle() + " as " + linkType + " with " + getPersistable().getTitle());
    // if (linkType.equals("related")) {
    // linkedInformationResource.getRelatedCitations().add(getPersistable());
    // } else if (linkType.equals("source")) {
    // linkedInformationResource.getSourceCitations().add(getPersistable());
    // }
    // getInformationResourceService().saveOrUpdate(linkedInformationResource);
    // }

    public void setLinkedResourceId(Long informationResourceId) {
        if (informationResourceId == null) {
            return;
        }
        linkedInformationResource = getInformationResourceService().find(informationResourceId);
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public InformationResource getLinkedInformationResource() {
        return linkedInformationResource;
    }

    public List<DocumentType> getDocumentTypes() {
        return documentTypes;
    }

    public List<DegreeType> getDegrees() {
        return degrees;
    }

    public String getLinkType() {
        return linkType;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    public Set<String> getValidFileExtensions() {
        return analyzer.getExtensionsForType(ResourceType.DOCUMENT);
    }

    public void setDocument(Document document) {
        setPersistable(document);
    }

    public Document getDocument() {
        return getPersistable();
    }

    public Class<Document> getPersistableClass() {
        return Document.class;
    }

    public DegreeType getDegree() {
        return degree;
    }

    public void setDegree(DegreeType degree) {
        this.degree = degree;
    }
    
    @Action("file-upload-test")
    public String fileUploadTest() {
        return SUCCESS;
    }
}
