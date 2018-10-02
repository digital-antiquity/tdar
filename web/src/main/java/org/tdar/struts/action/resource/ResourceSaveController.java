package org.tdar.struts.action.resource;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.InterceptorRef;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.resource.DegreeType;
import org.tdar.core.bean.resource.Document;
import org.tdar.core.bean.resource.DocumentType;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.struts.action.resource.AbstractInformationResourceController;
import org.tdar.struts.interceptor.annotation.HttpsOnly;
import org.tdar.struts_base.action.TdarActionException;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.struts_base.interceptor.annotation.PostOnly;
import org.tdar.struts_base.interceptor.annotation.WriteableSession;

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
@Namespace("/resource")
public class ResourceSaveController extends AbstractInformationResourceController<Document> {

    private static final long serialVersionUID = 1773433343235091835L;
    // incoming data
    private DocumentType documentType;
    private DegreeType degree;

    private List<DocumentType> documentTypes = Arrays.asList(DocumentType.values());

    private List<DegreeType> degrees = Arrays.asList(DegreeType.values());

    
    @Action(value = SAVE,
            interceptorRefs = { @InterceptorRef("editAuthenticatedStack") },
            results = {
                    @Result(name = SUCCESS, type = TdarActionSupport.REDIRECT, location = SAVE_SUCCESS_PATH),
                    @Result(name = SUCCESS_ASYNC, location = "view-async.ftl"),
                    @Result(name = INPUT, type=TdarActionSupport.REDIRECT, location = "/resource/edit?id=${persistable.id}")
            })
    @WriteableSession
    @PostOnly
    @HttpsOnly
    @Override
    /**
     * FIXME: appears to only override the INPUT result type compared to AbstractPersistableController's declaration,
     * see if it's possible to do this with less duplicatiousness
     * 
     * @see org.tdar.struts.action.AbstractPersistableController#save()
     */
    public String save() throws TdarActionException {
        return super.save();
    }


    
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
