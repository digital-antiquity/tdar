package org.tdar.core.bean.util;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

public class UrlUtils implements Serializable{

    private static final long serialVersionUID = -6368702426236211043L;

    private static final String KEYWORD_SLUG_REGEXP = "[^(A-Za-z0-9)]";

    public static String slugify(String input) {
        if (StringUtils.isBlank(input)) {
            return input;
        }
        return input.replaceAll(KEYWORD_SLUG_REGEXP, "-").replaceAll("\\--*","-").replaceAll("(\\-)+$", "");
        
    }
}
