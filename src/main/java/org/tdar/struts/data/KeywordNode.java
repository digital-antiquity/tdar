package org.tdar.struts.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.tdar.core.bean.keyword.HierarchicalKeyword;

/**
 * $Id$
 * 
 * @author <a href="mailto:matt.cordial@asu.edu">Matt Cordial</a>
 * @version $Rev$
 */
public class KeywordNode<K extends HierarchicalKeyword<K>> implements Iterable<KeywordNode<K>> {

    private K keyword;
    private SortedMap<Integer, KeywordNode<K>> children;

    public KeywordNode() {
        this.children = new TreeMap<Integer, KeywordNode<K>>();
    }

    public K getKeyword() {
        return keyword;
    }

    public void setKeyword(K keyword) {
        this.keyword = keyword;
    }

    public SortedMap<Integer, KeywordNode<K>> getChildren() {
        return children;
    }

    public static <K extends HierarchicalKeyword<K>> KeywordNode<K> organizeKeywords(Collection<K> keywords) {

        KeywordNode<K> root = new KeywordNode<K>();
        for (K keyword : keywords) {

            List<Integer> index = new ArrayList<Integer>();

            String indexString = keyword.getIndex();
            if (StringUtils.isBlank(indexString)) {
                continue;
            }
            for (String s : StringUtils.splitPreserveAllTokens(indexString, ".")) {
                index.add(Integer.parseInt(s));
            }

            // find the child node to put the keyword into creating parents
            // sans-keyword where necessary
            KeywordNode<K> targetNode = root;
            for (Integer i : index) {
                if (!targetNode.getChildren().containsKey(i)) {
                    KeywordNode<K> node = new KeywordNode<K>();
                    targetNode.getChildren().put(i, node);
                }
                targetNode = targetNode.getChildren().get(i);
            }
            targetNode.setKeyword(keyword);
        }

        return root;
    }

    public class KeywordNodeIterator implements Iterator<KeywordNode<K>> {

        private Iterator<Entry<Integer, KeywordNode<K>>> entIter;

        public KeywordNodeIterator() {
            this.entIter = children.entrySet().iterator();
        }

        @Override
        public boolean hasNext() {
            return entIter.hasNext();
        }

        @Override
        public KeywordNode<K> next() {
            return entIter.next().getValue();
        }

        @Override
        public void remove() {
            entIter.remove();
        }

    }

    @Override
    public Iterator<KeywordNode<K>> iterator() {
        return this.new KeywordNodeIterator();
    }

}