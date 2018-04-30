package org.tdar.core.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.billing.BillingAccount;
import org.tdar.core.bean.entity.TdarUser;
import org.tdar.core.bean.file.AbstractFile;
import org.tdar.core.bean.file.TdarDir;
import org.tdar.core.bean.file.TdarFile;
import org.tdar.core.exception.FileUploadException;
import org.tdar.filestore.personal.PersonalFileType;
import org.tdar.filestore.personal.PersonalFilestore;
import org.tdar.filestore.personal.PersonalFilestoreFile;

public interface PersonalFilestoreService {

    /**
     * Creates a new PersonalFilestoreTicket that the personal filestore can use to create a filestore in a unique location.
     * 
     * @return a new PersonalFilestoreTicket used to keep track of files uploaded by the given Person
     */
    PersonalFilestoreTicket createPersonalFilestoreTicket(TdarUser person);

    /**
     * Creates a new PersonalFilestoreTicket for the given Person with the given PersonalFileType (either UPLOAD or INTEGRATION)
     * 
     * @param person
     * @param fileType
     *            whether or not the ticket is being created for an upload or data integration process.
     * @return a new PersonalFilestoreTicket used to keep track of files generated by the given Person (either uploaded or via data integration)
     */
    PersonalFilestoreTicket createPersonalFilestoreTicket(TdarUser person, PersonalFileType fileType);

    /**
     * Returns a personal filestore for the given user.
     * FIXME: should this be based on the PersonalFilestoreTicket instead?
     * 
     * @param submitter
     * @return a properly synchronized filestore for the given user.
     */
    PersonalFilestore getPersonalFilestore(TdarUser submitter);

    /**
     * Find a @link PersonalFilestoreTicket based on the ID
     * 
     * @param ticketId
     * @return
     */
    PersonalFilestoreTicket findPersonalFilestoreTicket(Long ticketId);

    /**
     * Return all files in the @link PersonalFilestore based on the ticketId
     * 
     * @param ticketId
     * @return
     */
    List<PersonalFilestoreFile> retrieveAllPersonalFilestoreFiles(Long ticketId);

    /**
     * Get the @link PersonalFilestore
     * 
     * @param ticket
     * @return
     */
    PersonalFilestore getPersonalFilestore(PersonalFilestoreTicket ticket);

    /**
     * Get a filestore given a ticket
     * 
     * @param ticketId
     * @return
     */
    PersonalFilestore getPersonalFilestore(Long ticketId);

    /**
     * Store a file in the Personal Filestore
     * 
     * @param ticket
     * @param file
     * @param filename
     * @return
     * @throws FileUploadException
     * @throws IOException
     */
    void store(PersonalFilestoreTicket ticket, File file, String fileName, BillingAccount account, TdarUser user, TdarDir dir) throws FileUploadException;

    TdarDir createDirectory(TdarDir parent, String name, BillingAccount account, TdarUser authenticatedUser);

    List<AbstractFile> listFiles(TdarDir parent, BillingAccount account, String term, TdarUser authenticatedUser);

    void deleteFile(AbstractFile file, TdarUser authenticatedUser);

    void moveFiles(List<AbstractFile> files, TdarDir dir, TdarUser authenticatedUser);

    TdarDir findUnfileDir(TdarUser authenticatedUser);

    void editMetadata(TdarFile file, String note, boolean needsOcr, boolean curate, TdarUser authenticatedUser);

    void markCurated(List<TdarFile> files, TdarUser authenticatedUser);

    void markReviewed(List<TdarFile> files, TdarUser user);

    void addComment(AbstractFile file, String comment, TdarUser authenticatedUser);

}