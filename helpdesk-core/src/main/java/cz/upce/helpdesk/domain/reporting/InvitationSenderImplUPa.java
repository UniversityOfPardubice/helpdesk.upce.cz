/* $Id: InvitationSenderImplUPa.java 216 2012-07-18 14:45:54Z lusl0338 $ */
package cz.upce.helpdesk.domain.reporting;

import org.esupportail.helpdesk.domain.beans.Ticket;
import org.esupportail.helpdesk.domain.beans.User;
import org.esupportail.helpdesk.domain.reporting.InvitationSenderImpl;

/**
 *
 * @author $LastChangedBy: lusl0338 $
 * $Id: InvitationSenderImplUPa.java 216 2012-07-18 14:45:54Z lusl0338 $
 */
public class InvitationSenderImplUPa extends InvitationSenderImpl {

    private AlertSenderUtils alertSenderUtils;

    private AlertSenderUtils getAlertSenderUtils() {
        if (alertSenderUtils == null) {
            alertSenderUtils = new AlertSenderUtils(getI18nService(), getDomainService(), getActionI18nTitleFormatter(), getUserFormattingService());
        }
        return alertSenderUtils;
    }

    @Override
    protected boolean ticketMonitoringSendAlert(
            final String email,
            final String authTypeIfNullUser,
            final User user,
            final Ticket ticket,
            final String subject,
            final String contentHeader,
            final String contentFooter) {
        String htmlContent = "";
        htmlContent += getAlertSenderUtils().getEmailTicketLabel(user, ticket);
        htmlContent += contentHeader;
        htmlContent += getAlertSenderUtils().getEmailLastAction(user, ticket, true, true);
        if (isUseReplyTo()) {
            htmlContent += getReplyToContent(user, ticket);
        }
        htmlContent += getEmailOrPrintHistory(user, ticket);
        htmlContent += getEmailQuickLinks(authTypeIfNullUser, user, ticket);
        htmlContent += getAlertSenderUtils().getEmailOrPrintProperties(user, ticket);
        htmlContent += getEmailOrPrintFiles(user, ticket);
        if (getDomainService().userCanChangeLabel(user, ticket)) {
            htmlContent += getEmailOrPrintInvitations(user, ticket);
            htmlContent += getEmailOrPrintMonitoring(user, ticket);
        }
        htmlContent += getEmailOrPrintOwnerInfo(user, ticket);
        htmlContent += contentFooter;

        String messageId = genMessageId(ticket);
        if (user == null) {
            return send(email, null, messageId, subject, htmlContent);
        }
        return send(user, messageId, subject, htmlContent);
    }

    @Override
    protected String getEmailOrPrintHistory(
            final User user,
            final Ticket ticket) {
        return alertSenderUtils.getEmailOrPrintHistory(user, ticket, true, true);
    }
}

