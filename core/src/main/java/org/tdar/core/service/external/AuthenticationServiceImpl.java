package org.tdar.core.service.external;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tdar.core.bean.AuthNotice;
import org.tdar.core.bean.HasName;
import org.tdar.core.bean.TdarGroup;
import org.tdar.core.bean.entity.AuthorizedUser;
import org.tdar.core.bean.entity.Institution;
import org.tdar.core.bean.entity.Person;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.entity.UserInvite;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.UserNotificationDisplayType;
import org.tdar.core.bean.resource.Status;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.entity.InstitutionDao;
import org.tdar.core.dao.entity.PersonDao;
import org.tdar.core.dao.external.auth.AuthenticationProvider;
import org.tdar.core.dao.external.auth.AuthenticationResult;
import org.tdar.core.dao.external.auth.AuthenticationResultType;
import org.tdar.core.event.EventType;
import org.tdar.core.event.TdarEvent;
import org.tdar.core.exception.TdarAuthorizationException;
import org.tdar.core.exception.TdarRecoverableRuntimeException;
import org.tdar.core.service.UserNotificationService;
import org.tdar.core.service.external.auth.UserLogin;
import org.tdar.core.service.external.auth.UserRegistration;
import org.tdar.core.service.external.session.SessionData;
import org.tdar.utils.MessageHelper;
import org.tdar.utils.PersistableUtils;

@Service
public class AuthenticationServiceImpl implements AuthenticationService  {

    private static final String USER_AGENT = "User-Agent";
    private static final TdarConfiguration CONFIG = TdarConfiguration.getInstance();
    private static final String EMAIL_WELCOME_TEMPLATE = "email-welcome.ftl";


    @Autowired
    private ApplicationEventPublisher publisher;

    /*
     * we use a weak hashMap of the group permissions to prevent tDAR from constantly hammering the auth system with the group permissions. The hashMap will
     * track these permissions for short periods of time. Logging out and logging in should reset this
     */
    private final WeakHashMap<TdarUser, TdarGroup> groupMembershipCache = new WeakHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private PersonDao personDao;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserNotificationService userNotificationService;

    @Autowired
    private InstitutionDao institutionDao;

    private AuthenticationProvider provider;

    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#findByUsername(java.lang.String)
     */
    @Override
    @Transactional(readOnly = true)
    public TdarUser findByUsername(String username) {
        if (StringUtils.isEmpty(username)) {
            return null;
        }
        return personDao.findByUsername(username);
    }

    /*
     * exposes the groups the user is a member of from the external Provider; exposes groups as a String, as the external provider may include other permissions
     * beyond just tDAR groups
     */
    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#getGroupMembership(org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    public Collection<String> getGroupMembership(TdarUser person) {
        return Arrays.asList(getAuthenticationProvider().findGroupMemberships(person));
    }

    /*
     * Returns a list of the people in the @link groupMembershipCache which is useful in tracking what's going on with tDAR at a given moment. This would be
     * helpful for
     * a shutdown hook, as well as, for knowing when it's safe to deploy.
     */
    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#getCurrentlyActiveUsers()
     */
    @Override
    public synchronized List<TdarUser> getCurrentlyActiveUsers() {
        List<TdarUser> toReturn = new ArrayList<>(groupMembershipCache.keySet());
        toReturn.removeAll(Collections.singleton(null));
        return toReturn;
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#findGroupMemberships(org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    public List<TdarGroup> findGroupMemberships(TdarUser user) {
        String[] groups = getProvider().findGroupMemberships(user);
        logger.trace("Found {} memberships for {}", Arrays.asList(groups), user.getUsername());
        List<TdarGroup> toReturn = new ArrayList<>();
        for (String groupString : groups) {
            toReturn.add(TdarGroup.fromString(groupString));
        }
        return toReturn;
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#userHasPendingRequirements(org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    public boolean userHasPendingRequirements(TdarUser user) {
        return !getUserRequirements(user).isEmpty();
    }

    /*
     * Not currently used; but would allow for the updating of a username in the external auth system by deleting the user and adding them again. In Crowd 2.8
     * this is builtin function; but it might not be for LDAP.
     */
    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#updateUsername(org.tdar.core.bean.entity.TdarUser, java.lang.String, java.lang.String)
     */
    @Override
    public void updateUsername(TdarUser person, String newUsername, String password) {
        if (personDao.findByUsername(newUsername.toLowerCase()) != null) {
            throw new TdarRecoverableRuntimeException("auth.username_exists", Arrays.asList(newUsername));
        }

        String[] groupNames = getProvider().findGroupMemberships(person);
        List<TdarGroup> groups = new ArrayList<>();
        for (String groupName : groupNames) {
            groups.add(TdarGroup.valueOf(groupName));
        }
        getProvider().deleteUser(person);
        person.setUsername(newUsername.toLowerCase());
        getProvider().addUser(person, password, groups.toArray(new TdarGroup[groups.size()]));
    }

    /*
     * Authenticate a web user passing in the Request, Response, username and password. Checks that (a) the username is valid (b) that the user can authenticate
     * (c) that user exists and is valid within tDAR (active);
     * 
     * @param UserLogin - a bean containing username and password data
     * 
     * @param request - the @link HttpServletRequest to read cookies from or other information
     * 
     * @param response - the @link HttpServletResponse to set the error code on
     * 
     * @param sessionData - the @SessionData object to intialize with the user's session / cookie information if logged in properly.
     */
    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#authenticatePerson(org.tdar.core.service.external.auth.UserLogin, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.tdar.core.service.external.session.SessionData)
     */
    @Override
    @Transactional
    public AuthenticationResult authenticatePerson(UserLogin userLogin, HttpServletRequest request, HttpServletResponse response,
            SessionData sessionData) {
        // deny authentication if we've turned it off in cases of system manintance
        if (!CONFIG.allowAuthentication()
                || CONFIG.getAdminUsernames().contains(userLogin.getLoginUsername())) {
            return new AuthenticationResult(AuthenticationResultType.REMOTE_EXCEPTION);
        }

        if (!isPossibleValidUsername(userLogin.getLoginUsername())) {
            throw new TdarRecoverableRuntimeException("auth.username.invalid");
        }

        AuthenticationResult result = getAuthenticationProvider().authenticate(request, response, userLogin.getLoginUsername(), userLogin.getLoginPassword());
        if (!result.getType().isValid()) {
            logger.debug("Couldn't authenticate {} - (reason: {})", userLogin.getLoginUsername(), result);
            throw new TdarRecoverableRuntimeException("auth.couldnt_authenticate", Arrays.asList(result.getType().getMessage()));
        }

        TdarUser tdarUser = personDao.findByUsername(userLogin.getLoginUsername());
        if (tdarUser == null) {
            // FIXME: person exists in Crowd but not in tDAR..
            logger.debug("Person successfully authenticated by authentication service but not present in site database: {}", userLogin.getLoginUsername());
            tdarUser = new TdarUser();
            tdarUser.setUsername(userLogin.getLoginUsername());
            // how to pass along authentication information..?
            // username was in Crowd but not in tDAR? Redirect them to the account creation page
            result.setStatus(AuthenticationStatus.NEW);
            return result;
        }

        setupAuthenticatedUser(tdarUser, sessionData, request);
        personDao.registerLogin(tdarUser, getUserAgent(request));
        result.setStatus(AuthenticationStatus.AUTHENTICATED);
        return result;
    }

    @Transactional(readOnly=false)
    public void disableAccount(SessionData sessionData, HttpServletRequest servletRequest, HttpServletResponse servletResponse, TdarUser user) {
        user.setStatus(Status.DELETED);
        personDao.saveOrUpdate(user);
        getAuthenticationProvider().updateBasicUserInformation(user);
        logout(sessionData, servletRequest, servletResponse, user);
    }
    
    private void setupAuthenticatedUser(TdarUser tdarUser, SessionData sessionData, HttpServletRequest request) {

        if (!tdarUser.isActive()) {
            throw new TdarRecoverableRuntimeException("auth.cannot.deleted");
        }

        // enable us to force group cache to be cleared
        clearPermissionsCache(tdarUser);

        if (!isMember(tdarUser, TdarGroup.TDAR_USERS)) {
            throw new TdarRecoverableRuntimeException("auth.cannot.notmember");
        }

        logger.debug(String.format("%s (%s) logged in from %s using: %s", tdarUser.getUsername(), tdarUser.getEmail(), request.getRemoteAddr(),
                getUserAgent(request)));
        createAuthenticationToken(tdarUser, sessionData);
    }

    private String getUserAgent(HttpServletRequest request) {
        return request.getHeader(USER_AGENT);
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#isMember(org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.TdarGroup)
     */
    @Override
    public boolean isMember(TdarUser person, TdarGroup group) {
        return checkAndUpdateCache(person, group);
    }

    /*
     * Checks the current cache for the @link Person and their @linkTdarGroup permissions, if it exists, it returns whether the @link Person is a member of the
     * group. If not, it checks the external authentication and authorization service (CROWD/LDAP) to see what @link TdarGroup Memberships are set for that
     * 
     * @link Person
     * and then updates the cache (HashMap)
     */
    synchronized boolean checkAndUpdateCache(TdarUser tdarUser, TdarGroup requestedPermissionsGroup) {
        TdarGroup greatestPermissionGroup = groupMembershipCache.get(tdarUser);
        if (greatestPermissionGroup == null) {
            greatestPermissionGroup = findGroupWithGreatestPermissions(tdarUser);
            groupMembershipCache.put(tdarUser, greatestPermissionGroup);
        }
        return greatestPermissionGroup.hasGreaterPermissions(requestedPermissionsGroup);
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#findGroupWithGreatestPermissions(org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    public TdarGroup findGroupWithGreatestPermissions(TdarUser person) {
        if (person == null) {
            return TdarGroup.UNAUTHORIZED;
        }
        String login = person.getUsername();
        if (StringUtils.isBlank(login)) {
            return TdarGroup.UNAUTHORIZED;
        }
        TdarGroup greatestPermissionGroup = TdarGroup.UNAUTHORIZED;
        List<TdarGroup> groups = new ArrayList<>();
        try {
            groups.addAll(findGroupMemberships(person));
        } catch (TdarAuthorizationException ae) {
            logger.error("issue with crowd", ae);
        }
        logger.trace("Found {} memberships for {}", Arrays.asList(groups), login);
        for (TdarGroup group : groups) {
            if (group.hasGreaterPermissions(greatestPermissionGroup)) {
                greatestPermissionGroup = group;
            }
        }
        return greatestPermissionGroup;
    }

    /*
     * creates an authentication token (last step in authenticating); that tDAR can use for the entire session
     */
    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#createAuthenticationToken(org.tdar.core.bean.entity.TdarUser, org.tdar.core.service.external.session.SessionData)
     */
    @Override
    public void createAuthenticationToken(TdarUser person, SessionData session) {
        session.setTdarUser(person);
    }

    /*
     * Checks that a username to be added is valid
     */
    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#isValidUsername(java.lang.String)
     */
    @Override
    public boolean isValidUsername(String username) {
        if (StringUtils.isBlank(username)) {
            return false;
        }

        return username.matches(USERNAME_VALID_REGEX);
    }

    /*
     * This is separate to ensure that legacy usernames are supported by the system
     */
    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#isPossibleValidUsername(java.lang.String)
     */
    @Override
    public boolean isPossibleValidUsername(String username) {
        if (StringUtils.isBlank(username)) {
            return false;
        }

        return username.matches(LEGACY_USERNAME_VALID_REGEX);
    }

    /*
     * Checks that the email is a valid email address
     */
    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#isValidEmail(java.lang.String)
     */
    @Override
    public boolean isValidEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return false;
        }

        return email.matches(EMAIL_VALID_REGEX);
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#clearPermissionsCache()
     */
    @Override
    public synchronized void clearPermissionsCache() {
        logger.debug("Clearing group membership cache of all entries: {}", groupMembershipCache);
        groupMembershipCache.clear();
    }

    /*
     * Removes a specific @link Person from the Permissions cache (e.g. when they log out).
     */
    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#clearPermissionsCache(org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    public synchronized void clearPermissionsCache(TdarUser tdarUser) {
        logger.debug("Clearing group membership cache of entry for : {}", tdarUser);
        groupMembershipCache.remove(tdarUser);
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#addAndAuthenticateUser(org.tdar.core.service.external.auth.UserRegistration, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.tdar.core.service.external.session.SessionData)
     */
    @Override
    @Transactional(readOnly = false)
    public synchronized AuthenticationResult addAndAuthenticateUser(UserRegistration reg, HttpServletRequest request,
            HttpServletResponse response, SessionData sessionData) {
        // FIXME: pointless alias?
        TdarUser person = reg.getPerson();
        TdarUser findByUsername = personDao.findByUsername(person.getUsername());
        TdarUser findByEmail = personDao.findUserByEmail(person.getEmail());
        // short circut the login process -- if there username and password are registered and valid -- just move on.
        if (PersistableUtils.isNotNullOrTransient(findByUsername)) {
            try {
                UserLogin userLogin = new UserLogin(findByUsername.getUsername(), reg.getPassword(), null);
                AuthenticationResult result = authenticatePerson(userLogin, request, response, sessionData);
                if (result.getStatus() == AuthenticationStatus.AUTHENTICATED) {
                    return new AuthenticationResult(AuthenticationResultType.VALID);
                }
            } catch (Exception e) {
                logger.warn("could not authenticate", e);
            }
        }
        // FIXME: refactor overwritten reconciliations
        person = reconcilePersonWithTransient(person, findByUsername, MessageHelper.getMessage("userAccountController.error_username_already_registered"));
        // if there's a user with this email, that takes precedence
        person = reconcilePersonWithTransient(person, findByEmail, MessageHelper.getMessage("userAccountController.error_duplicate_email"));
        if (PersistableUtils.isTransient(person)) {
            Person findByEmail2 = personDao.findByEmail(person.getEmail());
            if (PersistableUtils.isNotNullOrTransient(findByEmail2)) {
                person = personDao.findConvertPersonToUser(findByEmail2, person.getUsername());
                logger.info("person: {}", person);
            }
        }
        String institutionName = StringUtils.trim(reg.getInstitutionName());
        Institution institution = institutionDao.findByName(institutionName);
        if ((institution == null) && !StringUtils.isBlank(institutionName)) {
            institution = new Institution();
            institution.setName(institutionName);
            personDao.save(institution);
        }
        person.setInstitution(institution);
        reg.trace(getUserAgent(request));
        person.setAffiliation(reg.getAffiliation());
        person.setContributorReason(reg.getContributorReason());
        AuthenticationResult addResult = getAuthenticationProvider().addUser(person, reg.getPassword());
        if (!addResult.getType().isValid()) {
            throw new TdarRecoverableRuntimeException(addResult.getType().getMessage());
        }
        // after the person has been saved, create a contributor request for
        // them as needed.
        if (reg.isRequestingContributorAccess()) {
            person.setContributor(true);
            satisfyPrerequisite(person, AuthNotice.CONTRIBUTOR_AGREEMENT);
        } else {
            person.setContributor(false);
        }
        satisfyPrerequisite(person, AuthNotice.TOS_AGREEMENT);

        // add user to Crowd
        person.setStatus(Status.ACTIVE);
        personDao.saveOrUpdate(person);
        updatePersonWithInvites(person);
        logger.debug("Trying to add user to auth service...");

        sendWelcomeEmail(person);
        userNotificationService.info(person, reg.getWelcomeNewUserMessageKey(), UserNotificationDisplayType.FREEMARKER);
        logger.info("Added user to auth service successfully.");
        // } else {
        // // we assume that the add operation failed because user was already in crowd. Common scenario for dev/alpha, but not prod.
        // logger.error("user {} already existed in auth service. Not unusual unless it happens in prod context ", person);
        // }
        // log person in.
        AuthenticationResult result = getAuthenticationProvider().authenticate(request, response, person.getUsername(), reg.getPassword());
        if (result.getType().isValid()) {
            logger.debug("Authenticated successfully with auth service, registering login and creating authentication token");
            personDao.registerLogin(person, getUserAgent(request));
            createAuthenticationToken(person, sessionData);
        }
        result.setPerson(person);
        return result;
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#updatePersonWithInvites(org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = false)
    public void updatePersonWithInvites(TdarUser person) {
        List<UserInvite> invites = personDao.checkInvite(person);
        Map<TdarUser, List<HasName>> notices = new HashMap<>();
        for (UserInvite invite : invites) {
            if (invite.getPermissions() == null) {
                continue;
            }
            DateTime now = DateTime.now();

            Date dateExpires = invite.getDateExpires();
            TdarUser authorizer = invite.getAuthorizer();
            if (dateExpires != null ||  now.isAfter(new DateTime(dateExpires))) {
                logger.debug("invite expired: {}", invite);
                continue;
            }
            notices.putIfAbsent(authorizer, new ArrayList<>());
            AuthorizedUser user = new AuthorizedUser(authorizer, person, invite.getPermissions());
            invite.setDateRedeemed(new Date());
            personDao.saveOrUpdate(invite);

            if (invite.getResourceCollection() != null) {
                invite.getResourceCollection().getAuthorizedUsers().add(user);
                personDao.saveOrUpdate(invite.getResourceCollection());
                personDao.saveOrUpdate(user);
                notices.get(authorizer).add((HasName)invite.getResourceCollection());
                publisher.publishEvent(new TdarEvent(invite.getResourceCollection(), EventType.CREATE_OR_UPDATE));
            } 

            if (invite.getResource() != null) {
                invite.getResource().getAuthorizedUsers().add(user);
                personDao.saveOrUpdate(invite.getResource());
                personDao.saveOrUpdate(user);
                notices.get(authorizer).add(invite.getResource());
                publisher.publishEvent(new TdarEvent(invite.getResource(), EventType.CREATE_OR_UPDATE));
            }

            //FIXME: REMOVE if rights changes work
        }
        emailService.sendUserInviteGrantedEmail(notices, person);

    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#checkToken(java.lang.String, org.tdar.core.service.external.session.SessionData, javax.servlet.http.HttpServletRequest)
     */
    @Override
    public AuthenticationResult checkToken(String token, SessionData sessionData, HttpServletRequest request) {
        AuthenticationResult result = provider.checkToken(token, request);
        logger.debug("token check result: {}", result.getStatus());
        if (result.getType().isValid()) {
            TdarUser tdarUser = personDao.findByUsername(result.getTokenUsername());
            setupAuthenticatedUser(tdarUser, sessionData, request);
        }
        return result;
    }

    private void sendWelcomeEmail(Person person) {
        Map<String, Object> result = new HashMap<>();
        result.put("user", person);
        result.put("config", CONFIG);
        try {
            String subject = MessageHelper.getMessage("userAccountController.welcome", Arrays.asList(CONFIG.getSiteAcronym()));
            Email email = new Email();
            email.setSubject(subject);
            email.addToAddress(person.getEmail());
            email.setUserGenerated(false);
            emailService.queueWithFreemarkerTemplate(EMAIL_WELCOME_TEMPLATE, result, email);
        } catch (Exception e) {
            // we don't want to ruin the new user's experience with a nasty error message...
            logger.error("Suppressed error that occurred when trying to send welcome email", e);
        }
    }

    private TdarUser reconcilePersonWithTransient(TdarUser incoming, TdarUser resultOfLookup, String error) {
        if ((resultOfLookup != null) && PersistableUtils.isNullOrTransient(incoming)) {
            switch (resultOfLookup.getStatus()) {
                default:
                    if (StringUtils.isBlank(resultOfLookup.getUsername())) {
                        resultOfLookup.setUsername(incoming.getUsername());
                    } else {
                        logger.debug("user is not valid");
                        throw new TdarRecoverableRuntimeException(error);
                    }
                case DRAFT:
                case DUPLICATE:
                    incoming.setStatus(Status.ACTIVE);
                    resultOfLookup.setStatus(Status.ACTIVE);
            }
            resultOfLookup.setFirstName(incoming.getFirstName());
            resultOfLookup.setLastName(incoming.getLastName());
            resultOfLookup.setAffiliation(incoming.getAffiliation());
            resultOfLookup.setInstitution(incoming.getInstitution());
            resultOfLookup.setPhone(incoming.getPhone());
            logger.debug("existing user: {} ", resultOfLookup);
            return resultOfLookup;
        }
        return incoming;
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#setProvider(org.tdar.core.dao.external.auth.AuthenticationProvider)
     */
    @Override
    @Autowired(required = false)
    @Qualifier("AuthenticationProvider")
    public void setProvider(AuthenticationProvider provider) {
        this.provider = provider;
        if (provider != null) {
            logger.debug("Authentication Provider: {}", provider.getClass().getSimpleName());
        } else {
            logger.debug("Authentication Provider: NOT CONFIGURED");
        }
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#getProvider()
     */
    @Override
    public AuthenticationProvider getProvider() {
        return provider;
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#logout(org.tdar.core.service.external.session.SessionData, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    @Transactional(readOnly = true)
    public void logout(SessionData sessionData, HttpServletRequest servletRequest, HttpServletResponse servletResponse, TdarUser user) {
        sessionData.clearAuthenticationToken();
        String token = getSsoTokenFromRequest(servletRequest);
        getAuthenticationProvider().logout(servletRequest, servletResponse, token, user);
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#getSsoTokenFromRequest(javax.servlet.http.HttpServletRequest)
     */
    @Override
    public String getSsoTokenFromRequest(HttpServletRequest request) {
        String token = request.getParameter(CONFIG.getRequestTokenName());
        if (StringUtils.isNotBlank(token)) {
            return token;
        }

        if (!ArrayUtils.isEmpty(request.getCookies())) {
            for (Cookie c : request.getCookies()) {
                if (c.getName().equals(CONFIG.getRequestTokenName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#getAuthenticationProvider()
     */
    @Override
    public AuthenticationProvider getAuthenticationProvider() {
        return getProvider();
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#getUserRequirements(org.tdar.core.bean.entity.TdarUser)
     */
    @Override
    public List<AuthNotice> getUserRequirements(TdarUser user) {
        List<AuthNotice> notifications = new ArrayList<>();
        // not public static final because don't work in testing
        Integer tosLatestVersion = CONFIG.getTosLatestVersion();
        Integer contributorAgreementLatestVersion = CONFIG.getContributorAgreementLatestVersion();
        if (user.getTosVersion() < tosLatestVersion) {
            notifications.add(AuthNotice.TOS_AGREEMENT);
        }

        if (user.isContributor() && (user.getContributorAgreementVersion() < contributorAgreementLatestVersion)) {
            notifications.add(AuthNotice.CONTRIBUTOR_AGREEMENT);
        }
        return notifications;
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#satisfyPrerequisite(org.tdar.core.bean.entity.TdarUser, org.tdar.core.bean.AuthNotice)
     */
    @Override
    @Transactional(readOnly = false)
    public void satisfyPrerequisite(TdarUser user, AuthNotice req) {
        // not public static final because don't work in testing
        Integer tosLatestVersion = CONFIG.getTosLatestVersion();
        Integer contributorAgreementLatestVersion = CONFIG.getContributorAgreementLatestVersion();
        switch (req) {
            case CONTRIBUTOR_AGREEMENT:
                user.setContributorAgreementVersion(contributorAgreementLatestVersion);
                break;
            case TOS_AGREEMENT:
                user.setTosVersion(tosLatestVersion);
                break;
//            case GUEST_ACCOUNT:
//                break;
        }
    }

    /*
     * @see #satisfyUserPrerequisites(SessionData sessionData, Collection<AuthNotice> notices)
     */
    @Transactional(readOnly = false)
    public void satisfyPrerequisites(TdarUser user, Collection<AuthNotice> notices) {
        for (AuthNotice notice : notices) {
            satisfyPrerequisite(user, notice);
        }
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#satisfyUserPrerequisites(org.tdar.core.service.external.session.SessionData, java.util.Collection)
     */
    @Override
    @Transactional(readOnly = false)
    public void satisfyUserPrerequisites(SessionData sessionData, Collection<AuthNotice> notices) {
        // we actually need to update two person instances: the persisted user record, and the detached user
        // associated with the session. We hide this detail from the caller.
        // TdarUser detachedUser = sessionData.getTdarUser();
        TdarUser persistedUser = personDao.find(TdarUser.class, sessionData.getTdarUserId());
        // satisfyPrerequisites(detachedUser, notices);
        satisfyPrerequisites(persistedUser, notices);
        personDao.saveOrUpdate(persistedUser);
        // logger.trace(" detachedUser:{}, tos:{}, ca:{}", detachedUser, detachedUser.getTosVersion(), detachedUser.getContributorAgreementVersion());
        logger.trace(" persistedUser:{}, tos:{}, ca:{}", persistedUser, persistedUser.getTosVersion(), persistedUser.getContributorAgreementVersion());
    }

    /*
     * Normalize the username being passed in; we may need to do more than lowercase it, such as run it through a REGEXP.
     */
    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#normalizeUsername(java.lang.String)
     */
    @Override
    public String normalizeUsername(String userName) {
        // for now, we just lowercase it.
        String normalizedUsername = userName.toLowerCase();
        return normalizedUsername;
    }

    /* (non-Javadoc)
     * @see org.tdar.core.service.external.AuthenticationService#requestPasswordReset(java.lang.String)
     */
    @Override
    @Transactional(readOnly = true)
    public void requestPasswordReset(String usernameOrEmail) {
        TdarUser user = personDao.findByUsername(usernameOrEmail);
        if (user == null) {
            user = personDao.findUserByEmail(usernameOrEmail);
        }

        if (user != null) {
            getAuthenticationProvider().requestPasswordReset(user);
        }

    }

}
