package org.tdar.struts.action.lookup;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.search.query.FacetGroup;
import org.tdar.struts.action.AbstractLookupController;

/**
 * $Id$
 * <p>
 * Handles ajax requests for people
 * 
 * @version $Rev$
 */
@Namespace("/lookup")
@ParentPackage("default")
@Component
@Scope("prototype")
public class PersonLookupAction extends AbstractLookupController<Person> {

    private static final long serialVersionUID = 8578701377110641153L;

    private String firstName;
    private String lastName;
    private String institution;
    private String email;
    private String registered;
    private String term;

    @Autowired
    private transient AuthorizationService authorizationService;

    @Action(value = "person", results = {
            @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "jsonInputStream" })
    })
    public String lookupPerson() throws SolrServerException, IOException {
        setMode("personLookup");
        return findPerson(firstName, term, lastName, institution, email, registered);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = StringUtils.trim(firstName);
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = StringUtils.trim(lastName);
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = StringUtils.trim(institution);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = StringUtils.trim(email);
    }

    public String getRegistered() {
        return registered;
    }

    public void setRegistered(String registered) {
        this.registered = registered;
    }

    /**
     * @param term
     *            the term to set
     */
    public void setTerm(String term) {
        this.term = StringUtils.trim(term);
    }

    /**
     * @return the term
     */
    public String getTerm() {
        return term;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<FacetGroup<? extends Enum>> getFacetFields() {
        return null;
    }

}
