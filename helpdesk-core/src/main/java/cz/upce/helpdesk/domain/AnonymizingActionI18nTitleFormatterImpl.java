/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.upce.helpdesk.domain;

import java.util.Locale;
import org.esupportail.commons.utils.BeanUtils;
import org.esupportail.helpdesk.domain.ActionI18nTitleFormatterImpl;
import org.esupportail.helpdesk.domain.DomainService;
import org.esupportail.helpdesk.domain.beans.Action;
import org.esupportail.helpdesk.web.controllers.SessionController;

/**
 *
 * @author lusl0338
 */
public class AnonymizingActionI18nTitleFormatterImpl extends ActionI18nTitleFormatterImpl {

    private DomainService domainService;
    private SessionController sessionController;
    /**
     * The i18n prefix.
     */
    private static final String PREFIX = "ACTION_TITLE.";
    private static final String OWNER_SUFFIX = "_OWNER";
    private static final String DOMAIN_SERVICE_BEAN = "domainService";
    private static final String SESSION_CONTROLLER_BEAN = "sessionController";

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
    }

    private DomainService getDomainService() {
        if (domainService == null) {
            domainService = (DomainService) BeanUtils.getBean(DOMAIN_SERVICE_BEAN);
        }
        return domainService;
    }

    private SessionController getSessionController() {
        if (sessionController == null) {
            sessionController = (SessionController) BeanUtils.getBean(SESSION_CONTROLLER_BEAN);
        }
        return sessionController;
    }

    protected String getAnonymizeActionTitleSuffix(final Action action) {
        if (action.getTicket().getOwner().equals(action.getUser())) {
            try {
                if (getDomainService().isDepartmentManager(action.getTicket().getDepartment(), getSessionController().getCurrentUser())) {
                    return "";
                }
            } catch (IllegalStateException ex) {
                // Pravděpodobně No Scope registered for scope 'session'
                return OWNER_SUFFIX;
            }
            return OWNER_SUFFIX;
        }
        return "";
    }

    /**
     * @param action
     * @param locale
     * @return the i18n title of an action.
     */
    @Override
    protected String getActionTitleApproveClosure(final Action action, final Locale locale) {
        return getI18nService().getString(PREFIX + "APPROVE" + getAnonymizeActionTitleSuffix(action), locale, new Object[]{
                    action.getDate(),
                    getUserFormattingService().format(action.getUser(), locale),});
    }

    /**
     * @param action
     * @param locale
     * @return the i18n title of an action.
     */
    @Override
    protected String getActionTitleCancel(final Action action, final Locale locale) {
        return getI18nService().getString(PREFIX + "CANCEL" + getAnonymizeActionTitleSuffix(action), locale, new Object[]{
                    action.getDate(),
                    getUserFormattingService().format(action.getUser(), locale),});
    }

    /**
     * @param action
     * @param locale
     * @return the i18n title of an action.
     */
    @Override
    protected String getActionTitleClose(final Action action, final Locale locale) {
        return getI18nService().getString(PREFIX + "CLOSE" + getAnonymizeActionTitleSuffix(action), locale, new Object[]{
                    action.getDate(),
                    getUserFormattingService().format(action.getUser(), locale),});
    }

    /**
     * @param action
     * @param locale
     * @return the i18n title of an action.
     */
    @Override
    protected String getActionTitleCreate(final Action action, final Locale locale) {
        return getI18nService().getString(PREFIX + "CREATE" + getAnonymizeActionTitleSuffix(action), locale, new Object[]{
                    action.getDate(),
                    getUserFormattingService().format(action.getUser(), locale),});
    }

    /**
     * @param action
     * @param locale
     * @return the i18n title of an action.
     */
    @Override
    protected String getActionTitleGiveInformation(final Action action, final Locale locale) {
        if (action.getUser() != null) {
            return getI18nService().getString(PREFIX + "GIVE_INFORMATION" + getAnonymizeActionTitleSuffix(action), locale, new Object[]{
                        action.getDate(),
                        getUserFormattingService().format(action.getUser(), locale),});
        }
        return getI18nService().getString(PREFIX + "GIVE_INFORMATION_APPLICATION", locale, new Object[]{
                    action.getDate(),});
    }

    /**
     * @param action
     * @param locale
     * @return the i18n title of an action.
     */
    @Override
    protected String getActionTitleRefuseClosure(final Action action, final Locale locale) {
        return getI18nService().getString(PREFIX + "REFUSE_CLOSURE" + getAnonymizeActionTitleSuffix(action), locale, new Object[]{
                    action.getDate(),
                    getUserFormattingService().format(action.getUser(), locale),});
    }

    /**
     * @param action
     * @param locale
     * @return the i18n title of an action.
     */
    @Override
    protected String getActionTitleReopen(final Action action, final Locale locale) {
        return getI18nService().getString(PREFIX + "REOPEN" + getAnonymizeActionTitleSuffix(action), locale, new Object[]{
                    action.getDate(),
                    getUserFormattingService().format(action.getUser(), locale),});
    }

    /**
     * @param action
     * @param locale
     * @return the i18n title of an action.
     */
    @Override
    protected String getActionTitleUpload(final Action action, final Locale locale) {
        if (action.getUser() != null) {
            return getI18nService().getString(PREFIX + "UPLOAD" + getAnonymizeActionTitleSuffix(action), locale, new Object[]{
                        action.getDate(),
                        getUserFormattingService().format(action.getUser(), locale),
                        action.getFilename(),});
        }
        return getI18nService().getString(PREFIX + "UPLOAD_APPLICATION", locale, new Object[]{
                    action.getDate(),
                    action.getFilename(),});
    }
}
