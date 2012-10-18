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
        protected List<SimpleMapEntry<K, V>> key = new LinkedList<SimpleMapEntry<K, V>>();

        private SimpleMapAdapter() {
        }

        public SimpleMapAdapter(Map<K, V> original) {
            for (Map.Entry<K, V> entry : original.entrySet()) {
                key.add(new SimpleMapEntry<K, V>(entry));
            }
        }

    }

    @XmlType
    @XmlRootElement
    public final static class SimpleMapEntry<K, V> {

        @XmlElement
        protected K key;

        @XmlElement
        protected V value;

        private SimpleMapEntry() {
        }

        public SimpleMapEntry(Map.Entry<K, V> original) {
            key = original.getKey();
            value = original.getValue();
        }

    }

    @Override
    public Map<K, V> unmarshal(SimpleMapAdapter<K, V> obj) {
        throw new UnsupportedOperationException("unmarshalling is never performed");
    }

    @Override
    public SimpleMapAdapter<K, V> marshal(Map<K, V> arg0) throws Exception {
        // TODO Auto-generated method stub
        return new SimpleMapAdapter<K, V>(arg0);
    }

}
