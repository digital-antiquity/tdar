package org.tdar.struts.action;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Properties;

import org.tdar.core.configuration.TdarConfiguration;

public class WebConfig {

    private TdarConfiguration config = TdarConfiguration.getInstance();
    private Properties changesetProps;

    public String getThemeDir() {
        return config.getThemeDir();
    }

    public String getCulturalTermsHelpUrl() {
        return config.getCulturalTermsHelpURL();
    }

    public String getInvestigationTypesHelpUrl() {
        return config.getInvestigationTypesHelpURL();
    }

    public String getMaterialTypesHelpUrl() {
        return config.getMaterialTypesHelpURL();
    }

    public String getSiteTypesHelpUrl() {
        return config.getSiteTypesHelpURL();
    }

    public String getGoogleMapsApiKey() {
        return config.getGoogleMapsApiKey();
    }

    public String getGoogleAnalyticsId() {
        return config.getGoogleAnalyticsId();
    }

    public boolean getPrivacyControlsEnabled() {
        return config.getPrivacyControlsEnabled();
    }

    public boolean isCopyrightMandatory() {
        return config.getCopyrightMandatory();
    }

    public boolean isArchiveFileEnabled() {
        return config.isArchiveFileEnabled();
    }

    public boolean isVideoEnabled() {
        return config.isVideoEnabled();
    }

    public boolean isLicensesEnabled() {
        return config.getLicenseEnabled();
    }

    public String getBugReportUrl() {
        return config.getBugReportUrl();
    }

    public String getDocumentationUrl() {
        return config.getDocumentationUrl();
    }

    public String getIntegrationDocumentationUrl() {
        return config.getIntegrationDocumentationUrl();
    }

    public boolean isProduction() {
        return config.getServerEnvironmentStatus().equalsIgnoreCase(TdarConfiguration.PRODUCTION);
    }

    public String getHelpUrl() {
        return config.getHelpUrl();
    }

    public String getAboutUrl() {
        return config.getAboutUrl();
    }

    public String getCommentsUrl() {
        return config.getAboutUrl();
    }

    public Boolean getRPAEnabled() {
        return config.isRPAEnabled();
    }

    public Boolean isRPAEnabled() {
        return config.isRPAEnabled();
    }

    public String getMapDefaultLat() {
        DecimalFormat latlong = new DecimalFormat("0.00");
        latlong.setGroupingUsed(false);
        return latlong.format(config.getMapDefaultLat());
    }

    public String getMapDefaultLng() {
        DecimalFormat latlong = new DecimalFormat("0.00");
        latlong.setGroupingUsed(false);
        return latlong.format(config.getMapDefaultLng());
    }

    public boolean isGeoLocationToBeUsed() {
        return config.isGeoLocationToBeUsed();
    }

    public String getNewsUrl() {
        return config.getNewsUrl();
    }

    public boolean isPayPerIngestEnabled() {
        return config.isPayPerIngestEnabled();
    }

    public boolean getShowJiraLink() {
        return config.getShowJiraLink();
    }

    public String getJiraScriptLink() {
        return config.getJiraScriptLink();
    }

    public boolean isViewRowSupported() {
        return config.isViewRowSupported();
    }

    public Long getGuestUserId() {
        return config.getGuestUserId();
    }

    public String getCulturalTermsLabel() {
        return config.getCulturalTermsLabel();
    }

    /**
     * @see TdarConfiguration#isSwitchableMapObfuscation()
     * @return whatever value the tdar configuration isSwitchableMapObfuscation returns.
     */
    public boolean isSwitchableMapObfuscation() {
        return config.isSwitchableMapObfuscation();
    }

    public boolean isShouldAutoDownload() {
        return config.shouldAutoDownload();
    }

    public String getTosUrl() {
        return config.getTosUrl();
    }

    public String getContributorAgreementUrl() {
        return config.getContributorAgreementUrl();
    }

    public String getPrivacyPolicyUrl() {
        return config.getPrivacyPolicyUrl();
    }

    public boolean isAuthenticationAllowed() {
        return config.allowAuthentication();
    }

    public String getResourceCreatorRoleDocumentationUrl() {
        return config.getResourceCreatorRoleDocumentationUrl();
    }

    public String getLeafletApiKey() {
        return config.getLeafletMapsApiKey();
    }

    public List<String> getBarColors() {
        return config.getBarColors();
    }

    public boolean isSelenium() {
        return config.isSelenium();
    }

    public String getContactEmail() {
        return config.getContactEmail();
    }

    public String getCommentUrl() {
        return config.getCommentUrl();
    }

    public String getCommentUrlEscaped() {
        String input = config.getCommentUrl();
        int length = input.length();
        StringBuffer output = new StringBuffer(length * 6);
        for (int i = 0; i < input.length(); i++) {
            output.append("&#");
            output.append((int) input.charAt(i));
            output.append(";");
        }
        return output.toString();
    }
    
    public String getChangeset() {
        Properties props = loadChangesetProps();
        if (props == null) {
            return "";
        }
        return String.format("%s (%s)", props.getProperty("git.commit.id") , props.getProperty("git.branch") );
    }

    public String getChangesetId() {
        Properties props = loadChangesetProps();
        if (props == null) {
            return "";
        }
        return props.getProperty("git.commit.id");
    }

    private Properties loadChangesetProps() {
        if (changesetProps != null) {
            return changesetProps;
        }
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("git.properties");
        if (resourceAsStream == null) {
            return null;
        }
        try {
            Properties props = new Properties(); 
            props.load(resourceAsStream);
            changesetProps = props;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            org.apache.commons.io.IOUtils.closeQuietly(resourceAsStream);
        }
        return changesetProps;
    }


    public boolean isListCollectionsEnabled() {
        return config.isListCollectionsEnabled();
    }

    public boolean isXmlExportEnabled() {
        return config.isXmlExportEnabled();
    }

}
