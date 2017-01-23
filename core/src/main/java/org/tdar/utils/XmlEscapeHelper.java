package org.tdar.utils;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlEscapeHelper {

    private boolean changed = false;
    private boolean printed = false;
    private Long id = -1L;
    private Set<Character> invalid = new HashSet<>();
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    public XmlEscapeHelper(Long id) {
        this.setId(id);
    }

    /**
     * 
     * http://blog.mark-mclaren.info/2007/02/invalid-xml-characters-when-valid-utf8_5873.html
     * 
     * This method ensures that the output String has only
     * valid XML unicode characters as specified by the
     * XML 1.0 standard. For reference, please see
     * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the
     * standard</a>. This method will return an empty
     * String if the input is null or empty.
     *
     * @param in
     *            The String whose non-valid characters we want to remove.
     * @return The in String, stripped of non-valid characters.
     */
    public String stripNonValidXMLCharacters(String in) {
        StringBuffer out = new StringBuffer(); // Used to hold the output.
        char current; // Used to reference the current character.
        if (in == null) {
            return null;
        }
        
        if ("".equals(in)) {
            return ""; 
        }

        for (int i = 0; i < in.length(); i++) {
            current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
            if ((current == 0x9) ||
                    (current == 0xA) ||
                    (current == 0xD) ||
                    ((current >= 0x20) && (current <= 0xD7FF)) ||
                    ((current >= 0xE000) && (current <= 0xFFFD)) ||
                    ((current >= 0x10000) && (current <= 0x10FFFF))) {
                out.append(current);
            } else {
                invalid.add(current);
                setChanged(true);
            }
        }
        return out.toString();
    }

    public boolean isChanged() {
        return changed;
    }

    private void setChanged(boolean changed) {
        this.changed = changed;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void logChange() {
        if (changed && printed == false) {
            printed = true;
            StringBuffer report = new StringBuffer();
            invalid.forEach(c -> {
                if (report.length() > 0) {
                    report.append(", ");
                }
                report.append(String.format("%04x", (int) c));
            });

            logger.error("tDAR id: {} has utf-8 encoding issues [{}]", id, report);
        }
    }
}
