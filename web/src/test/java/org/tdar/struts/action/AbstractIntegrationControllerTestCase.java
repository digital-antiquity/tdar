package org.tdar.struts.action;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.StrutsStatics;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.tdar.core.bean.AbstractIntegrationTestCase;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.permissions.GeneralPermissions;
import org.tdar.core.bean.resource.Resource;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.BookmarkedResourceService;
import org.tdar.core.service.EntityService;
import org.tdar.core.service.GenericService;
import org.tdar.core.service.PersonalFilestoreService;
import org.tdar.core.service.UrlService;
import org.tdar.core.service.collection.ResourceCollectionService;
import org.tdar.core.service.external.AuthenticationService;
import org.tdar.core.service.external.AuthorizationService;
import org.tdar.core.service.external.EmailService;
import org.tdar.core.service.external.session.SessionData;
import org.tdar.core.service.integration.DataIntegrationService;
import org.tdar.core.service.processes.SendEmailProcess;
import org.tdar.core.service.resource.DataTableService;
import org.tdar.core.service.resource.DatasetService;
import org.tdar.core.service.resource.InformationResourceService;
import org.tdar.core.service.resource.ProjectService;
import org.tdar.core.service.resource.ResourceService;
import org.tdar.junit.ControllerTestWatcher;
import org.tdar.search.config.TdarSearchAppConfiguration;
import org.tdar.search.service.index.SearchIndexService;
import org.tdar.search.service.query.SearchService;
import org.tdar.struts_base.ErrorListener;
import org.tdar.struts_base.action.TdarActionSupport;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PersistableUtils;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.DefaultLocaleProviderFactory;
import com.opensymphony.xwork2.LocaleProvider;
import com.opensymphony.xwork2.LocaleProviderFactory;
import com.opensymphony.xwork2.LocalizedTextProvider;
import com.opensymphony.xwork2.StrutsTextProviderFactory;
import com.opensymphony.xwork2.TextProviderFactory;
import com.opensymphony.xwork2.config.ConfigurationManager;
import com.opensymphony.xwork2.config.providers.XWorkConfigurationProvider;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.inject.Context;
import com.opensymphony.xwork2.ognl.OgnlValueStackFactory;
import com.opensymphony.xwork2.util.GlobalLocalizedTextProvider;
import com.opensymphony.xwork2.util.StrutsLocalizedTextProvider;
//import com.opensymphony.xwork2.util.LocalizedTextUtil;
import com.opensymphony.xwork2.util.ValueStack;

@ContextConfiguration(classes = TdarSearchAppConfiguration.class)
@SuppressWarnings("rawtypes")
public abstract class AbstractIntegrationControllerTestCase extends AbstractIntegrationTestCase implements ErrorListener {

    protected HttpServletRequest defaultHttpServletRequest = new MockHttpServletRequest();

    protected HttpServletRequest httpServletRequest = defaultHttpServletRequest;
    protected HttpServletRequest httpServletPostRequest = new MockHttpServletRequest("POST", "/");
    protected HttpServletResponse httpServletResponse = new MockHttpServletResponse();

    protected PlatformTransactionManager transactionManager;
    // private TransactionCallback verifyTransactionCallback;
    // private TransactionTemplate transactionTemplate;

    @Autowired
    protected SessionFactory sessionFactory;
    @Autowired
    protected ProjectService projectService;
    @Autowired
    protected DatasetService datasetService;
    @Autowired
    protected DataTableService dataTableService;
    @Autowired
    protected DataIntegrationService dataIntegrationService;
    @Autowired
    protected GenericService genericService;
    @Autowired
    protected UrlService urlService;
    @Autowired
    protected ResourceService resourceService;
    @Autowired
    protected EntityService entityService;
    @Autowired
    protected InformationResourceService informationResourceService;
    @Autowired
    protected SearchIndexService searchIndexService;
    @Autowired
    protected SearchService searchService;
    @Autowired
    protected BookmarkedResourceService bookmarkedResourceService;
    @Autowired
    protected PersonalFilestoreService filestoreService;
    @Autowired
    protected AuthorizationService authenticationAndAuthorizationService;
    @Autowired
    protected AuthenticationService authenticationService;
    @Autowired
    protected ResourceCollectionService resourceCollectionService;
    @Autowired
    public SendEmailProcess sendEmailProcess;

    @Autowired
    protected EmailService emailService;

    private List<String> actionErrors = new ArrayList<>();
    private boolean ignoreActionErrors = false;
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private SessionData sessionData;

    @Rule
    public TestWatcher failWatcher = new TestWatcher() {

        @Override
        protected void failed(Throwable e, Description description) {
            AbstractIntegrationControllerTestCase.this.onFail(e, description);
        }

    };

    @Rule
    public ControllerTestWatcher testWatcher = new ControllerTestWatcher();

    @Before
    public void announceTestStarting() {
        setIgnoreActionErrors(false);
        getActionErrors().clear();
        searchIndexService.purgeAll();
        searchIndexService.setUseTransactionalEvents(false);
    }

    // Called when your test fails. Did I say "when"? I meant "if".
    public void onFail(Throwable e, Description description) {
    }

    @After
    public void announceTestOver() {
        if (!isIgnoreActionErrors() && CollectionUtils.isNotEmpty(getActionErrors())) {
            logger.error("action errors {}", getActionErrors());
            Assert.fail(
                    String.format("There were %d action errors: \n {} ", getActionErrors().size(), StringUtils.join(getActionErrors().toArray(new String[0]))));
        }

    }

    protected <T> T generateNewController(Class<T> controllerClass) {
        getAuthorizedUserDao().clearUserPermissionsCache();
        // evictCache();

        T controller = applicationContext.getBean(controllerClass);
        if (controller instanceof AbstractAuthenticatableAction) {
            TdarActionSupport tas = (TdarActionSupport) controller;
            tas.setServletRequest(getServletRequest());
            tas.setServletResponse(getServletResponse());
            // set the context
        }
        Map<String, Object> contextMap = new HashMap<String, Object>();
        contextMap.put(StrutsStatics.HTTP_REQUEST, getServletRequest());
        ActionContext context = new ActionContext(contextMap);
        context.setLocale(Locale.getDefault());

        ConfigurationManager configurationManager = new ConfigurationManager(Container.DEFAULT_NAME);
        OgnlValueStackFactory factory = new OgnlValueStackFactory();
        configurationManager.addContainerProvider(new XWorkConfigurationProvider());
        configurationManager.reload();
        Container container = configurationManager.getConfiguration().getContainer();
        container.inject(factory);

        LocalizedTextProvider instance = container.getInstance(LocalizedTextProvider.class);
        applyLocales(instance);
        assertEquals(MessageHelper.getMessage("project.no_associated_project"), instance.findDefaultText("project.no_associated_project", Locale.getDefault()));

        if (controller instanceof ActionSupport) {
            logger.debug("setting container");
            ((ActionSupport) controller).setContainer(container);
        }
        context.setValueStack(factory.createValueStack());
        ActionContext.setContext(context);

        return controller;
    }

    private void applyLocales(LocalizedTextProvider localizedTextProvider) {
        localizedTextProvider.addDefaultResourceBundle("target/maven-shared-archive-resources/Locales/tdar-messages");
        localizedTextProvider.addDefaultResourceBundle("target/Locales/tdar-messages");
        localizedTextProvider.addDefaultResourceBundle("Locales/tdar-messages");
        localizedTextProvider.addDefaultResourceBundle("target/classes/Locales/tdar-messages");
    }

    protected void init(TdarActionSupport controller, TdarUser user) {
        if (controller != null) {
            TdarUser user_ = null;
            controller.setSessionData(getSessionData());

            if ((user != null) && PersistableUtils.isTransient(user)) {
                throw new TdarRecoverableRuntimeException("can't test this way right now, must persist first");
            } else if (user != null) {
                user_ = genericService.find(TdarUser.class, user.getId());
            } else {
                controller.getSessionData().clearAuthenticationToken();
            }
            controller.getSessionData().setTdarUser(user_);
        }
    }

    public <T extends ActionSupport> T generateNewInitializedController(Class<T> controllerClass) {
        return generateNewInitializedController(controllerClass, null);
    }

    public <T extends ActionSupport> T generateNewInitializedController(Class<T> controllerClass, TdarUser user) {
        T controller = generateNewController(controllerClass);
        if (controller instanceof TdarActionSupport) {
            if (user != null) {
                init((TdarActionSupport) controller, user);
            } else {
                init((TdarActionSupport) controller);
            }
            ((TdarActionSupport) controller).registerErrorListener(this);
        }
        return controller;
    }

    protected void init(TdarActionSupport controller) {
        init(controller, getSessionUser());
    }

    protected void initAnonymousUser(TdarActionSupport controller) {
        init(controller, null);
    }

    /* (non-Javadoc)
     * @see org.tdar.struts.action.TestFixtureSetup#getSessionData()
     */
    @Override
    public SessionData getSessionData() {
        if (sessionData == null) {
            this.sessionData = new SessionData();
        }
        return sessionData;
    }

    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    /* (non-Javadoc)
     * @see org.tdar.struts.action.TestFixtureSetup#getServletRequest()
     */
    public HttpServletRequest getServletRequest() {
        return httpServletRequest;
    }

    /* (non-Javadoc)
     * @see org.tdar.struts.action.TestFixtureSetup#getServletPostRequest()
     */
    public HttpServletRequest getServletPostRequest() {
        return httpServletPostRequest;
    }

    public HttpServletRequest getDefaultHttpServletRequest() {
        return defaultHttpServletRequest;
    }

    public void setHttpServletResponse(HttpServletResponse httpServletResponse) {
        this.httpServletResponse = httpServletResponse;
    }

    /* (non-Javadoc)
     * @see org.tdar.struts.action.TestFixtureSetup#getServletResponse()
     */
    public HttpServletResponse getServletResponse() {
        return httpServletResponse;
    }


    /**
     * @param ignoreActionErrors
     *            the ignoreActionErrors to set
     */
    public void setIgnoreActionErrors(boolean ignoreActionErrors) {
        this.ignoreActionErrors = ignoreActionErrors;
    }

    public void ignoreActionErrors(boolean ignoreActionErrors) {
        this.ignoreActionErrors = ignoreActionErrors;
    }

    /**
     * @return the ignoreActionErrors
     */
    public boolean isIgnoreActionErrors() {
        return ignoreActionErrors || testWatcher.isIgnoringActionErrors();
    }

    private TdarUser sessionUser;

    // private static Validator v;

    /**
     * @return
     */
    public TdarUser getSessionUser() {
        if (sessionUser != null) {
            return sessionUser;
        }
        return getUser();
    }

    public void setSessionUser(TdarUser user) {
        this.sessionUser = user;
    }

    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    @Override
    public void addError(String message) {
        getActionErrors().add(message);
        if (!ignoreActionErrors) {
            onActionError(message);
        }
    }

    /**
     * Override if you want granular control over how to handle action errors.
     * 
     * @param message
     */
    public void onActionError(String message) {
    }

    public List<String> getActionErrors() {
        return actionErrors;
    }

    public void setActionErrors(List<String> actionErrors) {
        this.actionErrors = actionErrors;
    }

    @Override
    public EntityService getEntityService() {
        return entityService;
    }
    
    @Override
    public GenericService getGenericService() {
        return genericService;
    }
}
