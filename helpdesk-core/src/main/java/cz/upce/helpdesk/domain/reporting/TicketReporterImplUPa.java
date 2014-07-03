/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.upce.helpdesk.domain.reporting;

import java.util.List;
import java.util.Map;
import org.esupportail.helpdesk.domain.beans.Department;
import org.esupportail.helpdesk.domain.beans.DepartmentManager;
import org.esupportail.helpdesk.domain.beans.Ticket;
import org.esupportail.helpdesk.domain.beans.User;
import org.esupportail.helpdesk.domain.reporting.TicketReporterImpl;
import org.esupportail.helpdesk.exceptions.DepartmentManagerNotFoundException;

/**
 *
 * @author lusl0338
 */
public class TicketReporterImplUPa extends TicketReporterImpl {

    @Override
    protected void sendTicketReport(
            final DepartmentManager manager,
            final List<Ticket> openedTickets) {
        Map<String, List<Ticket>> computeReportingResults = computeReporting(manager, openedTickets);
        if (!shouldSendReport(computeReportingResults)) {
            return;
        }
        super.sendTicketReport(manager, openedTickets);
    }

    @Override
    protected void sendTicketReport(
            final int hour,
            final boolean weekend,
            final User user,
            final Map<Department, List<Ticket>> openedTicketsMap) {
        boolean shouldSend = false;
        for (Department department : getDomainService().getDepartments()) {
            try {
                DepartmentManager manager = getDomainService().getDepartmentManager(department, user);
                if (isReportingManager(hour, weekend, manager)) {
                    Map<String, List<Ticket>> computeReportingResults =
                            computeReporting(manager, openedTicketsMap.get(department));
                    if (shouldSendReport(computeReportingResults)) {
                        shouldSend = true;
                        break;
                    }
                }
            } catch (DepartmentManagerNotFoundException e) {
                // not managing this department
            }
        }
        if (shouldSend) {
            super.sendTicketReport(hour, weekend, user, openedTicketsMap);
        }
    }

    private boolean shouldSendReport(Map<String, List<Ticket>> computeReportingResults) {
        for (List<Ticket> list : computeReportingResults.values()) {
            if (!list.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
