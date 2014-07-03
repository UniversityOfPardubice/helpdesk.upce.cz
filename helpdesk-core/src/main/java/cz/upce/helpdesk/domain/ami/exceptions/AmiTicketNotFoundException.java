/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.upce.helpdesk.domain.ami.exceptions;

import org.esupportail.helpdesk.domain.beans.AbstractTicket;

/**
 *
 * @author lusl0338
 */
public class AmiTicketNotFoundException extends Exception {
    private static final long serialVersionUID = 4896023406899451245L;

    public static AmiTicketNotFoundException getHelpdeskIdNotFound(final AbstractTicket ticket) {
        return new AmiTicketNotFoundException("AMI ticket pro helpdesk id=" + ticket.getId() + " nebyl nalezen");
    }

    public static AmiTicketNotFoundException getAmiIdNotFound(final Long id) {
        return new AmiTicketNotFoundException("AMI ticket pro id=" + id + " nebyl nalezen");
    }

    public static AmiTicketNotFoundException getAmiIdNotFound(final Long id, final Long rok) {
        return new AmiTicketNotFoundException("AMI ticket pro cislo=" + id + ", rok=" + rok + " nebyl nalezen");
    }

    private AmiTicketNotFoundException(String message) {
        super(message);
    }
}
