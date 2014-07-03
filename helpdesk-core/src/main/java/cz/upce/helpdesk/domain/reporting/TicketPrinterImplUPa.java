/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.upce.helpdesk.domain.reporting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import org.esupportail.commons.aop.cache.RequestCache;
import org.esupportail.commons.exceptions.ConfigException;
import org.esupportail.helpdesk.domain.beans.Ticket;
import org.esupportail.helpdesk.domain.beans.User;
import org.esupportail.helpdesk.domain.reporting.TicketPrinterImpl;

/**
 *
 * @author lusl0338
 */
public class TicketPrinterImplUPa extends TicketPrinterImpl {

    private AlertSenderUtils alertSenderUtils;

    private AlertSenderUtils getAlertSenderUtils() {
        if (alertSenderUtils == null) {
            alertSenderUtils = new AlertSenderUtils(getI18nService(), getDomainService(), getActionI18nTitleFormatter(), getUserFormattingService());
        }
        return alertSenderUtils;
    }

    @Override
    @RequestCache
    public String getTicketPrintContent(
            final User user,
            final Ticket ticket) {
        Locale locale = getDomainService().getUserStore().getUserLocale(user);
        String htmlContent = "";
        String subject = getI18nService().getString(
                "PRINT.TICKET.SUBJECT", locale,
                String.valueOf(ticket.getId()), ticket.getLabel());
        htmlContent += getEmailOrPrintHeader(locale, subject);
        htmlContent += getAlertSenderUtils().getEmailOrPrintHistory(user, ticket, false, false);
        htmlContent += getEmailOrPrintAttributes(user, ticket);
        htmlContent += getAlertSenderUtils().getEmailOrPrintProperties(user, ticket);
        htmlContent += getEmailOrPrintFiles(user, ticket);
        if (getDomainService().userCanChangeLabel(user, ticket)) {
            htmlContent += getEmailOrPrintInvitations(user, ticket);
            htmlContent += getEmailOrPrintMonitoring(user, ticket);
        }
        htmlContent += getEmailOrPrintOwnerInfo(user, ticket);
        htmlContent += getEmailOrPrintFooter(locale);
        htmlContent += "<script lang=\"javascript\">window.print();window.close();</script>\n";
        return htmlContent;
    }

    @RequestCache
    public String getTicketShortPrintContent(
            final User user,
            final Ticket ticket) {
        Locale locale = getDomainService().getUserStore().getUserLocale(user);
        String htmlContent = "";
        String subject = getI18nService().getString(
                "PRINT.TICKET.SUBJECT", locale,
                String.valueOf(ticket.getId()), ticket.getLabel());
        htmlContent += getEmailOrPrintHeaderShort(locale, subject);
        htmlContent += getAlertSenderUtils().getEmailTicketLabel(user, ticket);
        htmlContent += getEmailOrPrintAttributes(user, ticket);
        htmlContent += getAlertSenderUtils().getEmailOrPrintHistory(user, ticket, false, false);
        htmlContent += getEmailOrPrintFooter(locale);
        htmlContent += "<script lang=\"javascript\">window.print();window.close();</script>\n";
        return htmlContent;
    }

    protected String getEmailOrPrintHeaderShort(
            @SuppressWarnings("unused")
            final Locale locale,
            final String subject) {
        String htmlHeader = "<html><head><title>" + subject + "</title><style type=\"text/css\">\n<!--\n";
        try {
            InputStream is = getClass().getResourceAsStream("/properties/domain/email.css");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = reader.readLine();
            while (line != null) {
                htmlHeader += line + "\n";
                line = reader.readLine();
            }
            is.close();
        } catch (IOException e) {
            throw new ConfigException(e);
        }
        htmlHeader += "\n-->\n</style></head><body class=\"short-print\">";
        return htmlHeader;
    }
}
