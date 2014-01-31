package org.tdar.utils.jaxb.converters;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class JaxbMapConverter<K, V>
        extends XmlAdapter<JaxbMapConverter.SimpleMapAdapter<K, V>, Map<K, V>> {

    @XmlType
    @XmlRootElement
    public final static class SimpleMapAdapter<K, V> {

        @XmlElement
        private List<SimpleMapEntry<K, V>> key = new LinkedList<SimpleMapEntry<K, V>>();

        @SuppressWarnings("unused")
        private SimpleMapAdapter() {
        }

        public SimpleMapAdapter(Map<K, V> original) {
            for (Map.Entry<K, V> entry : original.entrySet()) {
                getKey().add(new SimpleMapEntry<K, V>(entry));
            }
        }

        public List<SimpleMapEntry<K, V>> getKey() {
            return key;
        }

        public void setKey(List<SimpleMapEntry<K, V>> key) {
            this.key = key;
        }

    }

    @XmlType
    @XmlRootElement
    public final static class SimpleMapEntry<K, V> {

        @XmlElement
        private K key;

        @XmlElement
        private V value;

        @SuppressWarnings("unused")
        private SimpleMapEntry() {
        }

        public SimpleMapEntry(Map.Entry<K, V> original) {
            key = original.getKey();
            value = original.getValue();
        }

        public K getKey() {
            return key;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

    }

    @Override
    public Map<K, V> unmarshal(SimpleMapAdapter<K, V> obj) {
        throw new UnsupportedOperationException("unmarshalling is never performed");
    }

    @Override
    public SimpleMapAdapter<K, V> marshal(Map<K, V> arg0) throws Exception {
        return new SimpleMapAdapter<K, V>(arg0);
    }

}
