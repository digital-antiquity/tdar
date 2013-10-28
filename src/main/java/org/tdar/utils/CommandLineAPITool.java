/**
 * $Id$
 *
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.utils;

import static java.lang.System.err;
import static java.lang.System.exit;
import static java.lang.System.lineSeparator;
import static java.lang.System.out;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;

/**
 * http://thegenomefactory.blogspot.com.au/2013/08/minimum-standards-for-bioinformatics.html
 * @author Adam Brin
 */
public class CommandLineAPITool {

    // The following are the names of the fields on the APIController that we set
    /** Set in the query. Not really needed. */
    private static final String API_UPLOADED_ITEM = "uploadedItem";
    /** The access restrictions that is to be applied to the uploaded files */
    private static final String API_FIELD_FILE_ACCESS_RESTRICTION = "fileAccessRestriction";
    /** The list of files that are to have the access restriction applied to them */
    private static final String API_FIELD_RESTRICTED_FILES = "restrictedFiles";
    /** The list of attachment files */
    private static final String API_FIELD_UPLOAD_FILE = "uploadFile";
    /** The meta-data describing the files, an xml file itself, adhering to the published schema */
    private static final String API_FIELD_RECORD = "record";
    /** The project to which the files are to be added. Will overwrite anything within the record */
    private static final String API_FIELD_PROJECT_ID = "projectId";
    /** The billing account ID that the upload is to be charge against */
    private static final String API_FIELD_ACCOUNT_ID = "accountId";

    private static final String IMPORT_LOG_FILE = "import.log";
    private static final String HTTP_PROTOCOL = "http://";
    private static final String HTTPS_PROTOCOL = "https://";
    private static final String ALPHA_PASSWORD = "alpha";
    private static final String ALPHA_USER_NAME = "tdar";
    private static final int EXIT_ARGUMENT_ERROR = -1;
    private static final int EXIT_OK = 0;
    private static final String SITE_ACRONYM = "TDAR/FAIMS";
    private static final String TOOL_URL = "https://dev.tdar.org/confluence/display/TDAR/CommandLine+API+Tool";
    private static final String OPTION_HELP = "help";
    private static final String OPTION_FILE = "file";
    private static final String OPTION_CONFIG = "config";
    private static final String OPTION_PASSWORD = "password";
    private static final String OPTION_USERNAME = "username";
    private static final String OPTION_HOST = "host";
    private static final String OPTION_LOG_FILE = "logFile";
    private static final String OPTION_ACCOUNTID = "accountid";
    private static final String OPTION_SLEEP = "sleep";
    private static final String OPTION_PROJECT_ID = "projectid";
    private static final String OPTION_ACCESS_RESTRICTION = API_FIELD_FILE_ACCESS_RESTRICTION;
    private static final String ALPHA_TDAR_ORG = "alpha.tdar.org";
    @SuppressWarnings("unused")
    private static final String CORE_TDAR_ORG = "core.tdar.org";
    private static final String OPTION_HTTP = "http";
    private static final String OPTION_SHOW_LOG = "log";

    private static final Logger logger = Logger.getLogger(CommandLineAPITool.class);
    private DefaultHttpClient httpclient = new DefaultHttpClient();
    private String hostname = ALPHA_TDAR_ORG; // DEFAULT SHOULD NOT BE CORE
    private String username = ALPHA_USER_NAME;
    private String password = ALPHA_PASSWORD;
    private File logFile = new File(IMPORT_LOG_FILE);
    private Long projectId;
    private Long accountId;
    private long msSleepBetween;
    private FileAccessRestriction fileAccessRestriction = FileAccessRestriction.PUBLIC;
    private List<String> seen = new ArrayList<>();
    private String httpProtocol = HTTPS_PROTOCOL;
    private File[] files;

    /**
     * The exit codes have the following meaning:
     * <ul>
     * <li>-1 : there was a problem encountered in the parsing of the arguments
     * <li>0 : no issues were encountered and the run completed successfully
     * <li>any number > 0 : the number of files that the tool was not able to import successfully.
     * </ul>
     * 
     * @param args
     */
    public static void main(String[] args) {
        CommandLineAPITool importer = new CommandLineAPITool();
        Options options = buildCommandLineOptions();
        try {
            parseArguments(args, importer, options);
            importer.verifyState();
            int errorCount = importer.processFiles();
            if (errorCount > 0) {
                err.println("Exiting with errors...");
                exit(errorCount);
            }
        } catch (ParseException | IOException exp) {
            exp.printStackTrace();
            err.println("Exception: " + exp.getMessage());
            showHelpAndExit(SITE_ACRONYM, options, EXIT_ARGUMENT_ERROR);
        }
    }

    private static void parseArguments(String[] args, CommandLineAPITool importer, Options options) throws ParseException, IOException {
        logger.info("args are: " + Arrays.toString(args));
        CommandLineParser parser = new GnuParser();
        CommandLine line = parser.parse(options, args);
        if (hasNoOptions(line) || line.hasOption(OPTION_HELP)) {
            showHelpAndExit(SITE_ACRONYM, options, EXIT_OK);
        }
        if (hasUnrecognizedOptions(line)) {
            showHelpAndExit(SITE_ACRONYM, options, EXIT_ARGUMENT_ERROR);
        }
        if (line.hasOption(OPTION_SHOW_LOG)) {
            copyLogOutputToScreen();
        }
        if (line.hasOption(OPTION_HTTP)) {
            importer.setHttpProtocol(HTTP_PROTOCOL);
        }
        if (line.hasOption(OPTION_CONFIG)) {
            // by looking at this option first, we allow the command line to overwrite any of the values in the property file...
            setOptionsFromConfigFile(importer, line);
        }
        if (line.hasOption(OPTION_USERNAME)) {
            importer.setUsername(line.getOptionValue(OPTION_USERNAME));
        }
        if (line.hasOption(OPTION_HOST)) {
            err.println("Setting host to " + line.getOptionValue(OPTION_HOST));
            importer.setHostname(line.getOptionValue(OPTION_HOST));
        }
        if (line.hasOption(OPTION_PASSWORD)) {
            importer.setPassword(line.getOptionValue(OPTION_PASSWORD));
        }
        if (line.hasOption(OPTION_FILE)) {
            importer.setFiles(line.getOptionValues(OPTION_FILE));
        }
        if (line.hasOption(OPTION_PROJECT_ID)) {
            importer.setProjectId(new Long(line.getOptionValue(OPTION_PROJECT_ID)));
        }
        if (line.hasOption(OPTION_ACCOUNTID)) {
            importer.setAccountId(new Long(line.getOptionValue(OPTION_ACCOUNTID)));
        }
        if (line.hasOption(OPTION_SLEEP)) {
            importer.setMsSleepBetween(new Long(line.getOptionValue(OPTION_SLEEP)));
        }
        if (line.hasOption(OPTION_ACCESS_RESTRICTION)) {
            importer.setFileAccessRestriction(FileAccessRestriction.valueOf(line.getOptionValue(OPTION_ACCESS_RESTRICTION)));
        }
        if (line.hasOption(OPTION_LOG_FILE)) {
            importer.setLogFile(line.getOptionValue(OPTION_LOG_FILE));
        }
    }

    @SuppressWarnings("static-access")
    private static Options buildCommandLineOptions() {
        Options options = new Options();
        options.addOption(OptionBuilder.withArgName(OPTION_HELP).withDescription("print this message").create(OPTION_HELP));
        options.addOption(OptionBuilder.withArgName(OPTION_HTTP).withDescription("use the http protocol (default is https)").create(OPTION_HTTP));
        options.addOption(OptionBuilder.withArgName(OPTION_SHOW_LOG).withDescription("send the log output to the screen at the info level")
                .create(OPTION_SHOW_LOG));
        options.addOption(OptionBuilder.withArgName(OPTION_USERNAME).hasArg().withDescription(SITE_ACRONYM + " username")
                .create(OPTION_USERNAME));
        options.addOption(OptionBuilder.withArgName(OPTION_PASSWORD).hasArg().withDescription(SITE_ACRONYM + " password")
                .create(OPTION_PASSWORD));
        options.addOption(OptionBuilder.withArgName(OPTION_HOST).hasArg().withDescription("override default hostname of " + ALPHA_TDAR_ORG)
                .create(OPTION_HOST));
        options.addOption(OptionBuilder.withArgName(OPTION_FILE).hasArg().withDescription("the unique file(s) or directories to process")
                .create(OPTION_FILE));
        options.addOption(OptionBuilder.withArgName(OPTION_CONFIG).hasArg().withDescription("optional configuration file")
                .create(OPTION_CONFIG));
        options.addOption(OptionBuilder.withArgName(OPTION_PROJECT_ID).hasArg().withDescription("the project id. to associate w/ resource")
                .create(OPTION_PROJECT_ID));
        options.addOption(OptionBuilder.withArgName(OPTION_ACCOUNTID).hasArg().withDescription("the users billing account id to use")
                .create(OPTION_ACCOUNTID));
        options.addOption(OptionBuilder.withArgName(OPTION_SLEEP).hasArg().withDescription("the time to wait between server calls, in milliseconds")
                .create(OPTION_SLEEP));
        options.addOption(OptionBuilder.withArgName(OPTION_LOG_FILE).hasArg()
                .withDescription("the name of the file to record successful file tranfers to (defaults to " + IMPORT_LOG_FILE + ")")
                .create(OPTION_LOG_FILE));
        options.addOption(OptionBuilder.withArgName(OPTION_ACCESS_RESTRICTION).hasArg()
                .withDescription("the access restriction to be applied - one of [" + getFileAccessRestrictionChoices() + "]")
                .create(OPTION_ACCESS_RESTRICTION));
        return options;
    }

    private static void setOptionsFromConfigFile(CommandLineAPITool importer, CommandLine line) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(line.getOptionValue(OPTION_CONFIG)));
        // we only want to set the properties that are actually in the file
        for (Object key : properties.keySet()) {
            final String option = (String) key;
            switch (option) {
                case OPTION_HOST:
                    importer.setHostname(properties.getProperty(option));
                    break;
                case OPTION_USERNAME:
                    importer.setUsername(properties.getProperty(option));
                    break;
                case OPTION_PASSWORD:
                    importer.setPassword(properties.getProperty(option));
                    break;
                case OPTION_PROJECT_ID:
                    importer.setProjectId(new Long(properties.getProperty(option)));
                    break;
                case OPTION_ACCOUNTID:
                    importer.setAccountId(new Long(properties.getProperty(option)));
                    break;
                case OPTION_SLEEP:
                    importer.setMsSleepBetween(new Long(properties.getProperty(option)));
                    break;
                case OPTION_LOG_FILE:
                    importer.setLogFile(properties.getProperty(option));
                    break;
                case OPTION_ACCESS_RESTRICTION:
                    importer.setFileAccessRestriction(FileAccessRestriction.valueOf((properties.getProperty(option))));
                    break;
                case OPTION_FILE:
                    importer.setFiles(properties.getProperty(option).split(","));
                    break;
                default:
                    throw new IOException("unknown property found in config file: " + option);
            }
        }
    }

    private static void copyLogOutputToScreen() {
        Logger.getRootLogger().removeAllAppenders();
        ConsoleAppender console = new ConsoleAppender(new PatternLayout("%-5p [%t]: %m%n"));
        console.setName("console");
        console.setWriter(new OutputStreamWriter(System.out));
        logger.setLevel(Level.INFO);
        logger.addAppender(console);
    }

    private static String getFileAccessRestrictionChoices() {
        String result = "";
        FileAccessRestriction[] values = FileAccessRestriction.values();
        for (int i = 1; i <= values.length; i++) {
            result = result + values[i - 1];
            if (i < values.length) {
                result = result + " | ";
            }
        }
        return result;
    }

    private static boolean hasNoOptions(CommandLine line) {
        return line.getOptions().length == 0;
    }

    private static boolean hasUnrecognizedOptions(CommandLine line) {
        if (line.getArgs().length > 0) {
            err.println("Unrecognized arguments found: " + Arrays.toString(line.getArgs()));
            return true;
        }
        return false;
    }

    private static void showHelpAndExit(String siteAcronym, Options options, int exitCode) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(siteAcronym + " cli api tool", options);
        out.println("-------------------------------------------------------------------------------");
        out.println("Visit " + TOOL_URL + " for documentation on how to use the " + siteAcronym + " commandline API Tool");
        out.println("-------------------------------------------------------------------------------");
        exit(exitCode);
    }

    private void verifyState() throws ParseException {
        if (StringUtils.isEmpty(getHostname())) {
            throw new ParseException("No hostname specified");
        }
        if (StringUtils.isEmpty(getUsername())) {
            throw new ParseException("No username specified");
        }
        if (StringUtils.isEmpty(getPassword())) {
            throw new ParseException("No password specified");
        }
        if (files.length == 0) {
            throw new ParseException("Nothing to do, no files or directories specified...");
        }
        for (File path : files) {
            out.print("."); // give a visual indicator of how many files there are...
            if (!path.exists()) {
                throw new ParseException("Specified file does not exist: " + path);
            }
        }
        out.println(); // end of visual indicator
        for (int i = 0; i < files.length; i++) {
            File current = files[i];
            for (int j = i + 1; j < files.length; j++) {
                File other = files[j];
                if (current.getAbsolutePath().equals(other.getAbsolutePath())) {
                    throw new ParseException("Duplicate path detected: " + current);
                }
            }
        }
    }

    /**
     * process all the files that were read in from the command line, and any nested sub-directories.
     */
    private int processFiles() {

        int errorCount = 0;
        try {

            if (getHostname().equalsIgnoreCase(ALPHA_TDAR_ORG)) {
                AuthScope scope = new AuthScope(getHostname(), 80);
                UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials(ALPHA_USER_NAME, ALPHA_PASSWORD);
                httpclient.getCredentialsProvider().setCredentials(scope, usernamePasswordCredentials);
                logger.info("creating challenge/response authentication request for alpha");
                HttpGet tdarIPAuth = new HttpGet(httpProtocol + getHostname() + "/");
                logger.debug(tdarIPAuth.getRequestLine());
                HttpResponse response = httpclient.execute(tdarIPAuth);
                HttpEntity entity = response.getEntity();
                entity.consumeContent();
            }
            // make tdar authentication call
            HttpPost tdarAuth = new HttpPost(httpProtocol + getHostname() + "/login/process");
            List<NameValuePair> postNameValuePairs = new ArrayList<>();
            postNameValuePairs.add(new BasicNameValuePair("loginUsername", getUsername()));
            postNameValuePairs.add(new BasicNameValuePair("loginPassword", getPassword()));

            tdarAuth.setEntity(new UrlEncodedFormEntity(postNameValuePairs, HTTP.UTF_8));
            HttpResponse response = httpclient.execute(tdarAuth);
            HttpEntity entity = response.getEntity();
            logger.trace("Login form get: " + response.getStatusLine());
            logger.trace("Post logon cookies:");
            List<Cookie> cookies = httpclient.getCookieStore().getCookies();
            boolean sawCrowdAuth = false;
            if (cookies.isEmpty()) {
                logger.trace("None");
            } else {
                for (int i = 0; i < cookies.size(); i++) {
                    if (cookies.get(i).getName().equals("crowd.token_key"))
                        sawCrowdAuth = true;
                    logger.trace("- " + cookies.get(i).toString());
                }
            }

            if (!sawCrowdAuth) {
                logger.warn("unable to authenticate, check username and password " + getHostname());
                // exit(0);
            }
            logger.trace(EntityUtils.toString(entity));
            entity.consumeContent();

            for (File file : files) {
                out.print("*"); // give the user some sort of visual indicator as to progress
                errorCount += processDirectory(file);
            }
            out.println(); // end of progress indicator

        } catch (Exception e) {
            e.printStackTrace();
            errorCount++;
        }
        return errorCount;
    }

    /**
     * @param file
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    private int processDirectory(File parentDir) throws UnsupportedEncodingException, IOException {
        List<File> directories = new ArrayList<>();
        List<File> attachments = new ArrayList<>();
        List<File> records = new ArrayList<>();

        int errorCount = 0;
        if (parentDir.isDirectory()) {
            for (File file : parentDir.listFiles()) {
                if (file.isHidden())
                    continue;
                String fileName = file.getName();
                if (file.isDirectory()) {
                    directories.add(file);
                } else if (FilenameUtils.getExtension(fileName).equalsIgnoreCase("xml")) {
                    records.add(file);
                } else {
                    attachments.add(file);
                }
            }
        } else if (FilenameUtils.getExtension(parentDir.getName()).equalsIgnoreCase("xml")) {
            records.add(parentDir);
        } 

        // if there is more than one record in a directory after scanning of the directory is
        // complete, then ignore all files that are not xml records
        if (records.size() > 1) {
            logger.debug("processing multiple xml files ...  (ignoring attachments) " + records);
            for (File record : records) {
                if (!makeAPICall(record, null)) {
                    errorCount++;
                }
            }
        } else if (records.size() == 1) {
            logger.debug("processing : " + records);
            if (!makeAPICall(records.get(0), attachments)) {
                errorCount++;
            }
        }

        for (File directory : directories) {
            processDirectory(directory);
        }
        return errorCount;
    }

    public boolean makeAPICall(File record, List<File> attachments) throws UnsupportedEncodingException, IOException {
        String path = record.getPath();
        HttpPost apicall = new HttpPost(httpProtocol + getHostname() + "/api/upload?" + API_UPLOADED_ITEM + "=" + URLEncoder.encode(path, "UTF-8"));
        MultipartEntity reqEntity = new MultipartEntity();
        boolean callSuccessful = true;
        if (seen.contains(path)) {
            logger.debug("skipping: " + path);
        }
        reqEntity.addPart(API_FIELD_RECORD, new StringBody(FileUtils.readFileToString(record)));

        if (projectId != null) {
            logger.trace("setting " + API_FIELD_PROJECT_ID + ":" + projectId);
            reqEntity.addPart(API_FIELD_PROJECT_ID, new StringBody(projectId.toString()));
        }
        if (accountId != null) {
            logger.trace("setting " + API_FIELD_ACCOUNT_ID + ":" + accountId);
            reqEntity.addPart(API_FIELD_ACCOUNT_ID, new StringBody(accountId.toString()));
        }

        reqEntity.addPart(API_FIELD_FILE_ACCESS_RESTRICTION, new StringBody(getFileAccessRestriction().name()));

        if (!CollectionUtils.isEmpty(attachments)) {
            for (int i = 0; i < attachments.size(); i++) {
                reqEntity.addPart(API_FIELD_UPLOAD_FILE, new FileBody(attachments.get(i)));
                if (getFileAccessRestriction().isRestricted()) {
                    reqEntity.addPart(API_FIELD_RESTRICTED_FILES, new StringBody(attachments.get(i).getName()));
                }
            }
        }

        apicall.setEntity(reqEntity);
        logger.debug("      files: " + StringUtils.join(attachments, ", "));

        HttpResponse response = httpclient.execute(apicall);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode >= HttpStatus.SC_BAD_REQUEST) {
            err.println("Server returned error: [" + record.getAbsolutePath() + "]:" + response.getStatusLine().getReasonPhrase());
            callSuccessful = false;
        }
        logger.info(record.toString() + " - " + response.getStatusLine());
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            String resp =  StringEscapeUtils.unescapeHtml4(EntityUtils.toString(entity));
            entity.consumeContent();
            if (resp != null && resp != "") {
                logger.info(resp);
            }
        }

        FileUtils.writeStringToFile(getLogFile(), path + " successful: " + callSuccessful + lineSeparator(), true);
        logger.info("done: " + path);
        try {
            Thread.sleep(msSleepBetween);
        } catch (Exception e) {
            // we woke up early...
        }
        return callSuccessful;
    }

    /**
     * @param hostname
     *            the hostname to set
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * @return the hostname
     */
    public String getHostname() {
        return hostname;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    private void setMsSleepBetween(Long msSleepBetween) {
        this.msSleepBetween = msSleepBetween;
    }

    public File getLogFile() {
        return logFile;
    }

    public void setLogFile(String logFile) throws IOException {
        this.logFile = new File(logFile);
        getSeen().addAll(FileUtils.readLines(this.logFile));
    }

    public List<String> getSeen() {
        return seen;
    }

    public void setSeen(List<String> seen) {
        this.seen = seen;
    }

    public FileAccessRestriction getFileAccessRestriction() {
        return fileAccessRestriction;
    }

    public void setFileAccessRestriction(FileAccessRestriction fileAccessRestriction) {
        this.fileAccessRestriction = fileAccessRestriction;
    }

    /**
     * @param httpProtocol
     *            the httpProtocol to set
     */
    private void setHttpProtocol(String httpProtocol) {
        this.httpProtocol = httpProtocol;
    }

    private void setFiles(String[] filenames) {
        files = new File[filenames.length];
        for (int i = 0; i < filenames.length; i++) {
            files[i] = new File(filenames[i].trim());
        }
    }

}
