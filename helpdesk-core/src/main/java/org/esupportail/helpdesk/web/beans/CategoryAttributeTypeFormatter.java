/**
 * ESUP-Portail Helpdesk - Copyright (c) 2004-2009 ESUP-Portail consortium.
 */
package org.esupportail.helpdesk.web.beans; 

import java.util.HashMap;
import java.util.Locale;

import org.esupportail.commons.aop.cache.RequestCache;
import org.esupportail.commons.services.i18n.I18nService;
import org.esupportail.commons.utils.Assert;
import org.esupportail.helpdesk.domain.beans.User;
import org.esupportail.helpdesk.web.controllers.SessionController;
import org.springframework.beans.factory.InitializingBean;


/** 
 * A session-scoped bean used to format users.
 */ 
public class CategoryAttributeTypeFormatter 
extends HashMap<String, String> implements InitializingBean {
	
	/**
	 * The serialization id.
	 */
    private static final long serialVersionUID = 371872670707984069L;

	/**
	 * The session controller.
	 */
	private SessionController sessionController;

	/**
	 * The i18n service.
	 */
    private I18nService i18nService;
	
	/**
	 * Bean constructor.
	 */
	public CategoryAttributeTypeFormatter() {
		super();
	}

	/**
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() {
		Assert.notNull(i18nService,
				"property i18nService of class " + this.getClass().getName() 
				+ " can not be null");
		Assert.notNull(sessionController,
				"property sessionController of class " + this.getClass().getName() 
				+ " can not be null");
	}

	/**
	 * Formats category attribute type.
	 * @param type the type
	 * @param locale the locale
	 * @return formated category attribute type
	 */
	@RequestCache
	protected String format(
			final String type,
			final Locale locale) {
		return i18nService.getString(
				"CATEGORY_ATTRIBUTE_TYPE." + type, locale);
	}

    /**
	 * @see java.util.HashMap#get(java.lang.Object)
	 */
	@Override
	public String get(final Object o) {
		if (o == null) {
			return null;
		}
		if (!(o instanceof String)) {
			return null;
		}
		return format((String) o, sessionController.getLocale());
	}

	/**
	 * @return the sessionController
	 */
	protected SessionController getSessionController() {
		return sessionController;
	}

	/**
	 * @param sessionController the sessionController to set
	 */
	public void setSessionController(final SessionController sessionController) {
		this.sessionController = sessionController;
	}

	/**
	 * @return the i18nService
	 */
    public I18nService getI18nService() {
        return i18nService;
    }

	/**
	 * @param i18nService the i18nService to set
	 */
    public void setI18nService(I18nService i18nService) {
        this.i18nService = i18nService;
    }

}

