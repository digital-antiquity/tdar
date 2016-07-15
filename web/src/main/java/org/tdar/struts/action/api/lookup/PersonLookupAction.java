package org.tdar.struts.action.api.lookup;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.struts.action.AbstractLookupController;
import org.tdar.utils.PersistableUtils;

/**
 * $Id$
 * <p>
 * Handles ajax requests for people
 * 
 * @version $Rev$
 */
@Namespace("/api/lookup")
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

    @Action(value = "person", results = {
            @Result(name = SUCCESS, type = JSONRESULT, params = { "stream", "jsonInputStream" })
    })
    public String lookupPerson() throws SolrServerException, IOException {
        setMode("personLookup");
        String email_ = null;
        // security -- don't allow searching for email address matches if you're not logged in
        if (PersistableUtils.isNotNullOrTransient(getAuthenticatedUser())) {
            email_ = email;
        }
        Person person = new Person(firstName, lastName,email_);
        if (StringUtils.isNotBlank(institution)) {
            Institution inst = new Institution(institution);
            person.setInstitution(inst);
        }
        
        return findPerson(term, person, Boolean.parseBoolean(registered));
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

}
