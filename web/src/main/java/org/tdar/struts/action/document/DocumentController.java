package org.tdar.struts.action.document;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.DegreeType;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.DocumentType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.struts.action.resource.AbstractInformationResourceController;

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

    private List<DegreeType> degrees = Arrays.asList(DegreeType.values());

    @Override
    public boolean isMultipleFileUploadEnabled() {
        return true;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public List<DocumentType> getDocumentTypes() {
        return documentTypes;
    }

    public List<DegreeType> getDegrees() {
        return degrees;
    }

    @Override
    public Set<String> getValidFileExtensions() {
        return getAnalyzer().getExtensionsForType(ResourceType.DOCUMENT);
    }

    public void setDocument(Document document) {
        setPersistable(document);
    }

    public Document getDocument() {
        return getPersistable();
    }

    @Override
    public Class<Document> getPersistableClass() {
        return Document.class;
    }

    public DegreeType getDegree() {
        return degree;
    }

    public void setDegree(DegreeType degree) {
        this.degree = degree;
    }

}
