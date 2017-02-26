package org.tdar.utils;

import java.util.Comparator;

import org.tdar.core.bean.HasName;
import org.tdar.core.bean.Sortable;

public class TitleSortComparator implements Comparator<HasName> {

        @Override
        public int compare(HasName o1, HasName o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            String t1 = Sortable.getTitleSort(o1.getName());
            String t2 = Sortable.getTitleSort(o2.getName());
            return t1.compareTo(t2);
        }
}
