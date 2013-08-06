/**
 * $Id$
 *
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
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
import org.apache.log4j.Logger;
import org.tdar.core.bean.resource.InformationResourceFile.FileAccessRestriction;

/**
 * @author Adam Brin
 * 
 */
public class CommandLineAPITool {

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
    private static final String OPTION_ACCESS_RESTRICTION = "fileAccessRestriction";
    private static final String ALPHA_TDAR_ORG = "alpha.tdar.org";
    @SuppressWarnings("unused")
    private static final String CORE_TDAR_ORG = "core.tdar.org";
    private static final String OPTION_HTTP = "http";

    private static final Logger logger = Logger.getLogger(CommandLineAPITool.class);
    private DefaultHttpClient httpclient = new DefaultHttpClient();
    private String hostname = ALPHA_TDAR_ORG; // DEFAULT SHOULD NOT BE CORE
    private String username = ALPHA_USER_NAME;
    private String password = ALPHA_PASSWORD;
    private File logFile = new File("import.log");
    private Long projectId;
    private Long accountId;
    private long msSleepBetween;
    private FileAccessRestriction fileAccessRestriction = FileAccessRestriction.PUBLIC;
    private List<String> seen = new ArrayList<>();
    private String httpProtocol = HTTPS_PROTOCOL;

    /**
     * return codes
     */

    // TODO: get rid of magic numbers

    /**
     * @param args
     */
    @SuppressWarnings("static-access")
    public static void main(String[] args) {
        CommandLineAPITool importer = new CommandLineAPITool();

        Options options = new Options();
        options.addOption(OptionBuilder.withArgName(OPTION_HELP).withDescription("print this message").create(OPTION_HELP));
        options.addOption(OptionBuilder.withArgName(OPTION_HTTP).withDescription("use the http protocol (default is https)").create(OPTION_HTTP));
        options.addOption(OptionBuilder.withArgName(OPTION_USERNAME).hasArg().withDescription(SITE_ACRONYM + " username")
                .create(OPTION_USERNAME));
        options.addOption(OptionBuilder.withArgName(OPTION_PASSWORD).hasArg().withDescription(SITE_ACRONYM + " password")
                .create(OPTION_PASSWORD));
        options.addOption(OptionBuilder.withArgName(OPTION_HOST).hasArg().withDescription("override default hostname of " + ALPHA_TDAR_ORG)
                .create(OPTION_HOST));
        options.addOption(OptionBuilder.withArgName(OPTION_FILE).hasArg().withDescription("the file(s) or directories to process")
                .create(OPTION_FILE));
        options.addOption(OptionBuilder.withArgName(OPTION_CONFIG).hasArg().withDescription("optional configuration file")
                .create(OPTION_CONFIG));
        options.addOption(OptionBuilder.withArgName(OPTION_PROJECT_ID).hasArg().withDescription(SITE_ACRONYM + " project id. to associate w/ resource")
                .create(OPTION_PROJECT_ID));
        options.addOption(OptionBuilder.withArgName(OPTION_ACCOUNTID).hasArg().withDescription(SITE_ACRONYM + " the users billing account id to use")
                .create(OPTION_ACCOUNTID));
        options.addOption(OptionBuilder.withArgName(OPTION_SLEEP).hasArg().withDescription(SITE_ACRONYM + " timeToSleep")
                .create(OPTION_SLEEP));
        options.addOption(OptionBuilder.withArgName(OPTION_LOG_FILE).hasArg().withDescription(SITE_ACRONYM + " logFile")
                .create(OPTION_LOG_FILE));
        options.addOption(OptionBuilder.withArgName(OPTION_ACCESS_RESTRICTION).hasArg()
                .withDescription("the access restriction to be applied - one of [" + getFileAccessRestrictionChoices() + "]")
                .create(OPTION_ACCESS_RESTRICTION));
        CommandLineParser parser = new GnuParser();

        String[] filenames = {};
        try {
            // parse the command line arguments
            System.err.println("args are: " + Arrays.toString(args));
            CommandLine line = parser.parse(options, args);

            if (hasNoOptions(line) || line.hasOption(OPTION_HELP)) {
                showHelpAndExit(SITE_ACRONYM, options, EXIT_OK);
            }
            
            if (hasUnrecognizedOptions(line)) {
                showHelpAndExit(SITE_ACRONYM, options, EXIT_ARGUMENT_ERROR);
            }
            
            if (line.hasOption(OPTION_HTTP)) {
                importer.setHttpProtocol(HTTP_PROTOCOL);
            }

            if (line.hasOption(OPTION_USERNAME)) {
                importer.setUsername(line.getOptionValue(OPTION_USERNAME));
            }

            if (line.hasOption(OPTION_HOST)) {
                System.err.println("Setting host to " + line.getOptionValue(OPTION_HOST));
                importer.setHostname(line.getOptionValue(OPTION_HOST));
            }

            if (line.hasOption(OPTION_PASSWORD)) {
                importer.setPassword(line.getOptionValue(OPTION_PASSWORD));
            }

            if (line.hasOption(OPTION_FILE)) {
                filenames = line.getOptionValues(OPTION_FILE);
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
                try {
                    importer.getSeen().addAll(FileUtils.readLines(importer.getLogFile()));
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(EXIT_ARGUMENT_ERROR);
                }
            }

            if (line.hasOption(OPTION_CONFIG)) {
                Properties properties = new Properties();
                try {
                    properties.load(new FileInputStream(line.getOptionValue(OPTION_CONFIG)));
                    importer.setHostname(properties.getProperty(OPTION_HOST, importer.getHostname()));
                    importer.setUsername(properties.getProperty(OPTION_USERNAME, importer.getHostname()));
                    importer.setPassword(properties.getProperty(OPTION_PASSWORD, importer.getHostname()));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    System.exit(EXIT_ARGUMENT_ERROR);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(EXIT_ARGUMENT_ERROR);
                }
            }

            if (StringUtils.isEmpty(importer.getHostname())) {
                throw new ParseException("no hostname specified");
            }
            if (StringUtils.isEmpty(importer.getUsername())) {
                throw new ParseException("no username specified");
            }
            if (StringUtils.isEmpty(importer.getPassword())) {
                throw new ParseException("no password specified");
            }

        } catch (ParseException exp) {
            exp.printStackTrace();
            System.err.println("ParseException: " + exp);
            showHelpAndExit(SITE_ACRONYM, options, EXIT_ARGUMENT_ERROR);
        }

        File[] paths = new File[filenames.length];
        for (int i = 0; i < filenames.length; i++) {
            paths[i] = new File(filenames[i]);
            if (!paths[i].exists()) {
                System.err.println("Specified file does not exist: " + paths[i]);
                System.exit(1);
            }
        }

        if (paths.length == 0) {
            System.err.println("Nothing to do, no files or directories specified... Try -help for more options");
            System.exit(1);
        }

        int errorCount = importer.test(paths);
        if (errorCount > 0) {
            System.err.println("Exiting with errors...");
            System.exit(errorCount);
        }

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
            System.err.println("Unrecognized arguments found: " + Arrays.toString(line.getArgs()));
            return true;
        }
        return false;
    }

    private static void showHelpAndExit(String siteAcronym, Options options, int exitCode) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(siteAcronym + " cli api tool", options);
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("Visit " + TOOL_URL + " for documentation on how to use the " + siteAcronym + " commandline API Tool");
        System.out.println("-------------------------------------------------------------------------------");
        System.exit(exitCode);
    }

    /**
     * 
     */
    private int test(File... files) {

        int errorCount = 0;
        try {

            if (getHostname().equalsIgnoreCase(ALPHA_TDAR_ORG)) {
                AuthScope scope = new AuthScope(getHostname(), 80);
                UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials(getUsername(), getPassword());
                httpclient.getCredentialsProvider().setCredentials(scope, usernamePasswordCredentials);
                logger.info("creating challenge/response authentication request for alpha");
                HttpGet tdarIPAuth = new HttpGet(httpProtocol + getHostname() + "/");
                logger.debug(tdarIPAuth.getRequestLine());
                HttpResponse response = httpclient.execute(tdarIPAuth);
                HttpEntity entity = response.getEntity();
                entity.consumeContent();
            } else {
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
                    // System.exit(0);
                }
                logger.trace(EntityUtils.toString(entity));
                entity.consumeContent();
            }

            for (File file : files) {
                errorCount += processDirectory(file);
            }

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

        // if there is more than one record in a directory after scanning of the directory is
        // complete, then ignore all files that are not xml records
        if (records.size() > 1) {
            logger.debug("processing multiple xml files ...  (ignoring attachments) " + records);
            for (File record : records) {
                if (!makeAPICall(record, null)) {
                    errorCount++;
                }
            }
        }
        if (records.size() == 1) {
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
        HttpPost apicall = new HttpPost(httpProtocol + getHostname() + "/api/upload?uploadedItem=" + URLEncoder.encode(path));
        MultipartEntity reqEntity = new MultipartEntity();
        boolean callSuccessful = true;
        if (seen.contains(path)) {
            logger.debug("skipping: " + path);
        }
        reqEntity.addPart("record", new StringBody(FileUtils.readFileToString(record)));

        if (projectId != null) {
            logger.trace("setting projectId:" + projectId);
            reqEntity.addPart("projectId", new StringBody(projectId.toString()));
        }
        if (accountId != null) {
            logger.trace("setting accountId:" + accountId);
            reqEntity.addPart("accountId", new StringBody(accountId.toString()));
        }

        reqEntity.addPart("accessRestriction", new StringBody(getFileAccessRestriction().name()));

        if (!CollectionUtils.isEmpty(attachments)) {
            for (int i = 0; i < attachments.size(); i++) {
                reqEntity.addPart("uploadFile", new FileBody(attachments.get(i)));
                if (getFileAccessRestriction().isRestricted()) {
                    reqEntity.addPart("restrictedFiles", new StringBody(attachments.get(i).getName()));
                }
            }
        }

        apicall.setEntity(reqEntity);
        logger.debug("      files: " + StringUtils.join(attachments, ", "));

        HttpResponse response = httpclient.execute(apicall);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode >= HttpStatus.SC_BAD_REQUEST) {
            System.err.println("Server returned error: [" + record.getAbsolutePath() + "]:" + response.getStatusLine().getReasonPhrase());
            callSuccessful = false;
        }
        logger.info(record.toString() + " - " + response.getStatusLine());
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            String resp = EntityUtils.toString(entity);
            entity.consumeContent();
            if (resp != null && resp != "") {
                logger.debug(resp);
            }
        }

        FileUtils.writeStringToFile(getLogFile(), path + "\r\n", true);
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

    public void setLogFile(String logFile) {
        this.logFile = new File(logFile);
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
     * @param httpProtocol the httpProtocol to set
     */
    private void setHttpProtocol(String httpProtocol) {
        this.httpProtocol = httpProtocol;
    }
}
