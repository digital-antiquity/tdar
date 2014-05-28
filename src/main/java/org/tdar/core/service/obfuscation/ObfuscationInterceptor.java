package org.tdar.core.service.obfuscation;

import java.util.concurrent.TimeUnit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.service.ObfuscationService;
import org.tdar.struts.action.AuthenticationAware;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Aspect
@Component
public class ObfuscationInterceptor {

    private ObfuscationService obfuscationService;

    protected transient Logger logger = LoggerFactory.getLogger(getClass());
    private final Cache<Integer, Boolean> seenSet;
    
    @Autowired
    public ObfuscationInterceptor(ObfuscationService obfuscationService) {
        this.obfuscationService = obfuscationService;
        seenSet = CacheBuilder.newBuilder()
                .concurrencyLevel(4)
                .maximumSize(1000)
                .expireAfterWrite(2, TimeUnit.MINUTES)
                .build();
    }

    /*
     * First, match @Action or @Actions on methods and add hashCode to set.  We'll only obfuscate things after the @Action has been called
     */
    @Around("(@annotation(org.apache.struts2.convention.annotation.Action) || @annotation(org.apache.struts2.convention.annotation.Actions))") 
    public Object aroundAction(ProceedingJoinPoint pjp) throws Throwable {
        Object result = pjp.proceed();
        seenSet.put(pjp.getTarget().hashCode(),true);
        return result;
    }
    
    /*
     * Then we wrap all of the getters, if the hashCode is stored in our set, we remove it 
     */
    @Around("execution( * org.tdar.struts.action..get*(..)) && !@annotation(org.tdar.struts.interceptor.annotation.DoNotObfuscate)")
    public Object obfuscate(ProceedingJoinPoint pjp) throws Throwable {
        Boolean done = seenSet.getIfPresent(pjp.getTarget().hashCode());
        Object retVal = pjp.proceed();
        if (TdarConfiguration.getInstance().obfuscationInterceptorDisabled() || obfuscationService.isWritableSession() || done != Boolean.TRUE) {
            return retVal;
        }
        TdarUser user = null;
        if (pjp.getTarget() instanceof AuthenticationAware) {
            user = ((AuthenticationAware)pjp.getTarget()).getAuthenticatedUser();
        }
        obfuscationService.obfuscateObject(retVal, user);
        return retVal;
    }
}
