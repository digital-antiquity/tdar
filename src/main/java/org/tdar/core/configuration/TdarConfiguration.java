package org.tdar.core.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.geotools.resources.image.ImageUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.LicenseType;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.PairtreeFilestore;

/**
 * $Id$
 * 
 * Configuration file for various tDAR webapp properties.
 * 
 * FIXME: convert to a Spring managed bean (and by extension, Filestore)
 * 
 * @author <a href='mailto:Allen.Lee@asu.edu'>Allen Lee</a>
 * @version $Rev$
 */
public class TdarConfiguration {

    public static final List<String> STOP_WORDS = Arrays.asList("the", "and", "a", "to", "of", "in", "i", "is", "that", "it", "on", "you", "this", "for",
            "but", "with", "are", "have", "be", "at", "or", "as", "was", "so", "if", "out", "not");
    private static final String JIRA_LINK = "https://dev.tdar.org/jira/s/en_USgh0sw9-418945332/844/18/1.2.9/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector.js?collectorId=959f12a3";
    private static final String DESCRIPTION = "tDAR is an international digital archive and repository that houses data about archaeological investigations, research, resources, and scholarship.  tDAR provides researchers new avenues to discover and integrate information relevant to topics they are studying.   Users can search tDAR for digital documents, data sets, images, GIS files, and other data resources from archaeological projects spanning the globe.  For data sets, users also can use data integration tools in tDAR to simplify and illuminate comparative research.";
    private static final String SECURITY_EXCEPTION_COULD_NOT_CREATE_PERSONAL_FILESTORE_HOME_DIRECTORY = "Security Exception: could not create personal filestore home directory";
    private static final String MESSAGE_QUEUE_IS_ENABLED_BUT_A_USERNAME_AND_PASSWORD_IS_NOT_DEFINED = "Message Queue is enabled, but a username and password is not defined";
    public static final String COULDN_T_CREATE_TEMPORARY_DIRECTORY_AT = "Couldn't create temporary directory at : ";
    public static final int DEFAULT_SCHEDULED_PROCESS_START_ID = 0;
    public static final int DEFAULT_SCHEDULED_PROCESS_END_ID = 400000;

    public static final String DEFAULT_HOSTNAME = "core.tdar.org";
    public static final int DEFAULT_PORT = 80; // we use this in test
    public static final String DEFAULT_SMTP_HOST = "localhost";
    private static final String SYSTEM_ADMIN_EMAIL = "tdar-svn@lists.asu.edu";

    public static final int DEFAULT_AUTHORITY_MANAGEMENT_DUPE_LIST_MAX_SIZE = 10;
    public static final int DEFAULT_AUTHORITY_MANAGEMENT_MAX_AFFECTED_RECORDS = 100;

    public static final int DEFAULT_SEARCH_EXCEL_EXPORT_RECORD_MAX = 1000;

    private final transient static Logger logger = LoggerFactory.getLogger(TdarConfiguration.class);

    private ConfigurationAssistant assistant;

    private Filestore filestore;

    private Set<String> stopWords = new HashSet<String>();
    private List<String> couponCodes = new ArrayList<String>();

    private String configurationFile;

    private final static TdarConfiguration INSTANCE = new TdarConfiguration();
    public static final String PRODUCTION = "production";
    private static final int USE_DEFAULT_EXCEL_ROWS = -1;

    private TdarConfiguration() {
        this("/tdar.properties");
    }

    /*
     * Do not use this except for via the @MultipleTdarConfigurationRunner
     */
    @Deprecated
    public void setConfigurationFile(String configurationFile) {
        assistant = new ConfigurationAssistant();
        assistant.loadProperties(configurationFile);
        this.configurationFile = configurationFile;
        filestore = loadFilestore();
        initPersonalFilestorePath();
        testQueue();
        initializeStopWords();

        if (ImageUtilities.isMediaLibAvailable()) {
            logger.info("JAI ImageIO available and configured");
        } else {
            logger.error("JAI-ImageIO is not properly installed with Native Libraries\n\nInstructions for Installation: http://docs.geoserver.org/latest/en/user/production/java.html");
            if (isProductionEnvironment()) {
                throw new IllegalStateException("cannot start up in production without JAI");
            }
        }
        intializeCouponCodes();
    }

    public String getConfigurationFile() {
        return configurationFile;
    }

    private TdarConfiguration(String configurationFile) {
        System.setProperty("java.awt.headless", "true");
        setConfigurationFile(configurationFile);
    }

    private void initializeStopWords() {
        try {
            stopWords.addAll(IOUtils.readLines(new FileInputStream(assistant.getStringProperty("lucene.stop.words.file"))));
        } catch (Exception e) {
            stopWords.addAll(STOP_WORDS);
        }
    }

    private void intializeCouponCodes() {
        try {
            couponCodes.addAll(IOUtils.readLines(new FileInputStream(assistant.getStringProperty("coupon.codes.file"))));
        } catch (Exception e) {
            couponCodes.addAll(Arrays.asList("acheulean", "acropolis", "agora", "alidade", "alloy", "alluvial", "amphora", "anthropology", "antiquarian",
                    "archaeoastronomy", "archaeology", "archaeozoology", "archaic", "aristocracy", "artifact", "assemblage", "association", "balk",
                    "benchmark", "biface", "cache", "ceramics", "chert", "citadel", "column", "conchoidal", "conservation", "context", "coprolite", "core",
                    "cortex", "crm", "culture", "cuneiform", "datum", "debitage", "dendrochronology", "diffusion", "ecofacts", "egyptology", "epigrapher",
                    "ethnography", "excavation", "fabric", "feature", "flake", "flint", "flotation", "geoarchaeology", "glaze", "grid", "harris",
                    "hieroglyphs", "hominid", "hominin", "hypostyle", "iconography", "ideogram", "insitu", "inorganic", "knapping", "levallois", "lineara",
                    "linearb", "lintel", "lithic", "locus", "looter", "matrix", "mesolithic", "microlith", "midden", "mousterian", "neolithic", "nomads",
                    "obsidian", "oldowan", "oligarchy", "organic", "osteology", "paleobotany", "paleolithic", "paleontology", "palynology", "papyrus",
                    "pedology", "petrology", "pictogram", "pithos", "polis", "prehistory", "profile", "provenance", "provenience", "quem", "radiocarbon",
                    "radiometric", "reconnaissance", "sediments", "seriation", "settlement", "sherd", "site", "slip", "soils", "square", "stela", "stele",
                    "stratigraphy", "style", "stylus", "surface", "survey", "tell", "temper", "terminus", "test", "thermoluminescence", "transit", "trench",
                    "tufa", "tumulus", "type", "typology", "varves", "ware", "ziggurat", "zone", "adovasio", "aharoni", "akbar", "akurgal", "alarcão",
                    "albright", "alcock", "allen", "alp", "amiran", "anderson", "andronicos", "archaeologist", "artamonov", "arık", "aston", "atkinson",
                    "australian", "aveni", "avigad", "azarnoush", "babington", "bahn", "bailey", "bandelier", "bandinelli", "bandyopadhyay", "baqir", "barkay",
                    "barker", "bateman", "batres", "beech", "belzoni", "berger", "bersu", "beule", "bey", "bicknell", "biddle", "biglari", "binford",
                    "bingham", "biondo", "biran", "black", "blegen", "bliss", "boni", "bordes", "borhegyi", "bourbourg", "bradley", "breasted", "breuer",
                    "bringmans", "brothwell", "brumfiel", "burl", "butzer", "calvert", "canina", "carr", "carter", "carver", "casey", "caso", "ceram",
                    "chakrabarti", "champe", "champollion", "chang", "chase", "cherry", "childe", "cicognara", "clark", "clarke", "clay", "cline", "coben",
                    "cole", "coles", "collier", "collis", "conkey", "connah", "corvinus", "cowgill", "crawford", "cribb", "croissier", "cumming", "cunliffe",
                    "cunnington", "curle", "curtius", "dales", "dani", "daniel", "dark", "davies", "dawkins", "deacon", "deetz", "delgado", "denon", "dent",
                    "deraniyagala", "desnoyers", "dezman", "didron", "dillehay", "dinçol", "dixon", "dobres", "dobson", "dovdoi", "dragendorff", "dunnell",
                    "dörpfeld", "emre", "enzheng", "erim", "evans", "fabricius", "fagan", "faklaris", "faussett", "fazioli", "fea", "feinman", "fellows",
                    "fernow", "fewkes", "finkelstein", "finlayson", "fischer", "fitzhugh", "flannery", "ford", "forman", "foucher", "fox", "frankell",
                    "frison", "fritz", "frost", "funari", "gabrovec", "galland", "gamble", "gann", "gardin", "gardner", "garfinkel", "garrod", "gell",
                    "george", "gerhard", "gero", "ghasidian", "gibbon", "gilbert", "gimbutas", "gjerstad", "goggin", "goodyear", "gopher", "goren", "graham",
                    "grakov", "greaves", "greene", "greenwell", "griffin", "grimes", "grote", "grube", "guarini", "hall", "hamed", "hanks", "hansen",
                    "harding", "harland", "harrington", "hasel", "haury", "hawass", "hawkes", "heizer", "hewett", "heydari", "heyne", "higgs", "hinton",
                    "hirschfeld", "hoare", "hodder", "hodge", "hoffman", "holdaway", "hole", "holliday", "horsley", "horvath", "hourany", "hume", "huot",
                    "imamovic", "isaac", "jahn", "jaubert", "jefferson", "jennings", "jewitt", "johanson", "johnson", "jones", "jorge", "joyce", "judge",
                    "kallee", "kamminga", "kamphaus", "kansu", "keeler", "kelley", "kelly", "kenyon", "kessler", "kidder", "kircher", "klein", "kloner",
                    "kober", "koldewey", "korfmann", "kossinna", "koşay", "kristiansen", "kuzman", "lanzi", "lape", "larcher", "lathrap", "lauer", "lawergren",
                    "lawrence", "layard", "leakey", "lenormant", "leone", "letronne", "lewis", "lhuillier", "lhuyd", "lipe", "littauer", "loeschcke",
                    "longacre", "loret", "loring", "lubbock", "lukis", "macalister", "macenery", "macneish", "maeir", "majidzadeh", "mallory", "mallowan",
                    "manley", "manyas", "marcus", "mariette", "marinatos", "marshall", "martinez", "mashkour", "mason", "maspero", "massiera", "mathiassen",
                    "mau", "maudslay", "mazar", "mazor", "mcburney", "mcghee", "mcguire", "mckern", "meggers", "mellars", "mengjia", "menon", "mercati",
                    "milanich", "milisauskas", "miller", "millon", "minnis", "minns", "montelius", "montet", "moore", "moorehead", "morley", "morse",
                    "mortimer", "moscati", "muckelroy", "muftarevic", "mulvaney", "murray", "merimee", "naderi", "negahban", "nelson", "netzer", "neustupny",
                    "newton", "noblecourt", "oakley", "oberlin", "olsen", "orser", "paranavithana", "pauketat", "pearsall", "pearson", "pengelly", "peregrine",
                    "perino", "perowne", "perthes", "petrie", "phillips", "piggott", "pinkerton", "piperno", "platon", "polosmak", "poole", "posener",
                    "possehl", "potter", "pour", "pryor", "quicherat", "rahtz", "ramsay", "rathje", "rebay", "renfrew", "renouf", "reuvens", "richards",
                    "rivers", "rochette", "rodriguez", "roe", "roebroeks", "rogers", "rostovtzeff", "rouge", "routledge", "royal", "rule", "rutar", "ryan",
                    "sakellarakis", "salisbury", "sandweiss", "sarianidi", "saulcy", "schaden", "schaeffer", "schiffer", "schliemann", "schmerling", "schrire",
                    "scipone", "shanks", "shaw", "shidrang", "sim", "smith", "snape", "south", "spector", "spence", "springs", "spurrell", "srinivasan",
                    "stanhope", "steen", "stein", "stekelis", "stephan", "stern", "stoddart", "strong", "sukenik", "sweden", "sümegi", "tarragon", "tarzi",
                    "taylor", "tello", "thom", "thomas", "thompson", "thomsen", "thurman", "thurston", "tilley", "tozzer", "trigger", "tuck", "tylecote",
                    "ucko", "ugolini", "ussishkin", "valera", "vaux", "vazeilles", "vinatie", "vince", "vinski", "visy", "voigt", "waelkens", "warren",
                    "watson", "webb", "wedel", "wegner", "welcker", "wendorf", "wettlaufer", "wheeler", "whitlow", "whittle", "wiegand", "willey", "williams",
                    "winckelmann", "woolley", "worsaae", "wurster", "wylie", "wymer", "yadin", "yoffee", "zeitlin", "ziffer", "zimmerman", "zimmermann",
                    "zuidema", "zvelebil"));
        }
    }

    /**
     * @return
     */
    private Filestore loadFilestore() {
        String filestoreClassName = assistant.getStringProperty("file.store.class",
                PairtreeFilestore.class.getCanonicalName());
        Filestore filestore = null;

        File filestoreLoc = new File(getFileStoreLocation());
        try {
            if (!filestoreLoc.exists()) {
                filestoreLoc.mkdirs();
            }
        } catch (Exception e) {
            logger.error("could not create filestore path:" + filestoreLoc.getAbsolutePath(), e);
        }

        try {
            Class<?> filestoreClass = Class.forName(filestoreClassName);
            filestore = (Filestore) filestoreClass.getConstructor(String.class).newInstance(getFileStoreLocation());
        } catch (Exception e) {
            String msg = "Could not instantiate Filestore: " + e.getMessage();
            logger.error(msg, e);
            throw new IllegalStateException(msg, e);
        }
        logger.info("instantiating filestore: {}", filestore.getClass().getCanonicalName());
        return filestore;
    }

    // verify that the personal filestore location exists, attempt to make it if it doesn't, and System.exit() if that fails
    private void initPersonalFilestorePath() {
        File personalFilestoreHome = new File(getPersonalFileStoreLocation());
        String msg = null;
        boolean pathExists = true;
        try {
            logger.info("initializing personal filestore at {}", getPersonalFileStoreLocation());
            if (!personalFilestoreHome.exists()) {
                pathExists = personalFilestoreHome.mkdirs();
                if (!pathExists) {
                    msg = "Could not create personal filestore at " + getPersonalFileStoreLocation();
                }
            }
        } catch (SecurityException ex) {
            logger.error(SECURITY_EXCEPTION_COULD_NOT_CREATE_PERSONAL_FILESTORE_HOME_DIRECTORY, ex);
            pathExists = false;
        }
        if (!pathExists) {
            throw new IllegalStateException(msg);
        }

    }

    public static TdarConfiguration getInstance() {
        return INSTANCE;
    }

    public String getSitemapDir() {
        return String.format("%s/%s", getPersonalFileStoreLocation(), "sitemap");
    }

    public String getBaseUrl() {
        String base = "http://" + getHostName();
        if (getPort() != 80) {
            base += ":" + getPort();
        }
        return base;
    }

    public String getBaseSecureUrl() {
        String base = "https://" + getHostName();
        if (getHttpsPort() != 443) {
            base += ":" + getHttpsPort();
        }
        return base;
    }

    public String getHostName() {
        return assistant.getStringProperty("app.hostname", DEFAULT_HOSTNAME);
    }

    public String getEmailHostName() {
        return assistant.getStringProperty("app.email.hostname", getHostName());
    }

    public int getPort() {
        return assistant.getIntProperty("app.port", DEFAULT_PORT);
    }

    public String getHelpUrl() {
        return assistant.getStringProperty("help.url", "http://dev.tdar.org/confluence/display/TDAR/User+Documentation");
    }

    public String getAboutUrl() {
        return assistant.getStringProperty("about.url", "http://www.tdar.org");
    }

    public String getCommentsUrl() {
        return assistant.getStringProperty("comments.url", "mailto:comments@tdar.org");
    }

    public String getFileStoreLocation() {
        return assistant.getStringProperty("file.store.location",
                "/home/tdar/filestore");
    }

    public String getPersonalFileStoreLocation() {
        return assistant.getStringProperty("personal.file.store.location", "/home/tdar/pfs");
    }

    public String getWebRoot() {
        return assistant.getStringProperty("web.root", "src/main/webapp");
    }

    public String getSmtpHost() {
        return assistant.getStringProperty("mail.smtp.host", DEFAULT_SMTP_HOST);
    }

    public String getSystemAdminEmail() {
        return assistant.getStringProperty("sysadmin.email", SYSTEM_ADMIN_EMAIL);
    }

    /*
     * One or many emails; split by semicolons
     */
    public String getBillingAdminEmail() {
        return assistant.getStringProperty("billing.admin.email", SYSTEM_ADMIN_EMAIL);
    }

    public String getServerEnvironmentStatus() {
        return assistant.getStringProperty("server.environment", "test");

    }

    public boolean getPrivacyControlsEnabled() {
        return assistant.getBooleanProperty("privacy.controls.enabled", false);

    }

    public boolean getLicenseEnabled() {
        return assistant.getBooleanProperty("licenses.enabled", false);
    }

    public boolean getCopyrightMandatory() {
        return assistant.getBooleanProperty("copyright.fields.enabled", false);
    }

    // TODO: make mapping props vendor neutral where possible (e.g. lat/long)
    public double getMapDefaultLat() {
        return assistant.getDoubleProperty("google.map.defaultLatitude", 40.00);

    }

    public double getMapDefaultLng() {
        return assistant.getDoubleProperty("google.map.defaultLongitude", -97.00);

    }

    public File getTempDirectory() {
        File file = new File(assistant.getStringProperty("tmp.dir", System.getProperty("java.io.tmpdir")));
        if (file.exists() && file.isDirectory()) {
            return file;
        }
        if (!file.mkdirs()) {
            String msg = COULDN_T_CREATE_TEMPORARY_DIRECTORY_AT + file.getAbsolutePath();
            logger.warn(msg);
            throw new IllegalStateException(msg);
        }
        return file;
    }

    public Filestore getFilestore() {
        return filestore;
    }

    public boolean shouldRunPeriodicEvents() {
        return assistant.getBooleanProperty("run.periodic.events", false);
    }

    /**
     * @return
     */
    public boolean useExternalMessageQueue() {
        return assistant.getBooleanProperty("message.queue.enabled", false);
    }

    public String getMessageQueueURL() {
        return assistant.getStringProperty("message.queue.server", "dev.tdar.org");
    }

    public String getMessageQueueUser() {
        return assistant.getStringProperty("message.queue.user");
    }

    public String getMessageQueuePwd() {
        return assistant.getStringProperty("message.queue.pwd");
    }

    public String getQueuePrefix() {
        return assistant.getStringProperty("message.queue.prefix", getDefaultQueuePrefix());
    }

    /**
     * @return
     */
    public String getDefaultQueuePrefix() {
        String host = "";
        try {
            InetAddress localMachine = InetAddress.getLocalHost();
            host = localMachine.getHostName();
        } catch (UnknownHostException e) {
            logger.debug("unknownhost: ", e);
        }
        return host + ".";
    }

    private void testQueue() {
        if (useExternalMessageQueue() && (StringUtils.isEmpty(getMessageQueuePwd()) || StringUtils.isEmpty(getMessageQueueUser()))) {
            throw new IllegalStateException(MESSAGE_QUEUE_IS_ENABLED_BUT_A_USERNAME_AND_PASSWORD_IS_NOT_DEFINED);
        }
    }

    /**
     * @return
     */
    public int getScrollableFetchSize() {
        return assistant.getIntProperty("scrollableResult.fetchSize", 100);
    }

    /**
     * @return
     */
    public int getIndexerFlushSize() {
        return assistant.getIntProperty("tdar.indexer.flushEvery", 500);
    }

    public int getAuthorityManagementDupeListMaxSize() {
        return assistant.getIntProperty("tdar.authorityManagement.dupeListMaxSize", DEFAULT_AUTHORITY_MANAGEMENT_DUPE_LIST_MAX_SIZE);
    }

    public int getAuthorityManagementMaxAffectedRecords() {
        return assistant.getIntProperty("tdar.authorityManagement.maxAffectedRecords", DEFAULT_AUTHORITY_MANAGEMENT_MAX_AFFECTED_RECORDS);
    }

    /**
     * @return the theme directory
     */
    public String getThemeDir() {
        String dir = assistant.getStringProperty("app.theme.dir", "includes/themes/tdar/");
        if (dir.startsWith("/")) {
            dir = dir.substring(1);
        }
        return dir;
    }

    public Set<String> getStopWords() {
        return stopWords;
    }

    public List<String> getCouponCodes() {
        return couponCodes;
    }

    public String getRecaptchaUrl() {
        return assistant.getStringProperty("recaptcha.host");
    }

    public String getGoogleMapsApiKey() {
        return assistant.getStringProperty("googlemaps.apikey");
    }

    public String getRecaptchaPrivateKey() {
        return assistant.getStringProperty("recaptcha.privateKey");
    }

    public String getRecaptchaPublicKey() {
        return assistant.getStringProperty("recaptcha.publicKey");
    }

    public String getRepositoryName() {
        return assistant.getStringProperty("oai.repository.name", "the Digital Archaeological Record");
    }

    public boolean enableTdarFormatInOAI() {
        return assistant.getBooleanProperty("oai.repository.enableTdarMetadataFormat", true);
    }

    public String getRepositoryNamespaceIdentifier() {
        return assistant.getStringProperty("oai.repository.namespace-identifier", "tdar.org");
    }

    public boolean getEnableEntityOai() {
        return assistant.getBooleanProperty("oai.repository.enableEntities", false);
    }

    // TODO: remove feature toggle when feature complete
    public boolean getLeftJoinDataIntegrationFeatureEnabled() {
        return assistant.getBooleanProperty("featureEnabled.leftJoinDataIntegration", false);
    }

    public String getSystemDescription() {
        return assistant
                .getStringProperty("oai.repository.description", DESCRIPTION);
    }

    public int getScheduledProcessStartId() {
        return assistant.getIntProperty("scheduled.startId", DEFAULT_SCHEDULED_PROCESS_START_ID);
    }

    public int getScheduledProcessBatchSize() {
        return assistant.getIntProperty("scheduled.batchSize", 100);
    }

    public Integer getScheduledProcessEndId() {
        return assistant.getIntProperty("scheduled.endId", DEFAULT_SCHEDULED_PROCESS_END_ID);
    }

    public String getGoogleAnalyticsId() {
        return assistant.getStringProperty("google.analytics", "");
    }

    public int getSessionCountLimitForBackgroundTasks() {
        return assistant.getIntProperty("scheduled.maxProcess", 6);
    }

    public int getSearchExcelExportRecordMax() {
        return assistant.getIntProperty("search.excel.export.recordMax", DEFAULT_SEARCH_EXCEL_EXPORT_RECORD_MAX);
    }

    public boolean isProductionEnvironment() {
        return PRODUCTION.equals(getServerEnvironmentStatus());
    }

    public int getMaxSpreadSheetRows() {
        return assistant.getIntProperty("excel.export.rowMax", USE_DEFAULT_EXCEL_ROWS);
    }

    public String getCulturalTermsHelpURL() {
        return assistant.getStringProperty("help.url.cultural", "http://dev.tdar.org/confluence/display/TDAR/Cultural+Terms");
    }

    public String getInvestigationTypesHelpURL() {
        return assistant.getStringProperty("help.url.investigation", "http://dev.tdar.org/confluence/display/TDAR/Investigation+Types");
    }

    public String getMaterialTypesHelpURL() {
        return assistant.getStringProperty("help.url.material", "http://dev.tdar.org/confluence/display/TDAR/Material+Types");
    }

    public String getSiteTypesHelpURL() {
        return assistant.getStringProperty("help.url.site", "http://dev.tdar.org/confluence/display/TDAR/Site+Types");
    }

    /*
     * Returns the collectionId to use for finding featured resources within
     * 
     * @default -1 -- used to say any collection
     */
    public Long getFeaturedCollectionId() {
        return assistant.getLongProperty("featured.collection.id", -1);
    }

    public String getDocumentationUrl() {
        return assistant.getStringProperty("help.baseurl", "http://dev.tdar.org/confluence/display/TDAR/User+Documentation");
    }

    public String getBugReportUrl() {
        return assistant.getStringProperty("bugreport.url", "http://dev.tdar.org/jira");
    }

    public String getCommentUrl() {
        return assistant.getStringProperty("comment.url", "mailto:comments@tdar.org");
    }

    public String getSiteAcronym() {
        return assistant.getStringProperty("site.acronym", "tDAR");
    }

    public String getSiteName() {
        return assistant.getStringProperty("site.name", "the Digital Archaeological Record");
    }

    public LicenseType getDefaultLicenseType() {
        return LicenseType.valueOf(assistant.getStringProperty("default.license.type", LicenseType.CREATIVE_COMMONS_ATTRIBUTION.name()));
    }

    public Boolean isRPAEnabled() {
        return assistant.getBooleanProperty("rpa.enabled", true);
    }

    public String getContactEmail() {
        return assistant.getStringProperty("app.contact.email", "info@digitalantiquity.org");
    }

    public String getNewsRssFeed() {
        return assistant.getStringProperty("news.rssFeed", "http://www.tdar.org/feed/");
    }

    public String getNewsUrl() {
        return assistant.getStringProperty("news.url", "http://www.tdar.org/news/");
    }

    public int getEmbargoPeriod() {
        return assistant.getIntProperty("embargo.period", 5);
    }

    public boolean isOdataEnabled() {
        return assistant.getBooleanProperty("odata.enabled", false);
    }

    public boolean isPayPerIngestEnabled() {
        return assistant.getBooleanProperty("pay.per.contribution.enabled", false);
    }

    public boolean isHttpsEnabled() {
        return assistant.getBooleanProperty("https.enabled", false);
    }

    public Integer getHttpsPort() {
        return assistant.getIntProperty("https.port", 443);
    }

    public Integer getMaxUploadFilesPerRecord() {
        return assistant.getIntProperty("upload.maxFilesPerResource", 50);
    }

    public Boolean isArchiveFileEnabled() {
        return assistant.getBooleanProperty("archive.enabled", false);
    }

    public boolean isVideoEnabled() {
        return assistant.getBooleanProperty("video.enabled", false);
    }

    public boolean isXmlExportEnabled() {
        return assistant.getBooleanProperty("xml.export.enabled", false);
    }

    public Boolean getShowJiraLink() {
        return assistant.getBooleanProperty("jira.link.show", true);
    }

    public String getJiraScriptLink() {
        return assistant.getStringProperty("jira.link", JIRA_LINK);
    }

    public boolean isViewRowSupported() {
        return assistant.getBooleanProperty("view.row.suppported", true);
    }

    public File getFremarkerTemplateDirectory() {
        return new File(assistant.getStringProperty("freemarker.templatedir", "includes/email/"));
    }

    public Long getGuestUserId() {
        return assistant.getLongProperty("guest.user.id", -1L);
    }

    public List<Long> getUserIdsToIgnoreInLargeTasks() {
        String users = assistant.getStringProperty("userids.to.ignore");
        List<Long> userIds = new ArrayList<Long>();
        for (String userid : users.split("[|,;]")) {
            try {
                if (StringUtils.isNotBlank(userid)) {
                    userIds.add(Long.parseLong(userid));
                }
            } catch (Exception e) {
                logger.warn("skipping: {} {}", userid, e);
            }
        }
        return userIds;
    }

    public boolean isGeoLocationToBeUsed() {
        return assistant.getBooleanProperty("is.geolocation.to.be.used", false);
    }

    public String getCreatorFOAFDir() {
        return getPersonalFileStoreLocation() + "/creatorInfo";
    }

    public String getCulturalTermsLabel() {
        return assistant.getStringProperty("cultural.terms.label", "Cultural Terms");
    }

    public int getDaysForCreatorProcess() {
        return assistant.getIntProperty("creator.analytics.days.to.process", 10);
    }

    /**
     * @return the directory that the kettle ETL tool will scan for input. If not defined will return the empty string.
     */
    public String getKettleInputPath() {
        return assistant.getStringProperty("kettle.input.path", "");
    }

}
