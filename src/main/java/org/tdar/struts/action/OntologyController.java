package org.tdar.struts.action;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
import org.tdar.core.bean.resource.InformationResourceFileVersion.VersionType;
import org.tdar.core.bean.resource.Ontology;
import org.tdar.core.bean.resource.OntologyNode;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.bean.resource.ResourceType;
import org.tdar.core.bean.resource.dataTable.DataTable;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.struts.data.FileProxy;
import org.tdar.transform.DcTransformer;
import org.tdar.transform.ModsTransformer;

/**
 * $Id$
 * <p>
 * Manages CRUD requests for OntologyS.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
@ParentPackage("secured")
@Namespace("/ontology")
@Component
@Scope("prototype")
public class OntologyController extends AbstractInformationResourceController<Ontology> {

    private static final long serialVersionUID = 4320412741803278996L;

    private List<Ontology> allSubmittedOntologies;

    private Long categoryId;
    private Long subcategoryId;

    private List<CategoryVariable> subcategories;
    private List<Resource> relatedResources;

    @Autowired
    private transient ModsTransformer.OntologyTransformer ontologyModsTransformer;
    @Autowired
    private transient DcTransformer.OntologyTransformer ontologyDcTransformer;

    // FIXME: duplicated with CodingSheetController
    @Override
    protected void loadCustomMetadata() {
        loadInformationResourceProperties();
        CategoryVariable categoryVariable = resource.getCategoryVariable();
        // abort if no category variable has been set
        if (categoryVariable != null) {
            // if the parent is null, just set the base category
            if (categoryVariable.getParent() == null) {
                setCategoryId(categoryVariable.getId());
            }
            // FIXME: assumes a strictly 1-level hierarchy
            else {
                setCategoryId(categoryVariable.getParent().getId());
                setSubcategoryId(categoryVariable.getId());
                loadSubcategories();
            }
        }
        setFileTextInput(getLatestUploadedTextVersionText());
    }

    @Override
    protected FileProxy createUploadedFileProxy(String fileTextInput) throws UnsupportedEncodingException {
        String filename = resource.getTitle() + ".owl";
        // convert text input to OWL XML text and use that as our archival version
        String owlXml = getOntologyService().toOwlXml(resource.getId(), fileTextInput);
        getLogger().trace("owl xml is: \n{}", owlXml);
        return new FileProxy(filename,
                new ByteArrayInputStream(owlXml.getBytes("UTF-8")),
                VersionType.UPLOADED);

    }

    @Override
    protected void processUploadedFile() throws IOException {
        // after the file has already been generated, etc.
        getOntologyService().shred(resource);
        saveCategories();
        getOntologyService().saveOrUpdate(resource);
    }

    /**
     * Sets the various pieces of metadata on this ontology and then saves it.
     * 
     * 
     * @param ontology
     */
    @Override
    protected String save(Ontology ontology) {
        super.saveBasicResourceMetadata();
        super.saveInformationResourceProperties();

        saveCategories();
        getOntologyService().saveOrUpdate(ontology);
        handleUploadedFiles();
        return SUCCESS;
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

    private void saveCategories() {
        getLogger().debug("category id: {}, subcategoryId: {} ", categoryId, subcategoryId);
        if (subcategoryId == null || subcategoryId == -1L) {
            getLogger().debug("Saving category: {}", categoryId);
            resource.setCategoryVariable(getCategoryVariableService().find(categoryId));
        } else {
            resource.setCategoryVariable(getCategoryVariableService().find(subcategoryId));
        }
    }

    @Override
    protected Ontology loadResourceFromId(Long ontologyId) {
        Ontology ontology = getOntologyService().find(ontologyId);
        if (ontology != null) {
            setProject(ontology.getProject());
        }

        return ontology;
    }

    /**
     * Returns all ontologies submitted by this user.
     * 
     * @return all ontologies submitted by this user
     */
    public List<Ontology> getAllSubmittedOntologies() {
        if (allSubmittedOntologies == null) {
            allSubmittedOntologies = getOntologyService().findBySubmitter(getAuthenticatedUser());
        }
        return allSubmittedOntologies;
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

    @Override
    protected Ontology createResource() {
        return new Ontology();
    }

    public Ontology getOntology() {
        return resource;
    }

    public void setOntology(Ontology ontology) {
        this.resource = ontology;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getSubcategoryId() {
        return subcategoryId;
    }

    public void setSubcategoryId(Long subcategoryId) {
        this.subcategoryId = subcategoryId;
    }

    public List<Resource> getRelatedResources() {
        relatedResources = new ArrayList<Resource>();
        for (DataTable table : getDataTableService().findDataTablesUsingResource(resource)) {
            relatedResources.add(table.getDataset());
        }
        return relatedResources;
    }

    @Override
    public DcTransformer<Ontology> getDcTransformer() {
        return ontologyDcTransformer;
    }

    @Override
    public ModsTransformer<Ontology> getModsTransformer() {
        return ontologyModsTransformer;
    }

    @Override
    public Set<String> getValidFileExtensions() {
        return analyzer.getExtensionsForType(ResourceType.ONTOLOGY);
    }

    public List<OntologyNode> getRootElements() {
        return getOntologyService().getRootElements(resource.getOntologyNodes());
    }

    public List<OntologyNode> getChildElements(OntologyNode node) {
        logger.trace("get children:" + node);
        return getOntologyService().getChildren(resource.getOntologyNodes(), node);
    }

    public List<OntologyNode> getChildElements(String index) {
        logger.trace("get children:" + index);
        for (OntologyNode node : resource.getOntologyNodes()) {
            if (node.getIndex().equals(index))
                return getOntologyService().getChildren(resource.getOntologyNodes(), node);
        }
        return null;
    }
}
