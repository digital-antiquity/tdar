package org.tdar.utils.jaxb.converters;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.tdar.core.bean.Persistable;

public class JaxbPersistableMapConverter<K extends Persistable, V>
        extends XmlAdapter<JaxbPersistableMapConverter.PersistableMapAdapter<K, V>, Map<K, V>> {

    @XmlType
    @XmlRootElement
    public final static class PersistableMapAdapter<K extends Persistable, V> {

        @XmlElement
        private List<PersistableMapEntry<K, V>> key = new LinkedList<PersistableMapEntry<K, V>>();

        @SuppressWarnings("unused")
        private PersistableMapAdapter() {
        }

        public PersistableMapAdapter(Map<K, V> original) {
            for (Map.Entry<K, V> entry : original.entrySet()) {
                key.add(new PersistableMapEntry<K, V>(entry));
            }
        }

    }

    @XmlRootElement
    public final static class PersistableMapEntry<K extends Persistable, V> {

        @XmlElement
        @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
        private K key;

        @XmlElement
        private V value;

        @SuppressWarnings("unused")
        private PersistableMapEntry() {
        }

        public PersistableMapEntry(Map.Entry<K, V> original) {
            key = original.getKey();
            value = original.getValue();
        }

    }

    @Override
    public Map<K, V> unmarshal(PersistableMapAdapter<K, V> obj) {
        throw new UnsupportedOperationException("unmarshalling is never performed");
    }

    @Override
    public PersistableMapAdapter<K, V> marshal(Map<K, V> arg0) throws Exception {
        // TODO Auto-generated method stub
        return new PersistableMapAdapter<K, V>(arg0);
    }

}
