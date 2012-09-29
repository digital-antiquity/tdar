package org.tdar.struts.action;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.DocumentType;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.transform.DcTransformer;
import org.tdar.transform.ModsTransformer;

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

    private List<DocumentType> documentTypes = Arrays.asList(DocumentType.values());

    private InformationResource linkedInformationResource;

    // either "source" or "related". kind of hacky. should be a better way to do this.
    private String linkType;

    @Autowired
    private transient ModsTransformer.DocumentTransformer documentModsTransformer;

    @Autowired
    private transient DcTransformer.DocumentTransformer documentDcTransformer;

    @Actions({
            @Action("related"),
            @Action("source")
    })
    @Override
    public String execute() {
        if (isNullOrNewResource()) {
            logger.warn("trying to link related/source document with null or new resource.");
            return REDIRECT_HOME;
        }
        return SUCCESS;
    }

    @Override
    public boolean isMultipleFileUploadEnabled() {
        return true;
    }

    protected void processUploadedFile() throws IOException {
        // XXX: currently nothing needs to be done to process uploaded documents
        // so this is a no-op.
        return;
    }

    @Override
    protected void loadCustomMetadata() {
        super.loadCustomMetadata();
        loadInformationResourceProperties();
        loadResourceProviderInformation();
    }

    @Override
    protected Document createResource() {
        return new Document();
    }

    @Override
    protected String save(Document document) {
        // save basic metadata
        super.saveBasicResourceMetadata();
        super.saveInformationResourceProperties();
        getGenericService().saveOrUpdate(document);
        handleUploadedFiles();
        handleLinkedInformationResource();
        getGenericService().saveOrUpdate(document);
        return SUCCESS;
    }

    private void handleLinkedInformationResource() {
        if (linkedInformationResource == null)
            return;
        if (StringUtils.isBlank(linkType))
            return;
        logger.debug("linking information resource: " + linkedInformationResource.getTitle() + " as " + linkType + " with " + resource.getTitle());
        if (linkType.equals("related")) {
            linkedInformationResource.getRelatedCitations().add(resource);
        } else if (linkType.equals("source")) {
            linkedInformationResource.getSourceCitations().add(resource);
        }
        getInformationResourceService().saveOrUpdate(linkedInformationResource);
    }

    @Override
    protected Document loadResourceFromId(Long documentId) {
        if (documentId == -1L) {
            logger.debug("XXX: tried to loadResourceFromId with -1, returning new document");
            return new Document();
        }
        Document document = getGenericService().find(Document.class, documentId);
        if (document != null) {
            setProject(document.getProject());
        }
        return document;
    }

    public Document getDocument() {
        return resource;
    }

    public void setDocument(Document document) {
        this.resource = document;
    }

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

    public String getLinkType() {
        return linkType;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    @Override
    public DcTransformer<Document> getDcTransformer() {
        return documentDcTransformer;
    }

    @Override
    public ModsTransformer<Document> getModsTransformer() {
        return documentModsTransformer;
    }

    public Set<String> getValidFileExtensions() {
        return analyzer.getExtensionsForType(ResourceType.DOCUMENT);
    }
}
