/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.upce.helpdesk.services.feed.imap.ami;

import cz.upce.helpdesk.domain.ami.AmiConnector;
import java.io.Serializable;
import java.net.URLDecoder;
import javax.mail.Message;
import javax.mail.MessagingException;
import org.esupportail.commons.utils.Assert;
import org.esupportail.helpdesk.domain.DomainService;
import org.esupportail.helpdesk.domain.beans.Ticket;
import org.esupportail.helpdesk.services.feed.ErrorHolder;
import org.springframework.beans.factory.InitializingBean;

/**
 *
 * @author lusl0338
 */
public class TicketMessageReaderImplAmi implements Serializable, InitializingBean {

    private static final long serialVersionUID = -8160795551990900049L;
    private AmiConnector amiConnector;
    private DomainService domainService;

    public Boolean read(Message message,
            ErrorHolder errorHolder) {
        Boolean ret = Boolean.FALSE;
        try {
            String subject = message.getSubject();
            String decodedSubject = URLDecoder.decode(subject, "UTF-8");
            errorHolder.addInfo("Email subject: " + decodedSubject);
            ret = amiConnector.processMessageSubject(decodedSubject, domainService, errorHolder);
        } catch (MessagingException e) {
            errorHolder.addError("Error getting header: " + e.getMessage());
        } catch (Exception e) {
            errorHolder.addError("Error processing email: " + e.getMessage());
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                errorHolder.addError("                           " + stackTraceElement.toString());
            }
        }
        return ret;
    }

    public boolean readAll(ErrorHolder errorHolder) {
        Boolean ret = Boolean.FALSE;
        try {
            ret = amiConnector.processAllMessages(domainService, errorHolder);
        } catch (Exception e) {
            errorHolder.addError("Error synchronizing AMI: " + e.getMessage());
        }
        return ret;
    }

    public boolean syncMissingToAmi(Long ticketId, ErrorHolder errorHolder) {
        Boolean ret = Boolean.FALSE;
        try {
            Ticket ticket = domainService.getTicket(ticketId);
            amiConnector.syncMissingTicket(domainService, ticket, true);
            ret = Boolean.TRUE;
        } catch (Exception e) {
            errorHolder.addError("Error synchronizing TO to AMI: " + e.getMessage());
        }
        return ret;
    }

    public boolean syncMissingToAmi(ErrorHolder errorHolder, boolean includeArchived) {
        Boolean ret = Boolean.FALSE;
        try {
            ret = amiConnector.syncMissingToAmi(domainService, errorHolder, includeArchived);
        } catch (Exception e) {
            errorHolder.addError("Error synchronizing TO to AMI: " + e.getMessage());
        }
        return ret;
    }

    public boolean syncMissingCommunication(ErrorHolder errorHolder) {
        Boolean ret = Boolean.FALSE;
        try {
            ret = amiConnector.syncMissingCommunication(domainService, errorHolder);
        } catch (Exception e) {
            errorHolder.addError("Error synchronizing lost communication from AMI: " + e.getMessage());
        }
        return ret;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(amiConnector,
                "property amiConnector of class " + this.getClass().getName() + " can not be null");
        Assert.notNull(domainService,
                "property domainService of class " + this.getClass().getName() + " can not be null");
    }

    public AmiConnector getAmiConnector() {
        return amiConnector;
    }

    public void setAmiConnector(AmiConnector amiConnector) {
        this.amiConnector = amiConnector;
    }

    public DomainService getDomainService() {
        return domainService;
    }

    public void setDomainService(DomainService domainService) {
        this.domainService = domainService;
    }
}
