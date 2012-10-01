package org.tdar.core.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.exception.TdarRuntimeException;
import org.tdar.search.query.QueryFieldNames;

/**
 * $Id$
 * 
 * Marks all classes that can be persisted to the database via our ORM.
 * 
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Revision$
 */
public interface Persistable extends Serializable {

    public Long getId();

    public void setId(Long number);

    /**
     * Returns the list of property objects used for equality comparison and
     * hashCode generation.
     */
    public List<?> getEqualityFields();

    /**
     * Convenience base class for Persistable entities providing JPA annotated
     * fields for ID and a property-aware equals()/hashCode() implementations.
     */
    @MappedSuperclass
    @XmlAccessorType(XmlAccessType.PROPERTY)
    @XmlType(name = "base")
    public abstract static class Base extends JsonModel.Base implements Persistable {

        private static final long serialVersionUID = -458438238558572364L;

        protected final static String[] DEFAULT_JSON_PROPERTIES = { "id" };

        @Transient
        protected final transient Logger logger = LoggerFactory.getLogger(getClass());

        /**
         * Uses GenerationType.IDENTITY, which translates to the (big)serial column type for
         * hibernate+postgres, i.e., one sequence table per entity type
         */
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id = -1L;

        // @XmlTransient
        @Field(store = Store.YES, analyzer = @Analyzer(impl = KeywordAnalyzer.class), name = QueryFieldNames.ID)
        @XmlAttribute(name = "id")
        public Long getId() {
            return id;
        }

        // @XmlID
        // public String getXmlId() {
        // return getId().toString();
        // }

        public void setId(Long id) {
            this.id = id;
        }

        @XmlTransient
        public boolean isTransient() {
            return isTransient(this);
        }

        /**
         * Returns true if:
         * <ol>
         * <li>object identity holds
         * <li>both object types are consistent and the ids for both this persistable object and the incoming persistable object are .equals()
         * <li>OR both object types are consistent and all of the class-specific equality fields specified in getEqualityFields() are .equals()
         * </ol>
         */
        @Override
        public boolean equals(final Object object) {
            if (object == this) {
                return true;
            }
            if (object instanceof Persistable && getClass().isInstance(object)) {
                return isEqual(this, getClass().cast(object));
            }
            return false;
        }

        private transient int hashCode = -1;

        /**
         * Returns a sensible hashCode() for persisted objects. For transient/unsaved objects, uses
         * the default Object.hashCode().
         */
        @Override
        public int hashCode() {
            Logger logger = LoggerFactory.getLogger(getClass());
            Object[] obj = { hashCode, getClass().getSimpleName(), getId() };
            if (hashCode == -1) {
                if (isNullOrTransient(this)) {
                    hashCode = super.hashCode();
                }
                hashCode = toHashCode(this);

                logger.trace("setting hashCode to {} ({}) {}", obj);
            } else {
                logger.trace("returning existing hashCode to {} ({}) {}", obj);
            }
            return hashCode;
        }

        /**
         * Should only be invoked after performing basic checks within your equals(Object) method to ensure that it's not null and equivalent types.
         * 
         * Returns true if
         * <ol>
         * <li>the two ids for both persistables are equal OR
         * <li>each element in the class specific List returned by getEqualityFields() are equal
         * </ol>
         * 
         * NOTE: if the two ids for both persistables are equal, the rest of the equality fields *should* also be equal, otherwise
         * we will run into problems with hashCode().
         * 
         * Returns false otherwise.
         * 
         * @param <T>
         * @param a
         * @param b
         * @return
         */
        public static boolean isEqual(Persistable a, Persistable b) {

            if (a == null || b == null) {
                return false;
            }

            Logger logger = LoggerFactory.getLogger(a.getClass());

            if (isTransient(a) && isTransient(b)) {
                // if both objects are transient then these two objects aren't safely comparable outside of
                // object pointer equality, which should already have been tested via equals(Object).
                return false;
            }

            // short-circuit when ids are equal.
            if (ObjectUtils.equals(a.getId(), b.getId())) {
                return true;
            }

            EqualsBuilder equalsBuilder = new EqualsBuilder();
            List<?> selfEqualityFields = a.getEqualityFields();
            List<?> candidateEqualityFields = b.getEqualityFields();
            logger.trace(String.format("comparing %s with %s", selfEqualityFields, candidateEqualityFields));
            if (CollectionUtils.isEmpty(selfEqualityFields) || selfEqualityFields.size() != candidateEqualityFields.size()) {
                logger.warn("empty or mismatched equivalence fields for " + a.getClass() + ": " + selfEqualityFields + "<-->" + candidateEqualityFields);
                return false;
            }
            for (int i = 0; i < selfEqualityFields.size(); i++) {
                equalsBuilder.append(selfEqualityFields.get(i), candidateEqualityFields.get(i));
            }
            return equalsBuilder.isEquals();
        }

        public static <P extends Persistable> Map<Long, P> createIdMap(Collection<P> items) {
            Map<Long, P> map = new HashMap<Long, P>();
            for (P item : items) {
                if (item == null)
                    continue;
                map.put(item.getId(), item);
            }
            return map;
        }

        public static int toHashCode(Persistable persistable) {
            HashCodeBuilder builder = new HashCodeBuilder(23, 37);
            builder.append(persistable.getEqualityFields().toArray());
            // FIXME: make sure this doesn't break the contract wrt equals.
            return builder.toHashCode();
        }

        /**
         * By default, use only the database ID as the implementation
         */
        @Override
        @XmlTransient
        public List<?> getEqualityFields() {
            return Arrays.asList(getId());
        }

        /*
         * Adds the contents of the collection to the set, and removes anything was not in the incoming collections (intersection) + add all
         * 
         * @return boolean representing whether the exiting set was changed in any way
         */
        public static <C> boolean reconcileSet(Set<C> existing, Collection<C> incoming) {
            if (existing == null) {
                throw new TdarRuntimeException("the existing collection should not be null");
            }
            if (incoming == null) {
                if (!CollectionUtils.isEmpty(existing)) {
                    existing.clear();
                    return true;
                }
                return false;
            }

            boolean changedRetain = existing.retainAll(incoming);
            boolean changedAddAll = existing.addAll(incoming);

            return (changedRetain || changedAddAll);
        }

        public static boolean isNotTransient(Persistable persistable) {
            return !isTransient(persistable);
        }

        public static boolean isNotNullOrTransient(Persistable persistable) {
            return !isNullOrTransient(persistable);
        }

        public static boolean isNotNullOrTransient(Number persistable) {
            return !isNullOrTransient(persistable);
        }

        public static boolean isTransient(Persistable persistable) {
            // object==primative only works for certain primative values (see http://stackoverflow.com/a/3815760/103814)
            return persistable.getId() == null || isNullOrTransient(persistable.getId());
        }

        public static boolean isNullOrTransient(Persistable persistable) {
            return persistable == null || isTransient(persistable);
        }

        public static boolean isNullOrTransient(Number val) {
            return val == null || val.longValue() == -1L;
        }

        @Override
        protected String[] getIncludedJsonProperties() {
            return DEFAULT_JSON_PROPERTIES;
        }

        public static <T extends Persistable> List<Long> extractIds(Collection<T> persistables) {
            List<Long> ids = new ArrayList<Long>();
            for (T persistable : persistables) {
                if (persistable != null) {
                    ids.add(persistable.getId());
                } else {
                    ids.add(null);
                }
            }
            return ids;
        }

        public static <T extends Persistable> List<Long> extractIds(Collection<T> persistables, int max) {
            List<Long> ids = new ArrayList<Long>();
            int count = 0;
            for (T persistable : persistables) {
                if (persistable != null) {
                    ids.add(persistable.getId());
                } else {
                    ids.add(null);
                }
                count++;
                if (count == max)
                    break;
            }
            return ids;
        }

    }

    @MappedSuperclass
    public abstract static class Sequence<E extends Sequence<E>> extends Persistable.Base implements Sequenceable<E> {
        private static final long serialVersionUID = -2667067170953144064L;

        @Column(name = "sequence_number")
        protected Integer sequenceNumber = 0;

        @Override
        public final int compareTo(E other) {
            if (sequenceNumber == null || other.sequenceNumber == null) {
                return 0;
            }
            return sequenceNumber.compareTo(other.sequenceNumber);
        }

        @XmlAttribute
        public Integer getSequenceNumber() {
            if (sequenceNumber == null) {
                setSequenceNumber(0);
            }
            return sequenceNumber;
        }

        public void setSequenceNumber(Integer sequenceNumber) {
            this.sequenceNumber = sequenceNumber;
        }

        /**
         * set the sequence number for the elements in a list based
         * 
         * @param collection
         */
        public static <T extends Sequenceable<T>> void applySequence(Collection<T> collection) {
            int sequenceNumber = 1;
            for (Sequenceable<T> item : collection) {
                if (item == null) {
                    Logger logger = LoggerFactory.getLogger(Sequenceable.class);
                    logger.debug("null sequenceable found in collection -- skipping");
                    continue;
                }
                item.setSequenceNumber(sequenceNumber);
                sequenceNumber++;
            }
        }

    }

}
