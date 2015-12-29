package org.tdar.search.service.query;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.external.AuthorizationService;

public abstract class AbstractSearchService {
    
    @Autowired
    protected AuthorizationService authorizationService;

    @Autowired
    protected AuthenticationService authenticationService;
    
    protected static final transient Logger logger = LoggerFactory.getLogger(SearchService.class);
    protected static final String[] LUCENE_RESERVED_WORDS = new String[] { "AND", "OR", "NOT" };
    protected static final Pattern luceneSantizeQueryPattern = Pattern.compile("(^|\\W)(" + StringUtils.join(LUCENE_RESERVED_WORDS, "|") + ")(\\W|$)");
    public static final int MAX_FTQ_RESULTS = 50_000;

}
