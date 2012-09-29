package org.tdar.struts.data;

import java.io.Serializable;
import java.util.List;

import org.tdar.core.bean.resource.OntologyNode;

/**
 * $Id$
 * 
 *
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public class IntegrationRowData implements Serializable {
    
    private static final long serialVersionUID = 1948189693155234691L;

    private Long id; 
    
    private List<String> dataValues;
    
    private List<OntologyNode> ontologyValues;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<String> getDataValues() {
        return dataValues;
    }

    public void setDataValues(List<String> dataValues) {
        this.dataValues = dataValues;
    }

    public List<OntologyNode> getOntologyValues() {
        return ontologyValues;
    }

    public void setOntologyValues(List<OntologyNode> ontologyValues) {
        this.ontologyValues = ontologyValues;
    }



}