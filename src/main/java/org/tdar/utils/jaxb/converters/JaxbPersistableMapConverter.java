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
                getKey().add(new PersistableMapEntry<K, V>(entry));
            }
        }

        public List<PersistableMapEntry<K, V>> getKey() {
            return key;
        }

        public void setKey(List<PersistableMapEntry<K, V>> key) {
            this.key = key;
        }

    }

    @XmlRootElement
    public final static class PersistableMapEntry<K extends Persistable, V> {

        @XmlElement
        @XmlJavaTypeAdapter(JaxbPersistableConverter.class)
        private  K key;

        @XmlElement
        private V value;

        @SuppressWarnings("unused")
        private PersistableMapEntry() {
        }

        public PersistableMapEntry(Map.Entry<K, V> original) {
            setKey(original.getKey());
            setValue(original.getValue());
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public void setKey(K key) {
            this.key = key;
        }

    }

    @Override
    public Map<K, V> unmarshal(PersistableMapAdapter<K, V> obj) {
        throw new UnsupportedOperationException("unmarshalling is never performed");
    }

    @Override
    public PersistableMapAdapter<K, V> marshal(Map<K, V> arg0) throws Exception {
        // TODO Auto-generated method stub
        return new PersistableMapAdapter<K, V>((Map<K, V>) arg0);
    }

}
