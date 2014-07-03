/* $Id: MonitoringSenderImplUPa.java 216 2012-07-18 14:45:54Z lusl0338 $ */
package cz.upce.helpdesk.domain.reporting;

import org.esupportail.commons.services.authentication.AuthUtils;
import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.esupportail.helpdesk.domain.ActionType;
import org.esupportail.helpdesk.domain.beans.Action;
import org.esupportail.helpdesk.domain.beans.Ticket;
import org.esupportail.helpdesk.domain.beans.User;

/**
 *
 * @author $LastChangedBy: lusl0338 $
 * $Id: MonitoringSenderImplUPa.java 216 2012-07-18 14:45:54Z lusl0338 $
 */
public class MonitoringSenderImplUPa extends org.esupportail.helpdesk.domain.reporting.MonitoringSenderImpl {

    /**
     * A logger.
     */
    private final Logger logger = new LoggerImpl(getClass());
    private AlertSenderUtils alertSenderUtils;

    private AlertSenderUtils getAlertSenderUtils() {
        if (alertSenderUtils == null) {
            alertSenderUtils = new AlertSenderUtils(getI18nService(), getDomainService(), getActionI18nTitleFormatter(), getUserFormattingService());
        }
        return alertSenderUtils;
    }

    private String getExternMonitoringAlertMessage(
            final User user,
            final Ticket ticket) {
        String htmlContent = "";

        htmlContent += getAlertSenderUtils().getEmailTicketLabel(user, ticket);
        htmlContent += getAlertSenderUtils().getEmailLastAction(user, ticket, true, false);
        if (isUseReplyTo()) {
            htmlContent += getReplyToContent(user, ticket);
        }
        htmlContent += getEmailOrPrintAttributes(user, ticket);
        htmlContent += getEmailOrPrintHistory(user, ticket);

        return htmlContent;
    }

    private String getCasMonitoringAlertMessage(
            final String authTypeIfNullUser,
            final User user,
            final Ticket ticket,
            final String contentHeader,
            final String contentFooter) {
        String htmlContent = "";

        htmlContent += getAlertSenderUtils().getEmailTicketLabel(user, ticket);
        htmlContent += contentHeader;
        htmlContent += getAlertSenderUtils().getEmailLastAction(user, ticket, true, false);
        if (isUseReplyTo()) {
            htmlContent += getReplyToContent(user, ticket);
        }
        htmlContent += getEmailOrPrintHistory(user, ticket);
        htmlContent += getEmailOrPrintAttributes(user, ticket);
        htmlContent += getEmailQuickLinks(authTypeIfNullUser, user, ticket);
        htmlContent += getAlertSenderUtils().getEmailOrPrintProperties(user, ticket);
        htmlContent += getEmailOrPrintFiles(user, ticket);
        if (getDomainService().userCanChangeLabel(user, ticket)) {
            htmlContent += getEmailOrPrintInvitations(user, ticket);
            htmlContent += getEmailOrPrintMonitoring(user, ticket);
        }
        htmlContent += getEmailOrPrintOwnerInfo(user, ticket);
        htmlContent += contentFooter;

        return htmlContent;
    }

    /**
     * @see org.esupportail.helpdesk.domain.DomainServiceImplUPa#ticketMonitoringSendAlert(java.lang.String, boolean, org.esupportail.helpdesk.domain.beans.User, org.esupportail.helpdesk
    .domain.beans.Ticket, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    protected boolean ticketMonitoringSendAlert(
            final String email,
            final String authTypeIfNullUser,
            final User user,
            final Ticket ticket,
            final String subject,
            final String contentHeader,
            final String contentFooter) {
        Action lastAction = getAlertSenderUtils().getLastAction(user, ticket, true, true);
        if (lastAction == null) {
            return false;
        }
        if (ActionType.EXPIRE.equals(lastAction.getActionType())) {
            return false;
        }

        String htmlContent = "";

        if (AuthUtils.APPLICATION.equals(user.getAuthType())) {
            htmlContent = getExternMonitoringAlertMessage(user, ticket);
        } else {
            htmlContent = getCasMonitoringAlertMessage(authTypeIfNullUser, user, ticket, contentHeader, contentFooter);
        }

        String messageId = genMessageId(ticket);
        if (user == null) {
            return send(email, null, messageId, subject, htmlContent);
        }
        return send(user, messageId, subject, htmlContent);
    }

    /**
     * @param user
     * @param ticket
     * @return the history to include in monitoring emails or prints.
     */
    @Override
    protected String getEmailOrPrintHistory(
            final User user,
            final Ticket ticket) {
        return alertSenderUtils.getEmailOrPrintHistory(user, ticket, false, true);
    }
}

