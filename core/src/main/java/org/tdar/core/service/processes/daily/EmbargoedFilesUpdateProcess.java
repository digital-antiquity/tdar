package org.tdar.core.service.processes.daily;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.resource.InformationResource;
import org.tdar.core.bean.resource.file.FileAccessRestriction;
import org.tdar.core.bean.resource.file.InformationResourceFile;
import org.tdar.core.configuration.TdarConfiguration;
import org.tdar.core.dao.GenericDao;
import org.tdar.core.service.external.EmailService;
import org.tdar.core.service.processes.AbstractScheduledProcess;
import org.tdar.core.service.resource.InformationResourceFileService;

@Component
@Scope("prototype")
public class EmbargoedFilesUpdateProcess extends AbstractScheduledProcess {

	private static final String SITE_ACRONYM = TdarConfiguration.getInstance().getSiteAcronym();
	private static final String BASE_URL = TdarConfiguration.getInstance().getBaseUrl();
	
	private static final long serialVersionUID = 5134091457634096415L;

	private static final String SUBJECT = SITE_ACRONYM + ": Embargo Expiration Warning";

	private static final String SUBJECT_WARNING = SITE_ACRONYM + ": Embargo about to expire";
	private static final String SUBJECT_EXPIRED = SITE_ACRONYM + ": Embargo Expired";

	@Autowired
	private transient EmailService emailService;
	@Autowired
	private transient GenericDao genericDao;

	@Autowired
	private transient InformationResourceFileService informationResourceFileService;

	private boolean completed;

	@Override
	public String getDisplayName() {
		return "Embargoed Files Process";
	}

	@Override
	public void execute() {
		List<InformationResourceFile> expired = informationResourceFileService.findAllExpiredEmbargoFiles();
		List<InformationResourceFile> toExpire = informationResourceFileService.findAllEmbargoFilesExpiringTomorrow();

		if (CollectionUtils.isEmpty(expired) && CollectionUtils.isEmpty(toExpire)) {
			return;
		}
		logger.debug("expired: {} toExpire: {}", CollectionUtils.size(expired), CollectionUtils.size(toExpire));

		if (CollectionUtils.isNotEmpty(toExpire)) {
			Map<TdarUser, Set<InformationResourceFile>> toExpiredMap = createMap(toExpire);
			sendExpirationNotices(toExpiredMap);
		}

		if (CollectionUtils.isNotEmpty(expired)) {
			Map<TdarUser, Set<InformationResourceFile>> expiredMap = createMap(expired);
			expire(expiredMap);
		}

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("expired", expired);
		map.put("toExpired", toExpire);
		map.put("siteAcronym", SITE_ACRONYM);
		map.put("baseUrl", BASE_URL);
		Email email = new Email();
		email.setSubject(SUBJECT);
		email.setUserGenerated(false);
		if (CollectionUtils.isNotEmpty(expired) || CollectionUtils.isNotEmpty(toExpire)) {
			emailService.queueWithFreemarkerTemplate("embargo/expiration-admin.ftl", map, email);
		}
	}

	private Map<TdarUser, Set<InformationResourceFile>> createMap(List<InformationResourceFile> expired) {
		Map<TdarUser, Set<InformationResourceFile>> expiredMap = new HashMap<>();
		for (InformationResourceFile file : expired) {
            InformationResource r = file.getInformationResource();
			TdarUser submitter = r.getSubmitter();
			if (!expiredMap.containsKey(submitter)) {
				expiredMap.put(submitter, new HashSet<>());
			}
			if (r.isActive() || r.isDraft()) {
			    expiredMap.get(submitter).add(file);
			}
		}
		return expiredMap;
	}

	private void expire(Map<TdarUser, Set<InformationResourceFile>> expiredMap) {
		for (TdarUser submitter : expiredMap.keySet()) {
			Set<InformationResourceFile> expired = expiredMap.get(submitter);
			for (InformationResourceFile file : expired) {
				file.setRestriction(FileAccessRestriction.PUBLIC);
				genericDao.saveOrUpdate(file);
			}
			sendEmail(submitter, expired, true);
		}
	}

	private void sendEmail(TdarUser submitter, Set<InformationResourceFile> files, boolean isExpiration) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("files", files);
		Email email = new Email();
		map.put("submitter", submitter);
		map.put("siteAcronym", SITE_ACRONYM);
		map.put("baseUrl", BASE_URL);
		email.setTo(submitter.getEmail());

		email.setUserGenerated(false);
		if (isExpiration) {
			email.setSubject(SUBJECT_EXPIRED);
			emailService.queueWithFreemarkerTemplate("embargo/expiration.ftl", map, email);
		} else {
			email.setSubject(SUBJECT_WARNING);
			emailService.queueWithFreemarkerTemplate("embargo/expiration-warning.ftl", map, email);
		}
		completed = true;
	}

	private void sendExpirationNotices(Map<TdarUser, Set<InformationResourceFile>> toExpiredMap) {
		for (TdarUser submitter : toExpiredMap.keySet()) {
			Set<InformationResourceFile> expired = toExpiredMap.get(submitter);
			sendEmail(submitter, expired, false);
		}
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isCompleted() {
		return completed;
	}

	@Override
	public boolean isSingleRunProcess() {
		// TODO Auto-generated method stub
		return false;
	}
}
