package org.tdar.core.bean.notification.aws;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tdar.core.bean.notification.Email;
import org.tdar.core.bean.notification.EmailType;

public abstract class AwsMessage {
	private EmailType emailType; 
	private Email email;
	private Map<String, Object> map = new HashMap<>();
	private List<File> attachments = new ArrayList<File>();
	private Map<String, File> inlineAttachments = new HashMap<String, File>();
	
	public EmailType getEmailType() {
		return emailType;
	}
	
	public void setEmailType(EmailType emailType) {
		this.emailType =  emailType;
	}

	public Email getEmail() {
		return email;
	}

	public void setEmail(Email email) {
		this.email = email;
	}

	public Map<String, Object> getMap() {
		return map;
	}

	public void setMap(Map<String, Object> map) {
		this.map = map;
	}

	public List<File> getAttachments() {
		return attachments;
	}

	/**
	 * Sets a list of attachments that should be included in the email.
	 * @param attachments
	 */
	public void setAttachments(List<File> attachments) {
		this.attachments = attachments;
	}
	
	/**
	 * Adds data to the parameters map for the template to consume.
	 * @param key
	 * @param value
	 * @return
	 */
	public AwsMessage addData(String key, Object value){
		this.map.put(key, value);
		return this;
	}
	
	public void addAttachment(File file){
		this.attachments.add(file);
	}
	
	public void addInlineAttachment(String contentId, File file){
		this.inlineAttachments.put(contentId, file);
	}
	

	public abstract String createSubjectLine();

	public Map<String, File> getInlineAttachments() {
		return inlineAttachments;
	}
	
}
