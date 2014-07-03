/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.upce.helpdesk.domain;

import cz.upce.helpdesk.domain.reporting.TicketPrinterImplUPa;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.esupportail.commons.aop.cache.RequestCache;
import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.esupportail.helpdesk.domain.ActionScope;
import org.esupportail.helpdesk.domain.DomainServiceImpl;
import org.esupportail.helpdesk.domain.beans.Category;
import org.esupportail.helpdesk.domain.beans.Department;
import org.esupportail.helpdesk.domain.beans.Ticket;
import org.esupportail.helpdesk.domain.beans.TicketAttributeData;
import org.esupportail.helpdesk.domain.beans.User;
import org.esupportail.helpdesk.domain.reporting.TicketPrinter;

/**
 *
 * @author lusl0338
 */
public class DomainServiceImplUPa extends DomainServiceImpl {

    private static final long serialVersionUID = -8866490149093957526L;
    private final Logger logger = new LoggerImpl(getClass());

    @Override
    @RequestCache
    public boolean userCanChangeScope(final User user, final Ticket ticket) {
        if (user == null) {
            return false;
        }
        return user.equals(ticket.getManager());
    }

    @Override
    @RequestCache
    public boolean userCanChangePriority(final User user, final Ticket ticket) {
        if (user == null) {
            return false;
        }
        return user.equals(ticket.getManager());
    }

    @Override
    @RequestCache
    public boolean userCanInvite(final User user, final Ticket ticket) {
        Department department = ticket.getDepartment();
        if (isDepartmentManager(department, user)) {
            return true;
        }
        return false;
    }

    @Override
    @RequestCache
    public boolean userCanRemoveInvitations(final User user, final Ticket ticket) {
        Department department = ticket.getDepartment();
        if (isDepartmentManager(department, user)) {
            return true;
        }
        return false;
    }

    @RequestCache
    public String getTicketShortPrintContent(
            final User user,
            final Ticket ticket) {
        TicketPrinter ticketPrinter = getTicketPrinter();
        if (ticketPrinter instanceof TicketPrinterImplUPa) {
            TicketPrinterImplUPa ticketPrinterImplUPa = (TicketPrinterImplUPa) ticketPrinter;
            return ticketPrinterImplUPa.getTicketShortPrintContent(user, ticket);
        }
        return ticketPrinter.getTicketPrintContent(user, ticket);
    }
    private static DataSource dataSource;
    private static final String JNDI = "jdbc/ora-ost";

    private static Connection getConnection() throws NamingException, SQLException {
        if (dataSource == null) {
            Context ic = new InitialContext();
            Context context = (Context) ic.lookup("java:comp/env");
            dataSource = (DataSource) context.lookup(JNDI);
        }
        return dataSource.getConnection();
    }

    private void addDbInvitedUsers(final Department department, final Ticket ticket) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement("select INVITE from HELPDESK_UZIVATELE where NETID=? AND ODDELENI=?");
            stmt.setString(1, ticket.getCreator().getRealId());
            stmt.setString(2, department.getLabel());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String invited = rs.getString("INVITE");
                if (invited != null && !invited.isEmpty()) {
                    for (String user : invited.split(",")) {
                        User invitedUser = getUserStore().getUserFromRealId(user);
                        if (!isInvited(invitedUser, ticket)) {
                            logger.info("Inviting user " + invitedUser + " to ticket " + ticket);
                            invite(null, ticket, invitedUser, null, ActionScope.DEFAULT, true);
                        }
                    }
                }
            }
            rs.close();
        } catch (Exception ex) {
            logger.error("Error inviting users from DB", ex);
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ex) {
                //nothing to close
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                //nothing to close
            }
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
        Ticket ticket = super.addWebTicket(author, owner, creationDepartment, category, label, computer, priorityLevel, message, ticketScope, ticketOrigin, ticketAttributesData);
        addDbInvitedUsers(creationDepartment, ticket);
        return ticket;
    }

    @Override
    public Ticket addEmailTicket(
            final User sender,
            final String address,
            final Department creationDepartment,
            final Category category,
            final String label) {
        Ticket ticket = super.addEmailTicket(sender, address, creationDepartment, category, label);
        addDbInvitedUsers(creationDepartment, ticket);
        return ticket;
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
        if (!ticket.getDepartment().equals(targetCategory.getDepartment())) {
            addDbInvitedUsers(ticket.getDepartment(), ticket);
        }
    }

    @Override
    public void changeTicketOwner(
            final User author,
            final Ticket ticket,
            final User owner,
            final String message,
            final String actionScope,
            final boolean alerts) {
        super.changeTicketOwner(author, ticket, owner, message, ActionScope.OWNER, alerts);
    }
}
