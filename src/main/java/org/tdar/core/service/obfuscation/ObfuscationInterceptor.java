package org.tdar.core.service.obfuscation;

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

@Aspect
@Component
public class ObfuscationInterceptor {

    private ObfuscationService obfuscationService;

    protected transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public ObfuscationInterceptor(ObfuscationService obfuscationService) {
        logger.debug("hi");
        this.obfuscationService = obfuscationService;
    }

    @Around("execution( * org.tdar.struts.action..get*(..)) && !@annotation(org.tdar.struts.interceptor.annotation.DoNotObfuscate)")
    public Object obfuscate(ProceedingJoinPoint pjp) throws Throwable {
        logger.debug("PROXY!!! {} : {}", pjp.getTarget(), pjp.getSignature() );
        Object retVal = pjp.proceed();
        if (TdarConfiguration.getInstance().obfuscationInterceptorDisabled()) {
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
