package org.tdar.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * $Id$
 * 
 * Utility class container for two arbitrary values.
 * 
 * @author <a href='mailto:allen.lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 * @param <R>
 * @param <S>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PROPERTY)
public class Pair<R, S> implements Serializable {

    private static final long serialVersionUID = -2511232129063917716L;
    private R first;
    private S second;

    @SuppressWarnings("unused")
    private Pair() {
    }

    public Pair(R first, S second) {
        setFirst(first);
        setSecond(second);
    }

    public static <T, U> Pair<T, U> create(T first, U second) {
        return new Pair<T, U>(first, second);
    }

    public static <T, U> List<T> allFirsts(List<Pair<T, U>> pairList) {
        ArrayList<T> firsts = new ArrayList<T>();
        for (Pair<T, U> pair : pairList) {
            firsts.add(pair.getFirst());
        }
        return firsts;
    }

    public static <T, U> List<U> allSeconds(List<Pair<T, U>> pairList) {
        ArrayList<U> seconds = new ArrayList<U>();
        for (Pair<T, U> pair : pairList) {
            seconds.add(pair.getSecond());
        }
        return seconds;
    }

    public R getFirst() {
        return first;
    }

    public void setFirst(R first) {
        this.first = first;
    }

    public S getSecond() {
        return second;
    }

    public void setSecond(S second) {
        this.second = second;
    }

    @Override
    public String toString() {
        return String.format("[%s, %s]", first, second);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }

        if (object == this) {
            return true;
        }
        try {
            Pair<R, S> other = (Pair<R, S>) object;
            return new EqualsBuilder().append(first, other.first).append(second, other.second).isEquals();
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 43).append(first).append(second).toHashCode();
    }

}
