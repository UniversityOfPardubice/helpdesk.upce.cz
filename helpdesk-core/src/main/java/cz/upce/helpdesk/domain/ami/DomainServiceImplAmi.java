/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.upce.helpdesk.domain.ami;

import cz.upce.helpdesk.domain.DomainServiceImplUPa;
import cz.upce.helpdesk.domain.ami.exceptions.AmiTicketNotFoundException;
import java.util.List;
import org.esupportail.commons.aop.cache.RequestCache;
import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.esupportail.commons.utils.Assert;
import org.esupportail.helpdesk.domain.beans.Action;
import org.esupportail.helpdesk.domain.beans.Category;
import org.esupportail.helpdesk.domain.beans.Department;
import org.esupportail.helpdesk.domain.beans.Ticket;
import org.esupportail.helpdesk.domain.beans.TicketAttributeData;
import org.esupportail.helpdesk.domain.beans.User;

/**
 *
 * @author lusl0338
 */
public class DomainServiceImplAmi extends DomainServiceImplUPa {

    private static final long serialVersionUID = -8546058990158746720L;
    private AmiConnector amiConnector;
    private final Logger logger = new LoggerImpl(getClass());
    private boolean shouldSendAmiMessage = true;

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        Assert.notNull(this.amiConnector,
                "property amiConnector of class " + this.getClass().getName() + " can not be null");
    }

    public void setAmiConnector(AmiConnector amiConnector) {
        this.amiConnector = amiConnector;
    }

    public AmiConnector getAmiConnector() {
        return this.amiConnector;
    }

    @Override
    public void moveTicket(
            final User author,
            final Ticket ticket,
            final Category targetCategory,
            final String message,
            final String actionScope,
            final boolean alerts,
            final boolean free,
            final boolean monitor,
            final boolean invite) {
        super.moveTicket(author, ticket, targetCategory, message, actionScope, alerts, free, monitor, invite);
        try {
            amiConnector.moveTicketAction(this, ticket, message);
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    @Override
    public Ticket addWebTicket(
            final User author,
            final User owner,
            final Department creationDepartment,
            final Category category,
            final String label,
            final String computer,
            final int priorityLevel,
            final String message,
            final String ticketScope,
            final String ticketOrigin,
            final List<TicketAttributeData> ticketAttributesData) {
        Ticket ticket = null;
        try {
            if (amiConnector.isCategoryMaintained(category)) {
                AmiConnector.setAllManagersMinimalAttributes(this, creationDepartment);
            }
            ticket = super.addWebTicket(author, owner, creationDepartment, category, label, computer, priorityLevel, message, ticketScope, ticketOrigin, ticketAttributesData);
            amiConnector.createTicketAction(ticket, message);
        } catch (Exception ex) {
            logger.error(ex);
        }
        return ticket;
    }

    @Override
    protected void addActionToTicket(
            final Ticket ticket,
            final Action action) {
        super.addActionToTicket(ticket, action);
        try {
            if (shouldSendAmiMessage) {
                amiConnector.addAction(ticket, action);
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public void suspendAmiMessages() {
        shouldSendAmiMessage = false;
    }

    public void resumeAmiMessages() {
        shouldSendAmiMessage = true;
    }

    @Override
    @RequestCache
    public boolean userCanApproveClosure(User user, Ticket ticket) {
        if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
            return super.userCanApproveClosure(user, ticket);
        }
        return false;
    }

    @Override
    @RequestCache
    public boolean userCanRefuseClosure(User user, Ticket ticket) {
        if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
            return super.userCanRefuseClosure(user, ticket);
        }
        return false;
    }

    /*
    @Override
    @RequestCache
    public boolean userCanGiveInformation(User user, Ticket ticket) {
    if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
    return super.userCanGiveInformation(user, ticket);
    }
    return false;
    }
     */

    /*
    @Override
    public boolean userCanCancel(User user, Ticket ticket) {
    if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
    return super.userCanCancel(user, ticket);
    }
    return false;
    }
     */
    @Override
    @RequestCache
    public boolean userCanRequestInformation(User user, Ticket ticket) {
        if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
            return super.userCanRequestInformation(user, ticket);
        }
        return false;
    }

    @Override
    @RequestCache
    public boolean userCanClose(User user, Ticket ticket) {
        if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
            return super.userCanClose(user, ticket);
        }
        return false;
    }

    @Override
    @RequestCache
    public boolean userCanRefuse(User user, Ticket ticket) {
        if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
            return super.userCanRefuse(user, ticket);
        }
        return false;
    }

    @Override
    @RequestCache
    public boolean userCanConnect(User user, Ticket ticket) {
        if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
            return super.userCanConnect(user, ticket);
        }
        return false;
    }

    @Override
    @RequestCache
    public boolean userCanPostpone(User user, Ticket ticket) {
        if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
            return super.userCanPostpone(user, ticket);
        }
        return false;
    }

    @Override
    @RequestCache
    public boolean userCanCancelPostponement(User user, Ticket ticket) {
        if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
            return super.userCanCancelPostponement(user, ticket);
        }
        return false;
    }

    @Override
    @RequestCache
    public boolean userCanReopen(User user, Ticket ticket) {
        if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
            return super.userCanReopen(user, ticket);
        }
        return false;
    }

    @Override
    @RequestCache
    public boolean userCanMove(User user, Ticket ticket) {
        if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
            return super.userCanMove(user, ticket);
        }
        return false;
    }

    @Override
    @RequestCache
    public boolean userCanTake(User user, Ticket ticket) {
        if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
            return super.userCanTake(user, ticket);
        }
        return false;
    }

    @Override
    @RequestCache
    public boolean userCanTakeAndClose(User user, Ticket ticket) {
        if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
            return super.userCanTakeAndClose(user, ticket);
        }
        return false;
    }

    @Override
    @RequestCache
    public boolean userCanTakeAndRequestInformation(User user, Ticket ticket) {
        if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
            return super.userCanTakeAndRequestInformation(user, ticket);
        }
        return false;
    }

    @Override
    @RequestCache
    public boolean userCanFree(User user, Ticket ticket) {
        if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
            return super.userCanFree(user, ticket);
        }
        return false;
    }

    @Override
    @RequestCache
    public boolean userCanAssign(User user, Ticket ticket) {
        if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
            return super.userCanAssign(user, ticket);
        }
        return false;
    }

    @Override
    @RequestCache
    public boolean userCanChangeOwner(User user, Ticket ticket) {
        if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
            return super.userCanChangeOwner(user, ticket);
        }
        return false;
    }

    @Override
    @RequestCache
    public boolean userCanChangeLabel(User user, Ticket ticket) {
        if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
            return super.userCanChangeLabel(user, ticket);
        }
        return false;
    }

    @Override
    @RequestCache
    public boolean userCanChangeOrigin(User user, Ticket ticket) {
        if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
            return super.userCanChangeOrigin(user, ticket);
        }
        return false;
    }

    @Override
    @RequestCache
    public boolean userCanChangeComputer(User user, Ticket ticket) {
        if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
            return super.userCanChangeComputer(user, ticket);
        }
        return false;
    }

    @Override
    @RequestCache
    public boolean userCanChangeSpentTime(User user, Ticket ticket) {
        if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
            return super.userCanChangeSpentTime(user, ticket);
        }
        return false;
    }

    @Override
    @RequestCache
    public boolean userCanInviteGroup(User user, Ticket ticket) {
        if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
            return super.userCanInviteGroup(user, ticket);
        }
        return false;
    }

    @Override
    @RequestCache
    public boolean userCanUseResponses(User user, Ticket ticket) {
        if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
            return super.userCanUseResponses(user, ticket);
        }
        return false;
    }

    @Override
    @RequestCache
    public boolean userCanRemoveInvitations(final User user, final Ticket ticket) {
        if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
            return super.userCanRemoveInvitations(user, ticket);
        }
        return false;
    }

    @Override
    @RequestCache
    public boolean userCanInvite(final User user, final Ticket ticket) {
        if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
            return super.userCanInvite(user, ticket);
        }
        return false;
    }

    @Override
    @RequestCache
    public boolean userCanChangePriority(final User user, final Ticket ticket) {
        if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
            return super.userCanChangePriority(user, ticket);
        }
        return false;
    }

    @Override
    @RequestCache
    public boolean userCanChangeScope(final User user, final Ticket ticket) {
        if (!amiConnector.isCategoryMaintained(ticket.getCategory())) {
            return super.userCanChangeScope(user, ticket);
        }
        return false;
    }

    @Override
    protected Action changeDepartment(
            final User actionOwner,
            final Ticket ticket,
            final String actionMessage,
            final Department newDepartment,
            final String actionScope,
            final Category newCategory) {
        try {
            try {
                amiConnector.getAmiTicketNumber(ticket);
            } catch (AmiTicketNotFoundException ex) {
                logger.info("Ticket " + ticket + " not found in AMI...creating");
                amiConnector.syncMissingTicket(this, ticket, false);
            }
        } catch (Exception ex) {
            logger.fatal("Error creating AMI ticket", ex);
        }
        return super.changeDepartment(actionOwner, ticket, actionMessage, newDepartment, actionScope, newCategory);
    }
}
