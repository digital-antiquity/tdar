package org.tdar.core.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tdar.core.bean.resource.LicenseType;
import org.tdar.filestore.Filestore;
import org.tdar.filestore.PairtreeFilestore;

import com.amazonaws.regions.Regions;

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
public class TdarConfiguration extends AbstractConfigurationFile {

    static final List<String> STOP_WORDS = Arrays.asList("the", "and", "a", "to", "of", "in", "i", "is", "that", "it", "on", "you", "this", "for",
            "but", "with", "are", "have", "be", "at", "or", "as", "was", "so", "if", "out", "not", "like");
    private static final String JIRA_LINK = "issues.tdar.org/s/en_USgh0sw9-418945332/844/18/1.2.9/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector.js?collectorId=959f12a3";
    private static final String SECURITY_EXCEPTION_COULD_NOT_CREATE_PERSONAL_FILESTORE_HOME_DIRECTORY = "Security Exception: could not create personal filestore home directory";
    public static final String COULDN_T_CREATE_TEMPORARY_DIRECTORY_AT = "Couldn't create temporary directory at : ";

    public static final int DEFAULT_AUTHORITY_MANAGEMENT_DUPE_LIST_MAX_SIZE = 50;
    public static final int DEFAULT_AUTHORITY_MANAGEMENT_MAX_AFFECTED_RECORDS = 1000;

    public static final int DEFAULT_SEARCH_EXCEL_EXPORT_RECORD_MAX = 1000;

    private final transient static Logger logger = LoggerFactory.getLogger(TdarConfiguration.class);
    private ConfigurationAssistant assistant;

    private Filestore filestore;

    private List<String> stopWords = new ArrayList<>();
    private List<String> couponCodes = new ArrayList<>();

    private String configurationFile;

    private final static TdarConfiguration INSTANCE = new TdarConfiguration();
    public static final String PRODUCTION = "production";
    private static final int USE_DEFAULT_EXCEL_ROWS = -1;
    private static final String[] defaultColors = new String[] { "#2C4D56", "#EBD790", "#4B514D", "#C3AA72", "#DC7612", "#BD3200", "#A09D5B", "#F6D86B",
            "#660000", "#909D5B" };

    private TdarConfiguration() {
        this("/tdar.properties");
    }

    public void printConfig() {
        logger.info("---------------------------------------------");
        logger.info("| Name:{} ({})", getRepositoryName(), isProductionEnvironment());
        logger.info("| ");
        logger.info("| HostName: {}  SecureHost: {}", getBaseUrl(), getBaseSecureUrl());
        logger.info("| CDN Host: {} (enabled: {})", getStaticContentHost(), isStaticContentEnabled());
        logger.info("| MailHost: {} (override to for testing: {}", getSmtpHost(), isSendEmailToTester());
        logger.info("| ");
        logger.info("| Storage:");
        logger.info("| FileStoreLocation: {}", getFileStoreLocation());
        logger.info("| PersonalFileStoreLocation: {}", getPersonalFileStoreLocation());
        logger.info("| HostedFileStoreLocation: {}", getHostedFileStoreLocation());
        logger.info("| ");
        logger.info("| RunScheduledProcesses: {}", shouldRunPeriodicEvents());
        logger.info("| PayPerIngest: {}", isPayPerIngestEnabled());
        logger.info("| CORS Hosts: {} ({})", getAllAllowedDomains(), getContentSecurityPolicyEnabled());
        logger.info("---------------------------------------------");
    }

    /**
     * Write the current properties to a the supplied outputstream.
     * 
     * @param outs
     *            stream to receive properties
     * @param comments
     *            Comment line to include at beginning of output
     */
    public void store(OutputStream outs, String comments) throws IOException {
        assistant.getProperties().store(outs, comments);
    }

    /**
     * Write current properties to stdout.
     * 
     * @throws IOException
     */
    public void store() throws IOException {
        // FIXME: this is kinda worthless because we don't include default properties.
        store(System.out, "Current tDAR Configuration");
    }

    public static void main(String[] args) {
        try {
            getInstance().store();
        } catch (IOException e) {
            System.err.println("Could not write properties.");
            System.exit(1);
        }
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
    }

    /**
     * Called separately so we can better handle configuration exceptions
     */
    public void initialize() {
        logger.debug("initializing filestore and setup");

        // initializeTimeZoneInfo();

        if (isProductionEnvironment()) {
            printConfig();
        }
        initializeStopWords();
        intializeCouponCodes();

//        if (isPayPerIngestEnabled() && !isHttpsEnabled()) {
//            throw new IllegalStateException("cannot run with pay-per-ingest enabled and https disabled");
//        }

        File filestoreLoc = new File(getFileStoreLocation());
        if (!filestoreLoc.exists()) {
            throw new IllegalStateException("could not create filestore path:" + filestoreLoc.getAbsolutePath());
        }

        initPersonalFilestorePath();
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
            stopWords.addAll(IOUtils.readLines(new FileInputStream(assistant.getStringProperty("lucene.stop.words.file")), Charset.defaultCharset()));
        } catch (Exception e) {
            stopWords.addAll(STOP_WORDS);
        }
    }

    private void intializeCouponCodes() {
        try {
            couponCodes.addAll(IOUtils.readLines(new FileInputStream(assistant.getStringProperty("coupon.codes.file")), Charset.defaultCharset()));
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
                    "tufa", "tumulus", "type", "typology", "varves", "ware", "ziggurat", "zone", "adovasio", "aharoni", "akbar", "akurgal", "alarcao",
                    "albright", "alcock", "allen", "alp", "amiran", "anderson", "andronicos", "archaeologist", "artamonov", "arik", "aston", "atkinson",
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
        String filestoreClassName = assistant.getStringProperty("file.store.class", PairtreeFilestore.class.getCanonicalName());
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

    boolean personalFilestorePathInitialized = false;
    private Properties changesetProps;

    // verify that the personal filestore location exists, attempt to make it if it doesn't, and System.exit() if that fails
    private void initPersonalFilestorePath() {
        if (personalFilestorePathInitialized) {
            return;
        }
        personalFilestorePathInitialized = true;
        File personalFilestoreHome = new File(getPersonalFileStoreLocation());
        String msg = null;
        try {
            logger.info("initializing personal filestore at {}", getPersonalFileStoreLocation());
            if (!personalFilestoreHome.exists()) {
                boolean pathExists = personalFilestoreHome.mkdirs();
                if (!pathExists) {
                    msg = "Could not create personal filestore at " + getPersonalFileStoreLocation();
                }
            }
        } catch (SecurityException ex) {
            logger.error(SECURITY_EXCEPTION_COULD_NOT_CREATE_PERSONAL_FILESTORE_HOME_DIRECTORY, ex);
            throw new IllegalStateException(msg);
        }
    }

    // FIXME: change to use + encorpearate sitemap (TDAR-4703)
    @SuppressWarnings("unused")
    private void initFilestorePath(String location) {
        if (personalFilestorePathInitialized) {
            return;
        }
        personalFilestorePathInitialized = true;
        File personalFilestoreHome = new File(location);
        String msg = null;
        try {
            logger.info("initializing personal filestore at {}", location);
            if (!personalFilestoreHome.exists()) {
                boolean pathExists = personalFilestoreHome.mkdirs();
                if (!pathExists) {
                    msg = "Could not create personal filestore at " + location;
                }
            }
        } catch (SecurityException ex) {
            logger.error(SECURITY_EXCEPTION_COULD_NOT_CREATE_PERSONAL_FILESTORE_HOME_DIRECTORY, ex);
            throw new IllegalStateException(msg);
        }
    }

    public static TdarConfiguration getInstance() {
        return INSTANCE;
    }

    public String getSitemapDir() {
        return String.format("%s/%s", getHostedFileStoreLocation(), "sitemap");
    }

    public String getBaseUrl() {
        String base = "http://" + getHostName();
        if (getPort() != DEFAULT_PORT) {
            base += ":" + getPort();
        }
        if (StringUtils.isNotBlank(getContextPath())) {
            base += getContextPath();
        }
        return base;
    }

    public String getContextPath() {
        return "";
    }

    public String getStaticContentBaseUrl() {
        String base = "http://" + getStaticContentHost();
        if (getStaticContentPort() != DEFAULT_PORT) {
            base += ":" + getStaticContentPort();
        }
        return base;
    }

    public String getBaseSecureUrl() {
        String base = "https://" + getHostName();
        if (getHttpsPort() != HTTPS_PORT_DEFAULT) {
            base += ":" + getHttpsPort();
        }
        if (StringUtils.isNotBlank(getContextPath())) {
            base += getContextPath();
        }
        return base;
    }

    public String getHelpUrl() {
        return assistant.getStringProperty("help.url", getDocRoot() + "Documentation+Home");
    }

    public String getAboutUrl() {
        return assistant.getStringProperty("about.url", "http://www.tdar.org");
    }

    public String getCommentsUrl() {
        return assistant.getStringProperty("comments.url", "mailto:comments@tdar.org");
    }

    public String getFileStoreLocation() {
        return assistant.getStringProperty("file.store.location", "/home/tdar/filestore");
    }

    public String getPersonalFileStoreLocation() {
        return assistant.getStringProperty("personal.file.store.location", "/home/tdar/personal-filestore");
    }

    public String getHostedFileStoreLocation() {
        return assistant.getStringProperty("hosted.file.store.location", "/home/tdar/hosted-filestore");
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
        String dir = assistant.getStringProperty("app.theme.dir", "/WEB-INF/");
        if ((dir.startsWith("/") || dir.startsWith("\\")) && !dir.startsWith("/WEB-INF")) {
            dir = dir.substring(1);
        }
        if (dir.endsWith("/") || dir.startsWith("\\")) {
            dir = dir.substring(0, dir.length() - 1);
        }
        return dir;
    }

    public List<String> getStopWords() {
        if (CollectionUtils.isEmpty(stopWords)) {
            initializeStopWords();
        }
        return stopWords;
    }

    public List<String> getCouponCodes() {
        if (CollectionUtils.isEmpty(couponCodes)) {
            intializeCouponCodes();
        }
        return couponCodes;
    }

    public String getRecaptchaUrl() {
        return assistant.getStringProperty("recaptcha.host");
    }

    public String getGoogleMapsApiKey() {
        return assistant.getStringProperty("googlemaps.apikey");
    }

    public String getLeafletMapsApiKey() {
        return assistant.getStringProperty("leaflet.apikey");
    }

    public String getRecaptchaPrivateKey() {
        return assistant.getStringProperty("recaptcha.privateKey");
    }

    public String getRecaptchaPublicKey() {
        return assistant.getStringProperty("recaptcha.publicKey");
    }

    // TODO: remove feature toggle when feature complete
    public boolean getLeftJoinDataIntegrationFeatureEnabled() {
        return assistant.getBooleanProperty("featureEnabled.leftJoinDataIntegration", false);
    }

    public int getScheduledProcessStartId() {
        return assistant.getIntProperty("scheduled.startId", -1);
    }

    public int getScheduledProcessBatchSize() {
        return assistant.getIntProperty("scheduled.batchSize", 100);
    }

    public int getScheduledProcessEndId() {
        return assistant.getIntProperty("scheduled.endId", -1);
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
        return assistant.getStringProperty("help.url.cultural", getDocRoot() + "Cultural+Terms");
    }

    public String getInvestigationTypesHelpURL() {
        return assistant.getStringProperty("help.url.investigation", getDocRoot() + "Investigation+Types");
    }

    public String getMaterialTypesHelpURL() {
        return assistant.getStringProperty("help.url.material", getDocRoot() + "Material+Types");
    }

    public String getSiteTypesHelpURL() {
        return assistant.getStringProperty("help.url.site", getDocRoot() + "Site+Types");
    }

    /*
     * Returns the collectionId to use for finding featured resources within
     * 
     * @default -1 -- used to say any collection
     */
    public Long getFeaturedCollectionId() {
        return assistant.getLongProperty("featured.collection.id", -1);
    }

    public String getDocRoot() {
        return assistant.getStringProperty("help.root", "https://docs.tdar.org/display/TDAR/");
    }

    public String getDocumentationUrl() {
        return assistant.getStringProperty("help.baseurl", getDocRoot() + "Documentation+Home");
    }

    public String getResourceCreatorRoleDocumentationUrl() {
        return assistant.getStringProperty("help.resourceCreatorRole", getDocRoot() + "Resource+Creator+Roles");
    }

    public String getIntegrationDocumentationUrl() {
        return assistant.getStringProperty("help.integrationUrl", getDocRoot() + "Documentation+Home");
    }

    public String getBugReportUrl() {
        return assistant.getStringProperty("bugreport.url", "https://issues.tdar.org");
    }

    public String getCommentUrl() {
        return assistant.getStringProperty("comment.url", "mailto:comments@tdar.org");
    }

    public String getSiteAcronym() {
        return assistant.getStringProperty("site.acronym", "tDAR");
    }

    public String getServiceProvider() {
        return assistant.getStringProperty("service.provider", "Digital Antiquity");
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
        return assistant.getStringProperty("news.rssFeed", "https://www.tdar.org/feed/");
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
        return assistant.getIntProperty("https.port", HTTPS_PORT_DEFAULT);
    }

    public Integer getMaxUploadFilesPerRecord() {
        return assistant.getIntProperty("upload.maxFilesPerResource", 50);
    }

    public Boolean isArchiveFileEnabled() {
        return assistant.getBooleanProperty("archive.enabled", false);
    }

    /**
     * @return true if <b>Video</b> <i>and</i> <b>Audio</b> are enabled, false otherwise.
     */
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
        List<Long> userIds = new ArrayList<>();
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

    public String getFileCacheDirectory() {
        return getPersonalFileStoreLocation() + "/fileCache";
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

    public String getContributorAgreementUrl() {
        return assistant.getStringProperty("contributor.url", "http://www.tdar.org/about/policies/contributors-agreement/");
    }

    public String getPrivacyPolicyUrl() {
        return assistant.getStringProperty("privacy.url", "http://www.tdar.org/about/policies/privacy-policy/");
    }

    public String getTosUrl() {
        return assistant.getStringProperty("tos.url", "http://www.tdar.org/about/policies/terms-of-use/");
    }

    public int getContributorAgreementLatestVersion() {
        return assistant.getIntProperty("contributor.agreement.version", 0);
    }

    public int getTosLatestVersion() {
        return assistant.getIntProperty("tos.version", 0);
    }

    /**
     * Introduced by TDAR-1978
     * 
     * @return true if tdar.properties has the property "switchable.map.obfuscation" set to true, false in all other cases.
     */
    public boolean isSwitchableMapObfuscation() {
        return assistant.getBooleanProperty("switchable.map.obfuscation", true);
    }

    public boolean obfuscationInterceptorDisabled() {
        return assistant.getBooleanProperty("obfuscation.interceptor.disabled", false);
    }

    public String getDefaultFromEmail() {
        return assistant.getStringProperty("email.default.from", FROM_EMAIL_NAME + getEmailHostName());
    }

    public boolean isJaiImageJenabled() {
        return assistant.getBooleanProperty("jai.imagej.enabled", true);
    }

    public boolean isStaticContentEnabled() {
        return assistant.getBooleanProperty("static.content.enabled", false);
    }

    public int getStaticContentSSLPort() {
        return assistant.getIntProperty("static.content.sslPort", HTTPS_PORT_DEFAULT);
    }

    public int getStaticContentPort() {
        return assistant.getIntProperty("static.content.port", DEFAULT_PORT);
    }

    public String getStaticContentHost() {
        return assistant.getStringProperty("static.content.host", getHostName());
    }

    public String getContentSecurityPolicyAdditions() {
        return assistant.getStringProperty("content.security.policy.additions", "");
    }

    public boolean getContentSecurityPolicyEnabled() {
        return assistant.getBooleanProperty("content.security.policy.enabled", false);
    }

    public boolean ignoreMissingFilesInFilestore() {
        return assistant.getBooleanProperty("filestore.ignoreMissing", false);
    }

    public String getAllAllowedDomains() {
        List<String> baseUrls = new ArrayList<>();
        baseUrls.add(getBaseSecureUrl());
        baseUrls.add(getBaseUrl());
        List<String> hosts = new ArrayList<>(Arrays.asList(getStaticContentHost(), "googleapis.com", "netda.bootstrapcdn.com", "ajax.aspnetcdn.com",
                "typekit.com", "api.recaptcha.net"));
        for (String term : StringUtils.split(getContentSecurityPolicyAdditions(), " ")) {
            term = StringUtils.trim(term);
            if (StringUtils.isBlank(term)) {
                continue;
            }

            if (term.startsWith("http")) {
                baseUrls.add(term);
            } else {
                hosts.add(term);
            }
        }
        for (String url : hosts) {
            url = StringUtils.trim(url);
            if (StringUtils.isBlank(url) || url.equals("\"\"")) {
                continue;
            }
            baseUrls.add("http://" + url);
            baseUrls.add("https://" + url);
        }
        String result = StringUtils.join(baseUrls, ",");
        return result;
    }

    public boolean shouldThrowExceptionOnConcurrentUserDownload() {
        return assistant.getBooleanProperty("exception.on.bad.download", false);
    }

    public boolean shouldUseCDN() {
        return assistant.getBooleanProperty("use.cdn", true);
    }

    public boolean shouldAutoDownload() {
        return assistant.getBooleanProperty("js.autodownload", true);
    }

    public boolean isSendEmailToTester() {
        boolean dflt = false;
        if (!isProductionEnvironment()) {
            dflt = true;
        }
        return assistant.getBooleanProperty("email.to.tester", dflt);
    }

    public String getURLRewriteRefresh() {
        return Integer.toString(assistant.getIntProperty("urlRewrite.refresh", -1));
    }

    public String getRequestTokenName() {
        return assistant.getStringProperty("auth.token.name", "crowd.token_key");
    }

    public Boolean allowAuthentication() {
        return assistant.getBooleanProperty("allow.authentication", true);
    }

    public List<String> getAdminUsernames() {
        String names_ = assistant.getProperty("allow.authentication.admin.users", "");
        List<String> names = new ArrayList<>();
        for (String name : StringUtils.split(names_, ",")) {
            if (StringUtils.isNotBlank(name)) {
                names.add(StringUtils.trim(name));
            }
        }
        return names;
    }

    public Integer getIntegrationPreviewSizePerDataTable() {
        return 10;
    }

    public boolean isTest() {
        if (isProductionEnvironment()) {
            return false;
        }
        return assistant.getBooleanProperty("is.test", false);
    }

    public boolean isPrettyPrintJson() {
        return assistant.getBooleanProperty("use.verbose.json", false);
    }

    public int getTdarDataBatchSize() {
        return assistant.getIntProperty("tdardata.batch_size", 5000);
    }

    public int getDownloadBufferSize() {
        return assistant.getIntProperty("download.buffer_size", 2048);
    }

    @Deprecated
    public void setConfigurationFile(File configFile) {
        assistant = new ConfigurationAssistant();
        assistant.loadProperties(configFile);
        this.configurationFile = configFile.getName();
        filestore = loadFilestore();
        initPersonalFilestorePath();
    }

    @Override
    protected ConfigurationAssistant getAssistant() {
        return assistant;
    }

    public String getStaticContext() {
        return assistant.getStringProperty("static.context", "");
    }

    public boolean shouldUseLowMemoryPDFMerger() {
        return assistant.getBooleanProperty("pdf.use_low_mem", true);
    }

    public boolean shouldLogToFilestore() {
        return assistant.getBooleanProperty("log.to.filestore", true);
    }

    public Long getAdminUserId() {
        return assistant.getLongProperty("admin.userid", 135028);
    }

    public Long getAdminBillingAccountId() {
        return assistant.getLongProperty("admin.billingAccount", 216);
    }

    public boolean isFilestoreReadOnly() {
        return assistant.getBooleanProperty("filestore.readonly", false);
    }

    public boolean tagEnabled() {
        return assistant.getBooleanProperty("tag.enabled", false);
    }

    public boolean tagEmbedded() {
        if (tagEnabled()) {
            return assistant.getBooleanProperty("tag.embedded", false);
        }
        return true;
    }

    public List<String> getBarColors() {
        return Arrays.asList(assistant.getStringArray("tdar.colors", defaultColors));
    }

    public boolean useTransactionalEvents() {
        return assistant.getBooleanProperty("transactional.events", true);
    }

    public Long getSAAContactId() {
        return assistant.getLongProperty("saa.contact_id", getAdminUserId());
    }

    public boolean isSelenium() {
        return assistant.getBooleanProperty("is.selenium", false);
    }

    public boolean includeSpecialCodingRules() {
        return assistant.getBooleanProperty("integration.special_coding_rules", true);
    }

    public boolean isSelect2Enabled() {
        return assistant.getBooleanProperty("select2.enabled", false);
    }

    public boolean isSelect2SingleEnabled() {
        return assistant.getBooleanProperty("select2.single.enabled", false);
    }

    public boolean shouldShowExactLocationToThoseWhoCanEdit() {
        return assistant.getBooleanProperty("show.exact.location.to.editable", false);
    }

    public MemoryUsageSetting getPDFMemoryReadSetting() {
        return MemoryUsageSetting.setupTempFileOnly();
    }

    public MemoryUsageSetting getPDFMemoryWriteSetting(File file) {
        if (TdarConfiguration.getInstance().shouldUseLowMemoryPDFMerger()) {
            return MemoryUsageSetting.setupMixed(Runtime.getRuntime().freeMemory() / 5L);
        } else {
            return MemoryUsageSetting.setupMainMemoryOnly();
        }
    }

    public boolean ssoEnabled() {
        return assistant.getBooleanProperty("sso.enabled", true);
    }

    public boolean useMapInNodeParticipation() {
        return assistant.getBooleanProperty("experimental.map_node_participation", false);
    }

    public Long getMaxTranslatedFileSize() {
        return assistant.getLongProperty("max.translated_file_size", 75_000_000);
    }

    public int getDataIntegrationMaximumDataTables() {
        return assistant.getIntProperty("data.integration.maxDataTables", 55);
    }

    public int getDataIntegrationMaximumColumns() {
        return assistant.getIntProperty("data.integration.maxOutputColumns", 35);
    }

    public boolean isListCollectionsEnabled() {
        return false;
    }

    public String getAwsAccessKey() {
        return assistant.getStringProperty("aws.accesskey.id");
    }

    public String getAwsSecretKey() {
        return assistant.getStringProperty("aws.accesskey.secret");
    }

    public String getCharacterSet() {
        return assistant.getStringProperty("aws.characterset");
    }

    public String getAwsQueueName() {
        return assistant.getStringProperty("aws.queuename");
    }

    public Regions getAwsRegion() {
        try {
            String key = assistant.getStringProperty("aws.region");
            return Regions.valueOf(key);
        } catch (NullPointerException | IllegalArgumentException e) {
            return Regions.US_WEST_2;
        }
    }

    public String getEmailAttachmentsDirectory() {
        // TODO populate this from tdar.properties.
        return assistant.getStringProperty("email.attachments.location", "/home/tdar/email-attachments/");
    }

    public String getStaffEmail() {
        return assistant.getStringProperty("app.staff.email", "staff@digitalantiquity.org");
    }

    public String getDeveloperTestEmail() {
        return assistant.getStringProperty("email.developer.test", "test@tdar.org");
    }



    public Properties loadChangesetProps() {
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


}
