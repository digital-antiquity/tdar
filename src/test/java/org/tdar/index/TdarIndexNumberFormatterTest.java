package org.tdar.index;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.tdar.search.index.TdarIndexNumberFormatter;

public class TdarIndexNumberFormatterTest {

    @Test
    public void testSorting() {

        String first = TdarIndexNumberFormatter.format(-2000);
        String second = TdarIndexNumberFormatter.format(-3);
        String third = TdarIndexNumberFormatter.format(0);
        String fourth = TdarIndexNumberFormatter.format(45);
        String fifth = TdarIndexNumberFormatter.format(90000);

        List<String> sorted = new ArrayList<String>(5);
        sorted.add(third);
        sorted.add(second);
        sorted.add(fifth);
        sorted.add(first);
        sorted.add(fourth);

        Collections.sort(sorted);

        assertEquals(first, sorted.get(0));
        assertEquals(second, sorted.get(1));
        assertEquals(third, sorted.get(2));
        assertEquals(fourth, sorted.get(3));
        assertEquals(fifth, sorted.get(4));
    }

}
