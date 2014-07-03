/**
 * ESUP-Portail Helpdesk - Copyright (c) 2004-2009 ESUP-Portail consortium.
 */
package org.esupportail.helpdesk.web.beans; 

import java.util.HashMap;

/** 
 * A provider for category attribute type i18n keys.
 */ 
public class CategoryAttributeTypeI18nKeyProvider extends HashMap<String, String> {
	
	/**
	 * The serialization id.
	 */
    private static final long serialVersionUID = 2421483293922537393L;
	
	/**
	 * The i18n prefix.
	 */
	private static final String PREFIX = "DOMAIN.CATEGORY_ATTRIBUTE.";
	
	/**
	 * Bean constructor.
	 */
	public CategoryAttributeTypeI18nKeyProvider() {
		super();
	}

	/**
	 * @see java.util.HashMap#get(java.lang.Object)
	 */
	@Override
	public String get(final Object categoryAttributeType) {
		return PREFIX + categoryAttributeType;
	}
	
	/**
	 * @param key the key 
	 * @return the i18n key that corresponds to a ticket scope. 
	 */
	public static String getI18nKey(final String key) {
		return new CategoryAttributeTypeI18nKeyProvider().get(key);
	}

}

