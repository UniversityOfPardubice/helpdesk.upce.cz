/* $Id: AlertSenderUtils.java 216 2012-07-18 14:45:54Z lusl0338 $ */
package cz.upce.helpdesk.domain.reporting;

import java.util.List;
import java.util.Locale;
import org.esupportail.commons.services.i18n.I18nService;
import org.esupportail.commons.utils.strings.StringUtils;
import org.esupportail.helpdesk.domain.ActionI18nTitleFormatter;
import org.esupportail.helpdesk.domain.DomainService;
import org.esupportail.helpdesk.domain.TicketScope;
import org.esupportail.helpdesk.domain.beans.Action;
import org.esupportail.helpdesk.domain.beans.Ticket;
import org.esupportail.helpdesk.domain.beans.User;
import org.esupportail.helpdesk.domain.userFormatting.UserFormattingService;
import org.esupportail.helpdesk.web.beans.ElapsedTimeI18nFormatter;
import org.esupportail.helpdesk.web.beans.OriginI18nKeyProvider;
import org.esupportail.helpdesk.web.beans.PriorityI18nKeyProvider;
import org.esupportail.helpdesk.web.beans.PriorityStyleClassProvider;
import org.esupportail.helpdesk.web.beans.SpentTimeI18nFormatter;
import org.esupportail.helpdesk.web.beans.TicketScopeI18nKeyProvider;
import org.esupportail.helpdesk.web.beans.TicketStatusI18nKeyProvider;

/**
 *
 * @author $LastChangedBy: lusl0338 $
 * $Id: AlertSenderUtils.java 216 2012-07-18 14:45:54Z lusl0338 $
 */
public class AlertSenderUtils {

    private I18nService i18nService;
    private DomainService domainService;
    private ActionI18nTitleFormatter actionI18nTitleFormatter;
    private UserFormattingService userFormattingService;

    public AlertSenderUtils(I18nService i18nService,
            DomainService domainService,
            ActionI18nTitleFormatter actionI18nTitleFormatter,
            UserFormattingService userFormattingService) {
        this.i18nService = i18nService;
        this.domainService = domainService;
        this.actionI18nTitleFormatter = actionI18nTitleFormatter;
        this.userFormattingService = userFormattingService;
    }

    public String getEmailTicketLabel(final User user, final Ticket ticket) {
        String htmlContent = "";
        Locale locale = domainService.getUserStore().getUserLocale(user);
        htmlContent += i18nService.getString(
                "EMAIL.TICKET.COMMON.HISTORY.TICKET_LABEL", locale, ticket.getId(), ticket.getLabel());
        return htmlContent;
    }

    public Action getLastAction(final User user, final Ticket ticket, final boolean forceInvited, final boolean includeApplication) {
        boolean invited = false;
        if (user != null) {
            invited = domainService.isInvited(user, ticket);
        }
        if (forceInvited) {
            invited = true;
        }
        List<Action> actions = domainService.getActions(ticket);
        Action lastAction = null;
        for (Action action : actions) {
            if (domainService.userCanViewActionMessage(user, invited, action) &&
                    ((action.getUser() != null) || includeApplication)) {
                lastAction = action;
                break;
            }
        }
        return lastAction;
    }

    public String getEmailLastAction(final User user, final Ticket ticket, final boolean setOnNull, final boolean forceInvited) {
        String htmlContent = "";
        Locale locale = domainService.getUserStore().getUserLocale(user);
        Action lastAction = getLastAction(user, ticket, forceInvited, false);
        if (lastAction != null) {
            String message = lastAction.getMessage();
            if ((message == null) || (message.length() == 0)) {
                message = "";
                if (!setOnNull) {
                    return "";
                }
            }
            String actionTitle = StringUtils.escapeHtml(actionI18nTitleFormatter.getActionTitle(lastAction, locale));
            htmlContent += "<br /><table class=\"history-first\">";
            htmlContent += i18nService.getString("EMAIL.TICKET.COMMON.HISTORY.ENTRY", locale, "even", domainService.getActionStyleClass(lastAction), actionTitle, message);
            htmlContent += "</table><br />";
        }
        return htmlContent;
    }

    public String getEmailOrPrintHistory(
            final User user,
            final Ticket ticket,
            final boolean forceInvited,
            final boolean ommitFirst) {
        String result = "";
        Locale locale = domainService.getUserStore().getUserLocale(user);
        result += i18nService.getString(
                "EMAIL.TICKET.COMMON.HISTORY.HEADER", locale,
                String.valueOf(ticket.getId()), ticket.getLabel());
        boolean invited = false;
        if (user != null) {
            invited = domainService.isInvited(user, ticket);
        }
        if (forceInvited) {
            invited = true;
        }
        boolean alternateColor = false;
        boolean firstAction = ommitFirst;
        boolean moreActions = false;
        for (Action action : domainService.getActions(ticket)) {
            String trClass = "table-";
            if (alternateColor) {
                trClass += "odd";
            } else {
                trClass += "even";
            }
            String actionTitle = StringUtils.escapeHtml(
                    actionI18nTitleFormatter.getActionTitle(action, locale));
            String message = null;
            if (domainService.userCanViewActionMessage(user, invited, action) &&
                    action.getUser() != null) {
                if (org.springframework.util.StringUtils.hasText(action.getMessage())) {
                    message = action.getMessage();
                } else {
                    message = "";
                }
            }

            if (message != null) {
                if (firstAction) {
                    firstAction = false;
                } else {
                    result += i18nService.getString(
                            "EMAIL.TICKET.COMMON.HISTORY.ENTRY", locale,
                            trClass, domainService.getActionStyleClass(action), actionTitle, message);
                    alternateColor = !alternateColor;
                    moreActions = true;
                }
            }
        }
        
        if (!moreActions) {
            return "";
        }

        result += i18nService.getString(
                "EMAIL.TICKET.COMMON.HISTORY.FOOTER", locale);
        return result;
    }

    protected String getEmailOrPrintProperties(
            final User user,
            final Ticket ticket) {
        String result = "";
        Locale locale = domainService.getUserStore().getUserLocale(user);
        String owner = "<strong>" + i18nService.getString(
                "TICKET_VIEW.PROPERTIES.USER", locale,
                StringUtils.escapeHtml(userFormattingService.format(ticket.getOwner(), locale))) + "</strong>";
        String manager;
        if (ticket.getManager() != null) {
            manager = "<strong>" + i18nService.getString(
                    "TICKET_VIEW.PROPERTIES.USER", locale,
                    StringUtils.escapeHtml(
                    userFormattingService.format(ticket.getManager(), locale))) + "</strong>";
        } else {
            manager = "<em>" + i18nService.getString(
                    "TICKET_VIEW.PROPERTIES.NO_MANAGER", locale) + "</em>";
        }
        String status = "<strong>" + i18nService.getString(
                TicketStatusI18nKeyProvider.getI18nKey(ticket.getStatus()), locale) + "</strong>";
        String creationDepartmentLabel;
        if (ticket.getCreationDepartment() != null) {
            creationDepartmentLabel = "<strong>" + StringUtils.escapeHtml(
                    ticket.getCreationDepartment().getLabel()) + "</strong>";
        } else {
            creationDepartmentLabel = "<em>" + i18nService.getString(
                    "TICKET_VIEW.PROPERTIES.DEPARTMENT_REMOVED", locale) + "</em>";
        }
        String categoryLabel = "<strong>" + i18nService.getString(
                "TICKET_VIEW.PROPERTIES.CATEGORY_VALUE", locale,
                StringUtils.escapeHtml(ticket.getDepartment().getLabel()),
                StringUtils.escapeHtml(ticket.getCategory().getLabel())) + "</strong>";
        String scope = "-";
        if (domainService.userCanChangeScope(user, ticket)) {
            scope = "<strong>";
            if (TicketScope.DEFAULT.equals(ticket.getScope())) {
                scope += i18nService.getString(
                        "TICKET_VIEW.PROPERTIES.SCOPE_VALUE_DEFAULT", locale,
                        i18nService.getString(TicketScopeI18nKeyProvider.getI18nKey(
                        TicketScope.DEFAULT), locale),
                        i18nService.getString(TicketScopeI18nKeyProvider.getI18nKey(
                        ticket.getEffectiveScope()), locale));
            } else {
                scope += i18nService.getString(TicketScopeI18nKeyProvider.getI18nKey(ticket.getScope()));
            }
            scope += "</strong>";
        }
        String priority = "-";
        if (domainService.userCanChangePriority(user, ticket)) {
            priority = "<strong><span class=\"" + PriorityStyleClassProvider.getStyleClass(ticket.getEffectivePriorityLevel()) + "\">";
            if (ticket.getPriorityLevel() == 0) {
                priority += i18nService.getString(
                        "TICKET_VIEW.PROPERTIES.PRIORITY_VALUE_DEFAULT", locale,
                        i18nService.getString(PriorityI18nKeyProvider.getI18nKey(
                        new Integer(0)), locale),
                        i18nService.getString(PriorityI18nKeyProvider.getI18nKey(
                        new Integer(ticket.getEffectivePriorityLevel())),
                        locale));
            } else {
                priority += i18nService.getString(PriorityI18nKeyProvider.getI18nKey(
                        new Integer(ticket.getEffectivePriorityLevel())), locale);
            }
            priority += "</span></strong>";
        }
        String computer;
        if (ticket.getComputer() != null) {
            computer = "<strong>" + StringUtils.escapeHtml(ticket.getComputer()) + "</strong>";
        } else {
            computer = "<em>" + i18nService.getString(
                    "TICKET_VIEW.PROPERTIES.NO_COMPUTER", locale) + "</em>";
        }
        String spentTime;
        if (ticket.getSpentTime() == -1) {
            spentTime = "<em>" + i18nService.getString(
                    "TICKET_VIEW.PROPERTIES.NO_SPENT_TIME", locale) + "</em>";
        } else {
            spentTime = "<strong>" + SpentTimeI18nFormatter.format(i18nService, ticket.getSpentTime(), locale) + "</strong>";
        }
        String origin = "<strong>" + i18nService.getString(
                OriginI18nKeyProvider.getI18nKey(ticket.getOrigin()), locale) + "</strong>";
        String creationDate = "<strong>" + ticket.getCreationDate() + "</strong>";
        String lastActionDate = "<strong>" + ticket.getLastActionDate() + "</strong>";
        String chargeTime;
        if (ticket.getChargeTime() == null) {
            chargeTime = "<em>" + ElapsedTimeI18nFormatter.format(
                    i18nService, ticket.getChargeTime(), locale) + "</em>";
        } else {
            chargeTime = "<strong>" + ElapsedTimeI18nFormatter.format(
                    i18nService, ticket.getChargeTime(), locale) + "</strong>";
        }
        String closureTime;
        if (ticket.getClosureTime() == null) {
            closureTime = "<em>" + ElapsedTimeI18nFormatter.format(
                    i18nService, ticket.getClosureTime(), locale) + "</em>";
        } else {
            closureTime = "<strong>" + ElapsedTimeI18nFormatter.format(
                    i18nService, ticket.getClosureTime(), locale) + "</strong>";
        }
        result += i18nService.getString(
                "EMAIL.TICKET.COMMON.PROPERTIES", locale,
                new Object[]{
                    owner, manager, status, creationDepartmentLabel, categoryLabel,
                    scope, priority, computer, spentTime, origin, creationDate, lastActionDate,
                    chargeTime, closureTime,});
        return result;
    }
}

