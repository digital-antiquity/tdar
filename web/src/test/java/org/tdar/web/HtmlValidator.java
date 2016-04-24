package org.tdar.web;

import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.tidy.Tidy;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.Page;

import nu.validator.messages.GnuMessageEmitter;
import nu.validator.messages.MessageEmitter;
import nu.validator.messages.MessageEmitterAdapter;
import nu.validator.servlet.imagereview.ImageCollector;
import nu.validator.source.SourceCode;
import nu.validator.validation.SimpleDocumentValidator;
import nu.validator.xml.SystemErrErrorHandler;

public class HtmlValidator {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected String[] ignores = { "<header>", "<nav>", "<section>", "<article>", "<aside>", "<footer>", "</header>", "</nav>",
            "</section>", "</article>", "</aside>", "</footer>", "unknown attribute", "trimming empty", "lacks \"type\" attribute",
            "replacing illegal character code", "lacks \"summary\" attribute", "unescaped & which",
            "Warning: '<' + '/' + letter not allowed here", /* javascript */
            "missing </a> before <div>",
            "missing </a> before <h3>",
            "discarding unexpected </div",
            "discarding unexpected </a>",
            "missing </div> before link",
            "discarding unexpected </span>", "missing </span> before ",
            "meta isn't allowed in", "missing </div> before meta", /* meta tags for search info, ok */
            "input repeated attribute" /* radiobutton duplicate css */,
            "inserting implicit <br>",
            "replacing element</p>",
            "discarding unexpected hr"
    };
    
    void validateHtmlViaNuValidator(Page internalPage) {
        SimpleDocumentValidator validator = new SimpleDocumentValidator(false);
        try {
            // System.setProperty("nu.validator.datatype.warn", "false");

            SourceCode sourceCode = validator.getSourceCode();
            ImageCollector imageCollector = new ImageCollector(sourceCode);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MessageEmitter jsonMessageEmitter = new GnuMessageEmitter(out, true);
            MessageEmitterAdapter errorHandler = new MessageEmitterAdapter(sourceCode, false, imageCollector, 0, true, jsonMessageEmitter);
            try {
                String schemaUrl = "http://s.validator.nu/html5-rdfalite.rnc";
                validator.setUpMainSchema(schemaUrl, new SystemErrErrorHandler());
            } catch (Exception e) {
                logger.error("exception in schema parsing", e);
            } catch (StackOverflowError e) {
                logger.error("StackOverflowError"
                        + " while evaluating HTML schema.\nThe checker requires a java thread stack size"
                        + " of at least 512k.\nConsider invoking java with the -Xss"
                        + " option. For example:\n  java -Xss512k ");
            }
            errorHandler.start(null);
            errorHandler.setHtml(true);
            validator.setUpValidatorAndParsers(errorHandler, false, false);
            validator.checkHtmlInputSource(new InputSource(internalPage.getWebResponse().getContentAsStream()));
            errorHandler.end("success", "failed");
            logger.debug("fatal: {}, errors: {}, warnings: {} ", errorHandler.getFatalErrors(), errorHandler.getErrors(), errorHandler.getWarnings());
            List<String> errors = new ArrayList<String>();
            List<String> warnings = new ArrayList<String>();
            if (errorHandler.getFatalErrors() > 0 || errorHandler.getErrors() > 0) {
                for (String line : out.toString().split("\n")) {
                    line = internalPage.getUrl() + " : "+ line;
                    if (line.contains(" error:")) {
                        // unapi standard
                        if (line.contains("The string \"unapi-server\" is not a registered keyword")) {
                            warnings.add(line);
                            continue;
                        }

                        if (line.contains("The string \"noindex\" is not a registered keyword")) {
                            warnings.add(line);
                            continue;
                        }

                        // sitemesh template
                        if (line.contains("error: Attribute \"parse\" not allowed on element")) {
                            warnings.add(line);
                            continue;
                        }
                        // struts form element?
                        if (line.contains("The \"for\" attribute of the \"label\" element must refer to a form control")) {
                            warnings.add(line);
                            continue;
                        }
                        
                        // hr / blockquote / h3
                        if (line.contains("not allowed as child of element \"span\"")) {
                            warnings.add(line);
                            continue;
                        }
                        
                        //https://localhost:8143/search/advanced?siteNameKeywords=disneyland (search results page)
                        if (line.contains("Attribute \"target\" not allowed on element \"input\" at this point")) {
                            warnings.add(line);
                            continue;
                        }

                        // article / section / div
                        // google scholar page
                        if (line.contains("Stray end tag") || line.contains("Unclosed element ")) {
                            warnings.add(line);
                            continue;
                        }

                        if (line.contains("Any \"input\" descendant of a \"label\" element with a \"for\" attribute must have an ID value that matches that \"for\" attribute")) {
                            warnings.add(line);
                            continue;
                        }
                        
                        if (line.contains("The \"nowrap\" attribute on the")) {
                            warnings.add(line);
                            continue;
                        }
                        
                        // google scholar page
                        if (line.contains("End tag \"div\" seen, but there were open elements")) {
                            warnings.add(line);
                            continue;
                        }

                        if (line.contains("No \"p\" element in scope but a \"p\" end tag seen")) {
                            warnings.add(line);
                            continue;
                        }

                        //https://localhost:8143/billing/4 
                        if (line.contains("Element \"thead\" not allowed as child of element ")) {
                            warnings.add(line);
                            continue;
                        }

                        // old table attributes
                        if (line.contains("The \"width\" attribute ") ||
                                line.contains("The \"cellpadding\" attribute") ||
                                line.contains("The \"cellspacing\" attribute")
                                ) {
                            warnings.add(line);
                            continue;
                        }

                        // dataset browse page
                        if (line.contains("End tag \"br\".")) {
                            warnings.add(line);
                            continue;
                        }

                        if (line.contains("Element \"title\" must not be empty")) {
                            warnings.add(line);
                            continue;
                        }
                        
                        if (line.contains("Element \"option\" without attribute \"label\" must not be empty.")) {
                            warnings.add(line);
                            continue;
                        }
                        
                        if (line.contains("\"label\" element may contain at most one \"input\", \"button\", \"select\", \"textarea\", or \"keygen\" descendant.")) {
                            warnings.add(line);
                            continue;
                        }
                        
                        if (line.contains("established by element \"td\" has no cells beginning in it")) {
                            warnings.add(line);
                            continue;
                        }
                        
                        if(line.contains("error: \"&\" did not start a character reference")) {
                            warnings.add(line);
                            continue;
                        }

                        // part of a template
                        if (line.contains("Duplicate ID \"groups_{")) {
                            continue;
                        }
                        // part of a template
                        if (line.contains("Duplicate ID \"groups[{")) {
                            continue;
                        }
                        
                        //:221.9-221.177: error: Bad value "/search/results?groups[0].fieldTypes[0]=KEYWORD_CULTURAL&groups[0].approvedCultureKeywordIdLists[0]=210110&explore=true" for attribute "href" on element "a": Illegal character in query: not a URL code point.
                        if (line.contains("for attribute \"href\" on element \"a\": Illegal character in query: not a URL code point")) {
                            warnings.add(line);
                            continue;
                        }
                        
                        // struts2 template issue?
                        if (line.contains("Bad value \"true\" for attribute \"required\"")) {
                            warnings.add(line);
                            continue;
                        }
                        
                        if (line.contains("Attribute \"value\" not allowed on element \"input\" at this point.")) {
                            warnings.add(line);
                            continue;
                        }
                        
                        //
                        if (line.contains("Bad value  for attribute \"src\" on element \"img\": Illegal character in query")) {
                            warnings.add(line);
                            continue;
                        }
                        
                        ///collection/2821/test-security-collection?null?type=
                        if (line.contains("Attribute \"alt\" not allowed on element \"i\"")) {
                            warnings.add(line);
                            continue;
                        }

                        if (line.contains("Element \"legend\" not allowed as child of element \"fieldset\"")) {
                            warnings.add(line);
                            continue;
                        }
                        
                        if (line.contains("Duplicate ID \"metadataForm_authorshipProxies") ||
                            line.contains("Duplicate ID \"metadataForm_authorizedUsers")) {
                            warnings.add(line);
                            continue;
                        }
                        
                        // tdar custom elements
                        if(line.contains("Attribute \"autocomplete") || 
                                line.contains("Attribute \"book") ||
                                line.contains("Attribute \"callback") ||
                                line.contains("Attribute \"journal") ||
                                line.contains("Attribute \"conference_presentation\"") ||
                                line.contains("Attribute \"other\"") ||
                                line.contains("Attribute \"truncate\"") ||
                                line.contains("Attribute \"thesis\"") ||
                                line.contains("Attribute \"numcolumns\"") ||
                                line.contains("Attribute \"emptyoption\"") ||
                                line.contains("Attribute \"resource-id\"") ||
                                line.contains("Attribute \"placeholder\"") ||
                                line.contains("Attribute \"tooltipfor") ||
                                line.contains("Attribute \"labelposition") ||
                                line.contains("Attribute \"addanother\"") 
                                ) {
                            warnings.add(line);
                            continue;
                        }
                        
                        errors.add(line);
                    } else {
                        warnings.add(line);
                    }
                }
                if (errors.size() > 0) {
                    logger.error("--errors--");
                    for (String err : errors) {
                        logger.error(err);
                    }
                    logger.warn("--warnings--");
                    for (String warn : warnings) {
                        logger.warn(warn);
                    }
                    fail(StringUtils.join(errors,"\n"));
                } else {
                    for (String warn : warnings) {
                        logger.warn(warn);
                    }
                }
            }
        } catch (IOException e1) {
            logger.error("IOException in validation: ", e1);
        } catch (SAXException e1) {
            logger.error("SaxParseException in validation:", e1);
        }
    }

    void validateViaTidy(Page internalPage) {
        Tidy tidy = new Tidy(); // obtain a new Tidy instance
        tidy.setXHTML(true); // set desired config options using tidy setters
        tidy.setQuiet(true);
        // tidy.setOnlyErrors(true);
        // tidy.setShowWarnings(false);
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            StringWriter errs = new StringWriter();
            tidy.setErrout(new PrintWriter(errs, true));
            tidy.parse(internalPage.getWebResponse().getContentAsStream(), baos);
            String[] lines = internalPage.getWebResponse().getContentAsString().split("\n");
            StringBuilder errors = new StringBuilder();
            for (String line : StringUtils.split(errs.toString(), "\n")) {
                boolean skip = false;
                for (String ignore : ignores) {
                    if (StringUtils.containsIgnoreCase(line, ignore)) {
                        skip = true;
                        logger.trace("skipping: {} ", line);
                    }
                }
                if (!skip && line.contains("unknown entity")) {
                    String part = line.substring(5, line.indexOf("column"));
                    int lineNum = Integer.parseInt(part.trim());
                    String lineText = lines[lineNum - 1];
                    logger.debug("{}: {}", lineNum, lineText);
                    if (lineText.toLowerCase().contains("http")) {
                        // NOTE: we may need to make this more strict in the future
                        // String substring = lineText.substring(lineText.toLowerCase().indexOf("http"));
                        skip = true;
                        logger.debug("skipping encoding in URL");
                    }
                }
                // FIXME: add regex to get line number from error: line 291 column 180 - Warning: unescaped & or unknown entity "&amount"
                // then check for URL

                if (skip) {
                    continue;
                }

                if (line.contains("Error:") || line.contains("Warning:")) {
                    errors.append(line).append("\n");
                    logger.error("adding: {}", line);
                }
            }
            String string = errors.toString();
            if (StringUtils.isNotBlank(string.trim())) {
                fail(string + "\r\n\r\n" + internalPage.getWebResponse().getContentAsString());
            }
        } catch (IOException e) {
            logger.error("{}", e);
        } // run tidy, providing an input and output stream
    }

}
