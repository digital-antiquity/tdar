package org.tdar.filestore.personal;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.tdar.core.bean.PersonalFilestoreTicket;
import org.tdar.core.bean.entity.Person;

/**
 * A personal filestore retains files that are pertinent only for a particular user. One example is an ajax upload scenario where a user uploads attachments
 * to a forthcoming resource. Another scenario is an integration file.
 * 
 * @author jimdevos
 * 
 */
public interface PersonalFilestore {

    String getStoreLocation(Person person);

    PersonalFilestoreFile store(PersonalFilestoreTicket ticket, File originalFile, String newFileName) throws IOException;

    void store(PersonalFilestoreTicket ticket, List<File> files, List<String> newfileNames) throws IOException;

    List<PersonalFilestoreFile> retrieveAll(PersonalFilestoreTicket ticket);

    void purge(PersonalFilestoreTicket ticket);

    void purge(Person person, PersonalFileType personalFileType);

    void purgeQuietly(PersonalFilestoreTicket ticket);

    PersonalFilestoreFile retrieve(PersonalFilestoreTicket ticket, String filename);
}
