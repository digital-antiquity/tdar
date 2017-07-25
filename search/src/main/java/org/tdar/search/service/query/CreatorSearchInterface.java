package org.tdar.search.service.query;

import java.io.IOException;

import org.tdar.core.bean.entity.Creator;
import org.tdar.core.bean.entity.Person;
import org.tdar.search.bean.PersonSearchOption;
import org.tdar.search.exception.SearchException;
import org.tdar.search.query.LuceneSearchResultHandler;
import org.tdar.search.query.SearchResult;
import org.tdar.utils.MessageHelper;

import com.opensymphony.xwork2.TextProvider;

public interface CreatorSearchInterface<I extends Creator<?>> {

	LuceneSearchResultHandler<I> searchInstitution(String name, LuceneSearchResultHandler<I> result,
			TextProvider provider) throws SearchException, IOException;

	LuceneSearchResultHandler<I> findPerson(String name, PersonSearchOption searchOption, LuceneSearchResultHandler<I> result, TextProvider provider)
			throws SearchException, IOException;

	LuceneSearchResultHandler<I> findInstitution(String institution, LuceneSearchResultHandler<I> result,
			TextProvider provider, int min) throws SearchException, IOException;

	LuceneSearchResultHandler<I> findPerson(Person person_, String term, Boolean registered,
			LuceneSearchResultHandler<I> result, TextProvider provider, int min) throws SearchException, IOException;

   LuceneSearchResultHandler<I> findPerson(String query, LuceneSearchResultHandler<I> result, TextProvider provider) throws SearchException, IOException;

}