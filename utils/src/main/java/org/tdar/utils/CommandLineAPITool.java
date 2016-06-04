/**
 * $Id$
 *
 * @author $Author$
 * @version $Revision$
 */
package org.tdar.utils;

import static java.lang.System.exit;
import static java.lang.System.lineSeparator;
import static java.lang.System.out;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * http://thegenomefactory.blogspot.com.au/2013/08/minimum-standards-for-bioinformatics.html
 *
 * @author Adam Brin
 */
public class CommandLineAPITool {


    private static final String IMPORT_SEEN_FILE = "files_seen";
    private static final String IMPORT_SEEN_FILE_EXTENSION = "txt";
    private static final String HTTP_PROTOCOL = "http://";
    private static final String HTTPS_PROTOCOL = "https://";
    private static final String ALPHA_PASSWORD = "alpha";
    private static final String ALPHA_USER_NAME = "tdar";
    private static final int EXIT_ARGUMENT_ERROR = -1;
    private static final int EXIT_OK = 0;
    private static final String SITE_ACRONYM = "TDAR/FAIMS";
    private static final String TOOL_URL = "https://docs.tdar.org/display/TDAR/CommandLine+API+Tool";
    private static final String OPTION_HELP = "help";
    private static final String OPTION_FILE = "file";
    private static final String OPTION_CONFIG = "config";
    private static final String OPTION_PASSWORD = "password";
    private static final String OPTION_USERNAME = "username";
    private static final String OPTION_HOST = "host";
    private static final String OPTION_SEEN_FILE = "seenFile";
    private static final String OPTION_ACCOUNTID = "accountid";
    private static final String OPTION_SLEEP = "sleep";
    private static final String OPTION_PROJECT_ID = "projectid";
    private static final String ALPHA_TDAR_ORG = "alpha.tdar.org";
    @SuppressWarnings("unused")
    private static final String CORE_TDAR_ORG = "core.tdar.org";
    private static final String OPTION_HTTP = "http";
//    private static final String OPTION_SHOW_LOG = "log";

    private static final transient Logger logger = LoggerFactory.getLogger(CommandLineAPITool.class);

    private static int errorCount = 0;
    private String hostname = ALPHA_TDAR_ORG; // DEFAULT SHOULD NOT BE CORE
    private String username = ALPHA_USER_NAME;
    private String password = ALPHA_PASSWORD;
    private File seenFile;
    private Long projectId;
    private Long accountId;
    private long msSleepBetween;
    private List<String> seen = new ArrayList<>();
    private String httpProtocol = HTTPS_PROTOCOL;
    private File[] files;
    private APIClient client;

    /**
     * The exit codes have the following meaning:
     * <ul>
     * <li>-1 : there was a problem encountered in the parsing of the arguments
     * <li>0 : no issues were encountered and the run completed successfully
     * <li>any number > 0 : the number of files that the tool was not able to import successfully.
     * </ul>
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        CommandLineAPITool importer = new CommandLineAPITool();
        importer.seenFile = File.createTempFile(IMPORT_SEEN_FILE, IMPORT_SEEN_FILE_EXTENSION);
        logger.warn("Seen file is: " + importer.seenFile.getCanonicalPath());
        Options options = buildCommandLineOptions();
        try {
            parseArguments(args, importer, options);
            errorCount = 0;
            importer.verifyState();
            importer.processFiles();
            if (errorCount > 0) {
                logger.error("Exiting with errors...");
                exit(errorCount);
            }
        } catch (ParseException | IOException exp) {
            logger.error(exp.getMessage(), exp);
            showHelpAndExit(SITE_ACRONYM, options, EXIT_ARGUMENT_ERROR);
        }
    }

    private static void parseArguments(String[] args, CommandLineAPITool importer, Options options) throws ParseException, IOException {
        logger.info("====> Start of run with args: " + Arrays.toString(args));
        CommandLineParser parser = new GnuParser();
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
        if (line.hasOption(OPTION_CONFIG)) {
            // by looking at this option first, we allow the command line to overwrite any of the values in the property file...
            setOptionsFromConfigFile(importer, line);
        }
        if (line.hasOption(OPTION_USERNAME)) {
            importer.setUsername(line.getOptionValue(OPTION_USERNAME));
        }
        if (line.hasOption(OPTION_HOST)) {
            logger.warn("Setting host to " + line.getOptionValue(OPTION_HOST));
            importer.setHostname(line.getOptionValue(OPTION_HOST));
        }
        if (line.hasOption(OPTION_PASSWORD)) {
            importer.setPassword(line.getOptionValue(OPTION_PASSWORD));
        }
        if (line.hasOption(OPTION_FILE)) {
            importer.setFiles(line.getOptionValues(OPTION_FILE));
        }
        if (line.hasOption(OPTION_PROJECT_ID)) {
            importer.setProjectId(getAsLong(line.getOptionValue(OPTION_PROJECT_ID)));
        }
        if (line.hasOption(OPTION_ACCOUNTID)) {
            importer.setAccountId(getAsLong(line.getOptionValue(OPTION_ACCOUNTID)));
        }
        if (line.hasOption(OPTION_SLEEP)) {
            importer.setMsSleepBetween(getAsLong(line.getOptionValue(OPTION_SLEEP)));
        }
        if (line.hasOption(OPTION_SEEN_FILE)) {
            importer.setSeenFile(line.getOptionValue(OPTION_SEEN_FILE));
        }
    }

    @SuppressWarnings("static-access")
    private static Options buildCommandLineOptions() {
        Options options = new Options();
        options.addOption(OptionBuilder.withArgName(OPTION_HELP).withDescription("print this message").create(OPTION_HELP));
        options.addOption(OptionBuilder.withArgName(OPTION_HTTP).withDescription("use the http protocol (default is https)").create(OPTION_HTTP));
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
        options.addOption(OptionBuilder.withArgName(OPTION_SEEN_FILE).hasArg()
                .withDescription("the name of the file to record successful file tranfers to (defaults to " + IMPORT_SEEN_FILE + ")")
                .create(OPTION_SEEN_FILE));
        return options;
    }

    private static void setOptionsFromConfigFile(CommandLineAPITool importer, CommandLine line) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(line.getOptionValue(OPTION_CONFIG)));
        // we only want to set the properties that are actually in the file
        for (Object key : properties.keySet()) {
            final String option = (String) key;
            final String property = properties.getProperty(option);
            switch (option) {
                case OPTION_HOST:
                    importer.setHostname(property);
                    break;
                case OPTION_USERNAME:
                    importer.setUsername(property);
                    break;
                case OPTION_PASSWORD:
                    importer.setPassword(property);
                    break;
                case OPTION_PROJECT_ID:
                    importer.setProjectId(getAsLong(property));
                    break;
                case OPTION_ACCOUNTID:
                    importer.setAccountId(getAsLong(property));
                    break;
                case OPTION_SLEEP:
                    importer.setMsSleepBetween(getAsLong(property));
                    break;
                case OPTION_SEEN_FILE:
                    importer.setSeenFile(property);
                    break;
                case OPTION_FILE:
                    importer.setFiles(property.split(","));
                    break;
                default:
                    throw new IOException("unknown property found in config file: " + option);
            }
        }
    }

    private static Long getAsLong(final String property) {
        return new Long(property.trim());
    }

    private static boolean hasNoOptions(CommandLine line) {
        return line.getOptions().length == 0;
    }

    private static boolean hasUnrecognizedOptions(CommandLine line) {
        if (line.getArgs().length > 0) {
            logger.error("Unrecognized arguments found: " + Arrays.toString(line.getArgs()));
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
        if (ArrayUtils.isEmpty(files)) {
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
    private void processFiles() {
        try {
            client  = new APIClient(httpProtocol + getHostname() + "/api/login");
            ApiClientResponse response = client.apiLogin(getUsername(), getPassword());

            logger.trace("Login form get: " + response.getStatusLine());

            for (File file : files) {
                out.print("*"); // give the user some sort of visual indicator as to progress
                processDirectory(file);
            }
            out.println(); // end of progress indicator

        } catch (Exception e) {
            e.printStackTrace();
            errorCount++;
        }
    }

    /**
     * @param file
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    private void processDirectory(File parentDir) {
        List<File> directories = new ArrayList<>();
        List<File> attachments = new ArrayList<>();
        List<File> records = new ArrayList<>();

        if (parentDir.isDirectory()) {
            for (File file : parentDir.listFiles()) {
                if (file.isHidden()) {
                    continue;
                }
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
    }

    public boolean makeAPICall(File record, List<File> attachments) {
        boolean callSuccessful = true;
        String path = record.getPath();
        try {

            if (seen.contains(path)) {
                logger.warn("skipping: " + path);
            }
            ApiClientResponse response = client.uploadRecord(FileUtils.readFileToString(record), null, accountId, attachments.toArray(new File[0]));
            logger.debug("      files: " + StringUtils.join(attachments, ", "));

            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode >= HttpStatus.SC_BAD_REQUEST) {
                logger.error("Server returned error: [" + record.getAbsolutePath() + "]:"
                        + response.getStatusLine().getReasonPhrase());
                callSuccessful = false;
            } else if (statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
                logger.error("Server returned found: [" + record.getAbsolutePath() + "]:"
                        + response.getStatusLine().getReasonPhrase());
                callSuccessful = false;
            }
            logger.info(record.toString() + " - " + response.getStatusLine());
            Document entity = response.getXmlDocument();
            logger.info("{}",entity);
            if (callSuccessful) {
                FileUtils.writeStringToFile(getSeenFile(), path + " successful: " + callSuccessful + lineSeparator(),
                        true);
                logger.info("successful: " + path);
            } else {
                logger.error("couldn't import: " + path);
            }

            try {
                Thread.sleep(msSleepBetween);
            } catch (Exception e) {
                // we woke up early...
            }
        } catch (Exception e) {
            // we want to suppress all exceptions that might stop the next file from being imported
            logger.error("couldn't import: " + path, e);
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

    public File getSeenFile() {
        return seenFile;
    }

    public void setSeenFile(String logFile) throws IOException {
        this.seenFile = new File(logFile);
        getSeen().addAll(FileUtils.readLines(this.seenFile));
    }

    public List<String> getSeen() {
        return seen;
    }

    public void setSeen(List<String> seen) {
        this.seen = seen;
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
