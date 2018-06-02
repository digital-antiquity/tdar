package org.tdar.core.dao;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.tdar.core.bean.file.AbstractFile;
import org.tdar.core.bean.file.TdarFile;


public enum FileOrder {
    FILENAME,
    DATE_UPLOADED,
    DATE_CURATED,
    DATE_EXTERNAL_REVIEWED,
    DATE_INITIAL_REVIEWED,
    DATE_REVIEWED;

    public void sort(List<AbstractFile> list, FileOrder sort) {
        switch (sort) {
            case DATE_CURATED:
            case DATE_INITIAL_REVIEWED:
            case DATE_EXTERNAL_REVIEWED:
            case DATE_REVIEWED:
            case DATE_UPLOADED:
                Collections.sort(list, new DateComparator(sort));
                break;
            case FILENAME:
                Collections.sort(list, new FilenameComparator());
                break;
        }
    }

    public static class FilenameComparator implements Comparator<AbstractFile> {

        @Override
        public int compare(AbstractFile o1, AbstractFile o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            String t1 = o1.getName();
            String t2 = o2.getName();

            if (t1 == null ^ t2 == null) {
                return (t1 == null) ? -1 : 1;
            }

            if (t1 == null && t2 == null) {
                return 0;
            }

            if (t1.equals(t2)) {
                return o1.getId().compareTo(o2.getId());
            }

            return t1.compareTo(t2);
        }
    }

    public static class DateComparator implements Comparator<AbstractFile> {

        private FileOrder order;

        public DateComparator(FileOrder order) {
            this.order = order;
        }

        @Override
        public int compare(AbstractFile o1, AbstractFile o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            if (o2 == null) {
                return 1;
            }
            Date t1 = getPart(o1, order);
            Date t2 = getPart(o2, order);

            if (t1 == null ^ t2 == null) {
                return (t1 == null) ? -1 : 1;
            }

            if (t1 == null && t2 == null) {
                return 0;
            }

            if (t1.equals(t2)) {
                return o1.getId().compareTo(o2.getId());
            }

            return t1.compareTo(t2);
        }

        private Date getPart(AbstractFile o1, FileOrder order2) {
            if (o1 == null) {
                return null;
            }

            if (order2 == FileOrder.DATE_UPLOADED) {
                return o1.getDateCreated();
            }

            if (o1 instanceof TdarFile) {
                TdarFile f = (TdarFile) o1;
                switch (order2) {
                    case DATE_CURATED:
                        return f.getDateCurated();
                    case DATE_EXTERNAL_REVIEWED:
                        return f.getDateExternalReviewed();
                    case DATE_INITIAL_REVIEWED:
                        return f.getDateInitialReviewed();
                    case DATE_REVIEWED:
                        return f.getDateReviewed();
                    case DATE_UPLOADED:
                        return f.getDateCreated();
                }
            }
            return null;
        }
    }
}
