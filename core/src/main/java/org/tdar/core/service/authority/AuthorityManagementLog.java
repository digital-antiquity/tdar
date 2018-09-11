package org.tdar.core.service.authority;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.tdar.core.bean.Persistable;
import org.tdar.core.bean.entity.Person;
import org.tdar.utils.jaxb.converters.JaxbPersistableMapConverter;

/**
 * Static entry for the XML / bean representation to an entire Log
 * 
 * @author abrin
 * 
 * @param <R>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "log")
public class AuthorityManagementLog<R> {
    private String userDisplayName = "n/a";
    private R authority;
    private Set<R> dupes;
    // map of referrer -> ( [field containing the dupe, id of the dupe] ... )
    private Map<Persistable, AuthorityManagementLogPart> updatedReferrers = new HashMap<Persistable, AuthorityManagementLogPart>();
    private DupeMode dupeMode;

    public AuthorityManagementLog() {
    }

    public AuthorityManagementLog(R authority, Set<R> dupes, Person user, DupeMode dupeMode) {
        this.authority = authority;
        this.dupes = dupes;
        this.setDupeMode(dupeMode);
        this.userDisplayName = String.format("%s (%s)", user.getProperName(), user.getId());
    }

    public void add(Persistable referrer, Field field, Persistable dupe) {
        AuthorityManagementLogPart dupeList = updatedReferrers.get(referrer);
        if (dupeList == null) {
            dupeList = new AuthorityManagementLogPart();
            updatedReferrers.put(referrer, dupeList);
        }
        dupeList.add(field.getName(), dupe.getId());
    }

    /**
     * @return the authority
     */
    public R getAuthority() {
        return authority;
    }

    /**
     * @param authority
     *            the authority to set
     */
    public void setAuthority(R authority) {
        this.authority = authority;
    }

    /**
     * @return the dupes
     */
    public Set<R> getDupes() {
        return dupes;
    }

    /**
     * @param dupes
     *            the dupes to set
     */
    public void setDupes(Set<R> dupes) {
        this.dupes = dupes;
    }

    /**
     * @return the updatedReferrers
     */
    @XmlJavaTypeAdapter(JaxbPersistableMapConverter.class)
    public Map<Persistable, AuthorityManagementLogPart> getUpdatedReferrers() {
        return updatedReferrers;
    }

    @Override
    public String toString() {
        return String.format("Authority: %s, dupes: %s, referrers: %s", authority, dupes, updatedReferrers.values());
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public void setUserDisplayName(String userDisplayName) {
        this.userDisplayName = userDisplayName;
    }

    public DupeMode getDupeMode() {
        return dupeMode;
    }

    public void setDupeMode(DupeMode dupeMode) {
        this.dupeMode = dupeMode;
    }

}