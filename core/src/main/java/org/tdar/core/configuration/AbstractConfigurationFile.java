package org.tdar.core.configuration;

public abstract class AbstractConfigurationFile {

	public static final int HTTPS_PORT_DEFAULT = 443;
	private static final String DESCRIPTION = "tDAR is an international digital archive and repository that houses data about archaeological investigations, research, resources, and scholarship.  tDAR provides researchers new avenues to discover and integrate information relevant to topics they are studying.   Users can search tDAR for digital documents, data sets, images, GIS files, and other data resources from archaeological projects spanning the globe.  For data sets, users also can use data integration tools in tDAR to simplify and illuminate comparative research.";
	public static final String DEFAULT_HOSTNAME = "core.tdar.org";
	public static final int DEFAULT_PORT = 80; // we use this in test
	public static final String DEFAULT_SMTP_HOST = "localhost";
	protected final static String FROM_EMAIL_NAME = "info@";
	protected static final String SYSTEM_ADMIN_EMAIL = "tdar-svn@lists.asu.edu";

	public AbstractConfigurationFile() {
		super();
	}

	public String getRepositoryName() {
		return getAssistant().getStringProperty("oai.repository.name", "the Digital Archaeological Record");
	}

	public String getSystemDescription() {
		return getAssistant().getStringProperty("oai.repository.description", DESCRIPTION);
	}

	public String getBaseUrl() {
		String base = "http://" + getHostName();
		if (getPort() != DEFAULT_PORT) {
			base += ":" + getPort();
		}
		return base;
	}

	public String getHostName() {
		return getAssistant().getStringProperty("app.hostname", DEFAULT_HOSTNAME);
	}

	public String getSystemAdminEmail() {
		return getAssistant().getStringProperty("sysadmin.email", SYSTEM_ADMIN_EMAIL);
	}

	public String getEmailHostName() {
		return getAssistant().getStringProperty("app.email.hostname", getHostName());
	}

	public int getPort() {
		return getAssistant().getIntProperty("app.port", DEFAULT_PORT);
	}

	protected abstract ConfigurationAssistant getAssistant();
}