package org.tdar.search.query;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.lucene.analysis.Token;
import org.apache.solr.spelling.QueryConverter; 

/**                                                                                                                                                                                                                                         
 * Converts the query string to a Collection of Lucene tokens.                                                                                                                                                                             
 **/ 
public class SpellingQueryConverter extends QueryConverter  { 

  /**                                                                                                                                                                                                                                       
   * Converts the original query string to a collection of Lucene Tokens.                                                                                                                                                                 
   * @param original the original query string                                                                                                                                                                                             
   * @return a Collection of Lucene Tokens                                                                                                                                                                                                 
   */ 
  @Override 
  public Collection<Token> convert(String original) { 
    if (original == null) {                                                                                                                                                             
      return Collections.emptyList(); 
    } 
    Collection<Token> result = new ArrayList<Token>(); 
    @SuppressWarnings("deprecation")
    Token token = new Token(original, 0, original.length()); 
    result.add(token); 
    return result; 
  } 

} 
