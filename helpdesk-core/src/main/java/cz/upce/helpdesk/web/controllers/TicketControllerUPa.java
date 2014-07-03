/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.upce.helpdesk.web.controllers;

import cz.upce.helpdesk.domain.DomainServiceImplUPa;
import java.util.ArrayList;
import java.util.List;
import org.apache.myfaces.custom.tree2.TreeNode;
import org.apache.myfaces.custom.tree2.TreeNodeBase;
import org.esupportail.commons.aop.cache.RequestCache;
import org.esupportail.commons.utils.DownloadUtils;
import org.esupportail.helpdesk.domain.DomainService;
import org.esupportail.helpdesk.domain.beans.Action;
import org.esupportail.helpdesk.domain.beans.Department;
import org.esupportail.helpdesk.web.beans.CategoryNode;
import org.esupportail.helpdesk.web.beans.TicketHistoryEntry;
import org.esupportail.helpdesk.web.controllers.TicketController;

/**
 *
 * @author lusl0338
 */
public class TicketControllerUPa extends TicketController {

    private static final long serialVersionUID = -6631628667580475697L;

    /**
     * JSF callback.
     * @return a String.
     */
    public String shortPrint() {
        boolean updated = updateTicket();
        if (updated) {
            return null;
        }
        DomainService domainService = getDomainService();
        if (domainService instanceof DomainServiceImplUPa) {
            DomainServiceImplUPa domainServiceUPa = (DomainServiceImplUPa) domainService;
            setDownloadId(DownloadUtils.setDownload(
                    domainServiceUPa.getTicketShortPrintContent(getCurrentUser(), getTicket()).getBytes(),
                    getTicket().getId() + "-" + System.currentTimeMillis() + ".html",
                    "text/html"));
        } else {
            return print();
        }
        return null;
    }

    @Override
    protected TreeNode buildRootAddNode() {
        TreeNode rootNode = new TreeNodeBase("root", "root", true);
        for (Department department : getDomainService().getTicketCreationDepartments(
                getCurrentUser(), getClient())) {
            Department realDepartment;
            if (department.isVirtual()) {
                realDepartment = department.getRealDepartment();
            } else {
                realDepartment = department;
            }
            if (!realDepartment.getHideToExternalUsers()
                    || !getDomainService().getUserStore().isApplicationUser(getCurrentUser())) {
                CategoryNode departmentNode = new CategoryNode(department);
                addAddTreeSubCategories(
                        department, departmentNode,
                        getRootCategories(realDepartment));
                rootNode.getChildren().add(departmentNode);
                rootNode.setLeaf(false);
            }
        }
        return rootNode;
    }

    @Override
    protected boolean checkActionMessageLength() {
        return true;
    }

    /**
     * @return the historyEntries
     */
    @RequestCache
    @Override
    public List<TicketHistoryEntry> getHistoryEntries() {
        if (getTicket() == null) {
            return null;
        }
        List<TicketHistoryEntry> historyEntries = new ArrayList<TicketHistoryEntry>();
        boolean invited = isInvited();
        for (Action action : getDomainService().getActions(getTicket())) {
            if (getDomainService().userCanViewActionMessage(getCurrentUser(), invited, action)) {
                historyEntries.add(new TicketHistoryEntry(
                        action,
                        getDomainService().userCanViewActionMessage(
                        getCurrentUser(), invited, action),
                        getDomainService().userCanChangeActionScope(
                        getCurrentUser(), action),
                        getDomainService().getAlerts(action),
                        getDomainService().getActionStyleClass(action)));
            }
        }
        return historyEntries;
    }
}
