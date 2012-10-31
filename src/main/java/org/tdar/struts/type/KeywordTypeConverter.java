package org.tdar.struts.type;

import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.struts2.util.StrutsTypeConverter;
import org.tdar.core.bean.keyword.Keyword;

import com.opensymphony.xwork2.conversion.TypeConversionException;

@SuppressWarnings("rawtypes")
public class KeywordTypeConverter extends StrutsTypeConverter {

	public static final Logger logger = Logger.getLogger(KeywordTypeConverter.class);
	
	@Override
	public Object convertFromString(Map context, String[] values, Class toClass) {
		if (values.length < 1 || !Keyword.class.isAssignableFrom(toClass)) return null;
		logger.debug("Attempting to convert something.");
		try {
			Keyword keyword = (Keyword) toClass.newInstance();
			keyword.setLabel(values[0]);
			return keyword;
		} catch (InstantiationException e) {
			throw new TypeConversionException("", e);
		} catch (IllegalAccessException e) {
			throw new TypeConversionException("", e);
		}
	}

	@Override
	public String convertToString(Map context, Object o) {
		logger.debug("Attempting to convert something.");
		Keyword k = (Keyword)o;
		return k.getLabel();
	}

}
