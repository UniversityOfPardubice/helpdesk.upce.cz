/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.upce.helpdesk.domain.ami;

import cz.upce.helpdesk.domain.ami.exceptions.AmiTemplateNotFoundException;
import cz.upce.helpdesk.domain.ami.exceptions.AmiUserNotFoundException;
import cz.upce.helpdesk.domain.ami.exceptions.AmiTicketNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.mail.internet.InternetAddress;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.esupportail.commons.aop.cache.SessionCache;
import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.esupportail.commons.utils.Assert;
import org.esupportail.helpdesk.domain.ActionScope;
import org.esupportail.helpdesk.domain.ActionType;
import org.esupportail.helpdesk.domain.DomainService;
import org.esupportail.helpdesk.domain.TicketStatus;
import org.esupportail.helpdesk.domain.beans.AbstractTicket;
import org.esupportail.helpdesk.domain.beans.Action;
import org.esupportail.helpdesk.domain.beans.ArchivedAction;
import org.esupportail.helpdesk.domain.beans.ArchivedTicket;
import org.esupportail.helpdesk.domain.beans.Category;
import org.esupportail.helpdesk.domain.beans.Department;
import org.esupportail.helpdesk.domain.beans.DepartmentManager;
import org.esupportail.helpdesk.domain.beans.Ticket;
import org.esupportail.helpdesk.domain.beans.User;
import org.esupportail.helpdesk.domain.userManagement.UserStore;
import org.esupportail.helpdesk.exceptions.DepartmentManagerNotFoundException;
import org.esupportail.helpdesk.exceptions.TicketNotFoundException;
import org.esupportail.helpdesk.services.feed.ErrorHolder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.util.HtmlUtils;

/**
 *
 * @author lusl0338
 */
public class AmiConnector implements InitializingBean {

    /**
     * mapování helpdesk -> AMI
     */
    private Map<Integer, Long> priorityMapping = null;
    /**
     * mapování AMI -> helpdesk
     */
    private Map<Long, Integer> priorityInverseMapping = null;
    /**
     * mapování helpdesk -> AMI
     */
    private Map<String, Long> statusMapping = null;
    /**
     * mapování AMI -> helpdesk
     */
    private Map<Long, String> statusInverseMapping = null;
    /**
     * mapování helpdesk (ticket action) -> AMI
     */
    private Map<String, Long> actionMapping = null;
    /**
     * mapování helpdesk -> AMI
     */
    private Map<Long, Long> categoryTemplateMapping = null;
    private String jndiContext;
    private String jndiSource;
    private String jdbcDriverClass;
    private String jdbcUrl;
    private String jdbcUsername;
    private String jdbcPassword;
    private static DataSource jndiDataSource = null;
    private static final Logger logger = new LoggerImpl(AmiConnector.class);
    private UserStore userStore;
    private Boolean useJndi = Boolean.TRUE;
    private static boolean testing = false;
    private static final String AMICOMMID = "AMICOMMID";
    private static final String HELPDESKID = "HELPDESKID";
    private static final String POSTSYNC = "POSTSYNC";
    private static final String POZADAVEK_FYZICKY_VYRESEN = "POZADAVEK_FYZICKY_VYRESEN";

    public AmiConnector() {
        super();
    }

    private void initDbConnection() throws NamingException {
        Context ic = new InitialContext();
        Context context = (Context) ic.lookup(jndiContext);
        jndiDataSource = (DataSource) context.lookup(jndiSource);
    }

    public Connection getAmiConnection() throws Exception {
        if (useJndi && (jndiDataSource == null)) {
            try {
                initDbConnection();
            } catch (NamingException ex) {
                logger.info("Error initializing JNDI, using JDBC", ex);
                useJndi = Boolean.FALSE;
            }
        }
        if (useJndi) {
            return jndiDataSource.getConnection();
        } else {
            return DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
        }
    }

    public static void closeConnection(final Connection conn) {
        closeConnection(conn, null, null);
    }

    public static void closeConnection(final Connection conn, final Statement stmt) {
        closeConnection(conn, stmt, null);
    }

    public static void closeConnection(final Statement stmt, final ResultSet rs) {
        closeConnection(null, stmt, rs);
    }

    public static void closeConnection(final Connection conn, final Statement stmt, final ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ex) {
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ex) {
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ex) {
            }
        }
    }

    public Boolean isCategoryMaintained(final Category category) {
        return isCategoryMaintained(category, true);
    }

    public Boolean isCategoryMaintained(final Category category, boolean useTesting) {
        if (useTesting && testing) {
            return false;
        }
        return categoryTemplateMapping.containsKey(category.getId());
    }

    public Map<Long, Long> getCategoryTemplateMapping() {
        return categoryTemplateMapping;
    }

    public void setCategoryTemplateMapping(final Map<Long, Long> categoryTemplateMapping) {
        this.categoryTemplateMapping = categoryTemplateMapping;
    }

    public UserStore getUserStore() {
        return userStore;
    }

    public void setUserStore(final UserStore userStore) {
        this.userStore = userStore;
    }

    public String getJndiContext() {
        return jndiContext;
    }

    public void setJndiContext(final String jndiContext) {
        this.jndiContext = jndiContext;
    }

    public String getJndiSource() {
        return jndiSource;
    }

    public void setJndiSource(final String jndiSource) {
        this.jndiSource = jndiSource;
    }

    public Map<Long, Integer> getPriorityInverseMapping() {
        return priorityInverseMapping;
    }

    public void setPriorityInverseMapping(final Map<Long, Integer> priorityInverseMapping) {
        this.priorityInverseMapping = priorityInverseMapping;
    }

    public Map<Integer, Long> getPriorityMapping() {
        return priorityMapping;
    }

    public void setPriorityMapping(final Map<Integer, Long> priorityMapping) {
        this.priorityMapping = priorityMapping;
    }

    public Map<Long, String> getStatusInverseMapping() {
        return statusInverseMapping;
    }

    public void setStatusInverseMapping(final Map<Long, String> statusInverseMapping) {
        this.statusInverseMapping = statusInverseMapping;
    }

    public Map<String, Long> getStatusMapping() {
        return statusMapping;
    }

    public void setStatusMapping(final Map<String, Long> statusMapping) {
        this.statusMapping = statusMapping;
    }

    public Map<String, Long> getActionMapping() {
        return actionMapping;
    }

    public void setActionMapping(final Map<String, Long> actionMapping) {
        this.actionMapping = actionMapping;
    }

    public String getJdbcDriverClass() {
        return jdbcDriverClass;
    }

    public void setJdbcDriverClass(final String jdbcDriverClass) {
        this.jdbcDriverClass = jdbcDriverClass;
    }

    public String getJdbcPassword() {
        return jdbcPassword;
    }

    public void setJdbcPassword(final String jdbcPassword) {
        this.jdbcPassword = jdbcPassword;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(final String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getJdbcUsername() {
        return jdbcUsername;
    }

    public void setJdbcUsername(final String jdbcUsername) {
        this.jdbcUsername = jdbcUsername;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.priorityMapping,
                "property priorityMapping of class " + this.getClass().getName() + " can not be null");
        Assert.notNull(this.priorityInverseMapping,
                "property priorityInverseMapping of class " + this.getClass().getName() + " can not be null");
        Assert.notNull(this.statusMapping,
                "property statusMapping of class " + this.getClass().getName() + " can not be null");
        Assert.notNull(this.statusInverseMapping,
                "property statusInverseMapping of class " + this.getClass().getName() + " can not be null");
        Assert.notNull(this.actionMapping,
                "property actionMapping of class " + this.getClass().getName() + " can not be null");
        Assert.notNull(this.categoryTemplateMapping,
                "property categoryTemplateMapping of class " + this.getClass().getName() + " can not be null");
        Assert.hasText(this.jndiContext,
                "property jndiContext of class " + this.getClass().getName() + " can not be empty");
        Assert.hasText(this.jndiSource,
                "property jndiSource of class " + this.getClass().getName() + " can not be empty");
        Assert.notNull(this.userStore,
                "property userStore of class " + this.getClass().getName() + " can not be empty");
        Assert.hasText(this.jdbcDriverClass,
                "property jdbcDriverClass of class " + this.getClass().getName() + " can not be empty");
        Class.forName(jdbcDriverClass);
        Assert.hasText(this.jdbcUrl,
                "property jdbcUrl of class " + this.getClass().getName() + " can not be empty");
        Assert.hasText(this.jdbcUsername,
                "property jdbcUsername of class " + this.getClass().getName() + " can not be empty");
        Assert.hasText(this.jdbcPassword,
                "property jdbcPassword of class " + this.getClass().getName() + " can not be empty");
    }

    public static void setAllManagersMinimalAttributes(final DomainService domainService, final Department department) {
        List<DepartmentManager> departmentManagers = domainService.getDepartmentManagers(department);
        for (DepartmentManager departmentManager : departmentManagers) {
            setManagerMinimalAttributes(domainService, departmentManager);
        }
    }

    public static void setManagerMinimalAttributes(final DomainService domainService, final Department department, final User manager) throws DepartmentManagerNotFoundException {
        DepartmentManager departmentManager = domainService.getDepartmentManager(department, manager);
        setManagerMinimalAttributes(domainService, departmentManager);
    }

    private static void setManagerMinimalAttributes(final DomainService domainService, final DepartmentManager departmentManager) {
        if (!testing) {
            departmentManager.setAssignTicket(false);
            departmentManager.setAvailable(false);
            departmentManager.setManageCategories(false);
            departmentManager.setManageFaq(false);
            departmentManager.setManageManagers(false);
            departmentManager.setManageProperties(false);
            departmentManager.setModifyTicketDepartment(false);
            departmentManager.setRefuseTicket(false);
            departmentManager.setReopenAllTickets(false);
            departmentManager.setReportWeekend(false);
            departmentManager.setSetOwnAvailability(false);
            departmentManager.setTakeAlreadyAssignedTicket(false);
            departmentManager.setTakeFreeTicket(false);
            departmentManager.setTicketMonitoringAny(DepartmentManager.TICKET_MONITORING_NEVER);
            departmentManager.setTicketMonitoringCategory(DepartmentManager.TICKET_MONITORING_NEVER);
            departmentManager.setTicketMonitoringManaged(DepartmentManager.TICKET_MONITORING_NEVER);
            domainService.updateDepartmentManager(departmentManager);
        }
    }

    private class AmiTemplate {

        Long spravceId;
        Long skupinaSpravcuId;
        Float pozReakDobaPrij;
        Float pozReakDobaReseni;
        Long pozTypId;
        Long priorita;
        AmiDetail detail;
    }

    private AmiDetail getAmiTemplateDetail(Long pozTypId) throws Exception {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            connection = getAmiConnection();
            stmt = connection.prepareStatement("select DETAIL from C_POZ_TYP where C_POZ_TYP_ID=?");
            stmt.setLong(1, pozTypId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return new AmiDetail(this, rs.getString("DETAIL"));
            }
        } finally {
            closeConnection(connection, stmt, rs);
        }
        return new AmiDetail(this, "");
    }

    private AmiTemplate getAmiTemplate(final Long templateId) throws Exception {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            connection = getAmiConnection();
            stmt = connection.prepareStatement("select "
                    + "SPRAVCE_ID, "
                    + "SKUPINA_SPRAVCU_ID, "
                    + "POZ_REAK_DOBA_PRIJETI, "
                    + "POZ_REAK_DOBA_RESENI, "
                    + "C_POZ_TYP_ID, "
                    + "C_POZ_PRIORITA_ID "
                    + "from POZ_SABLONA where "
                    + "POZ_SABLONA_ID=?");
            stmt.setLong(1, templateId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                AmiTemplate amiTemplate = new AmiTemplate();
                amiTemplate.spravceId = rs.getLong("SPRAVCE_ID");
                amiTemplate.skupinaSpravcuId = rs.getLong("SKUPINA_SPRAVCU_ID");
                amiTemplate.pozReakDobaPrij = rs.getFloat("POZ_REAK_DOBA_PRIJETI");
                amiTemplate.pozReakDobaReseni = rs.getFloat("POZ_REAK_DOBA_RESENI");
                amiTemplate.pozTypId = rs.getLong("C_POZ_TYP_ID");
                amiTemplate.priorita = rs.getLong("C_POZ_PRIORITA_ID");
                amiTemplate.detail = getAmiTemplateDetail(amiTemplate.pozTypId);
                logger.info("Got AMI template ["
                        + "SPRAVCE_ID=" + amiTemplate.spravceId + ", "
                        + "SKUPINA_SPRAVCU_ID=" + amiTemplate.skupinaSpravcuId + ", "
                        + "POZ_REAK_DOBA_PRIJETI=" + amiTemplate.pozReakDobaPrij + ","
                        + "POZ_REAK_DOBA_RESENI=" + amiTemplate.pozReakDobaReseni + ","
                        + "C_POZ_TYP_ID=" + amiTemplate.pozTypId + ","
                        + "C_POZ_PRIORITA_ID=" + amiTemplate.priorita
                        + "]");
                return amiTemplate;
            } else {
                throw new AmiTemplateNotFoundException(templateId);
            }
        } finally {
            closeConnection(connection, stmt, rs);
        }
    }

    private List<Long> getAmiTicketIds() throws Exception {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Long> ret = null;
        try {
            connection = getAmiConnection();
            stmt = connection.prepareStatement("select "
                    + "POZADAVEK_ID "
                    + "from POZADAVEK "
                    + "order by POZADAVEK_ID");
            rs = stmt.executeQuery();
            ret = new LinkedList<Long>();
            while (rs.next()) {
                ret.add(rs.getLong("POZADAVEK_ID"));
            }
        } finally {
            closeConnection(connection, stmt, rs);
        }
        return ret;
    }

    private static String cleanupText(String text) {
        String out = text.replaceAll("&(nbsp|#160);", " ");
        out = out.replaceAll(" +", " ");
        out = out.replaceAll("\\<br[^>]*\\>\r?\n?", "\r\n"); // BR to NL
        out = out.replaceAll("\\<[^>]*\\>", ""); //odstraní všechny tagy
        out = HtmlUtils.htmlUnescape(out); //přeloží "podivné znaky" např. &gt;
        if (out.length() > 1024) {
            System.out.println("Truncating...");
            out = out.substring(0, 1020);
        }
        return out;
    }

    public void moveTicketAction(final DomainService domainService, final Ticket ticket, final String message) throws Exception {
        try {
            getAmiTicketNumber(ticket);
            return;
        } catch (AmiTicketNotFoundException ex) {
            logger.info("Creating AMI ticket for moved helpdesk ticket");
        }

        List<Action> actions = domainService.getActions(ticket);
        Collections.reverse(actions);
        for (Action action : actions) {
            if (action.getActionType().equals(ActionType.CREATE)) {
                createTicketAction(ticket, action.getMessage());
            } else {
                addAction(ticket, action);
            }
        }
    }

    public void createTicketAction(final Ticket ticket, final String message) throws Exception {
        createTicketAction(ticket, message, false);
    }

    public void createTicketAction(final Ticket ticket, final String message, boolean postSync) throws Exception {
        Long amiTemplateId = categoryTemplateMapping.get(ticket.getCategory().getId());
        if (amiTemplateId == null) {  //toto neni v AMI
            return;
        }
        AmiTemplate amiTemplate = getAmiTemplate(amiTemplateId);

        Connection connection = null;
        PreparedStatement stmt = null;
        try {
            connection = getAmiConnection();
            String query = "insert into POZADAVEK("
                    + "ZADAVATEL_ID, "
                    + "ZADAVATEL, "
                    + "ZADAVATEL_EMAIL, "
                    + "NAZEV, "
                    + "TEXT, "
                    + "C_POZ_STAV_ID, "
                    + "C_POZ_PRIORITA_ID, "
                    + "C_POZ_TYP_ID, "
                    + "POZ_REAK_DOBA_RESENI, "
                    + "POZ_REAK_DOBA_PRIJETI, "
                    + "SPRAVCE_ID, "
                    + "SKUPINA_SPRAVCU_ID, "
                    + "DETAIL) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            stmt = connection.prepareStatement(query);
            stmt.setLong(1, getAmiUserNumber(ticket.getCreator()));
            stmt.setString(2, ticket.getCreator().getDisplayName());//Zadavatel
            InternetAddress internetAddress = userStore.getUserInternetAddress(ticket.getCreator());
            String email = null;
            if (internetAddress != null) {
                email = userStore.getUserInternetAddress(ticket.getCreator()).getAddress();
            }
            if (email == null) {
                email = "ami@upce.cz";
            }
            stmt.setString(3, email);//Email zadavatele
            stmt.setString(4, ticket.getLabel());//Nazev
            stmt.setString(5, cleanupText(message));//Text
            stmt.setLong(6, 1);//Stav - FREE
            stmt.setLong(7, amiTemplate.priorita);//priorita
            stmt.setLong(8, amiTemplate.pozTypId);//typ
            stmt.setFloat(9, amiTemplate.pozReakDobaReseni);//poz. reak. doba reseni
            stmt.setFloat(10, amiTemplate.pozReakDobaPrij);//poz. reak. doba prijeti
            stmt.setLong(11, amiTemplate.spravceId);//spravce
            stmt.setLong(12, amiTemplate.skupinaSpravcuId);//skupina spravcu
            amiTemplate.detail.mergeTicketAttributes(ticket);
            amiTemplate.detail.setField(HELPDESKID, "ID v helpdesku", "" + ticket.getId(), "Number");
            amiTemplate.detail.setField(AMICOMMID, "ID komunikace", "-1", "Number");
            if (postSync) {
                amiTemplate.detail.setField(POSTSYNC, "Pozdní synchronizace", "Ano");
            }
            stmt.setString(13, amiTemplate.detail.getXmlAsString());//detail
            logger.info("Creating AMI ticket ["
                    + "user=" + getAmiUserNumber(ticket.getCreator()) + ", "
                    + "name=" + ticket.getCreator().getDisplayName() + ", "
                    + "email=" + email + ", "
                    + "label=" + ticket.getLabel() + ", "
                    + "category=" + amiTemplate.pozTypId
                    + "]");
            stmt.execute();
        } finally {
            closeConnection(connection, stmt);
        }
    }

    public void addAction(final Ticket ticket, final Action action) throws Exception {
        if (!isCategoryMaintained(ticket.getCategory(), false)
                || ActionType.CREATE.equals(action.getActionType())
                || (action.getUser() == null)) {
            return;
        }
        logger.info("New message for AMI ["
                + "ticket=" + ticket + ", "
                + "action=" + action + "]");
        addAmiMessage(ticket, action);
        changeAmiStatus(ticket, action);
    }

    public Long getAmiTicketNumber(final AbstractTicket ticket) throws Exception {
//        String query = "select POZADAVEK_ID from POZADAVEK where to_number(extract(XMLType(detail), '/DETAIL/FIELD[@NAME=\"HELPDESKID\"]/attribute::VALUE'))=?";
        //Původní verze s extrakcí pomocí XML je nehorázně pomalá, zaměněno za verzi s jednoduchým LIKE
        String query = "select POZADAVEK_ID from POZADAVEK"
                + " where DETAIL like '%VALUE=\"" + ticket.getId() + "\"%' and (REGEXP_LIKE(detail, '.*<FIELD[^>]*NAME=\"HELPDESKID\"[^>]*VALUE=\"" + ticket.getId() + "\"[^>]*/>.*') or REGEXP_LIKE(detail, '.*<FIELD[^>]*VALUE=\"" + ticket.getId() + "\"[^>]*NAME=\"HELPDESKID\"[^>]*/>.*'))";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getAmiConnection();
            stmt = conn.prepareStatement(query);
            //stmt.setLong(1, ticket.getId());
            rs = stmt.executeQuery();
            if (rs.next()) {
                Long ret = rs.getLong("POZADAVEK_ID");
                logger.info("Got AMI ID=" + ret + " for ticket=" + ticket);
                return ret;
            }
        } finally {
            closeConnection(conn, stmt, rs);
        }
        throw AmiTicketNotFoundException.getHelpdeskIdNotFound(ticket);
    }

    @SessionCache
    private Collection<Long> getTicketIdsFromAmi() throws Exception {
        String query = "select to_number(extract(XMLType(detail), '/DETAIL/FIELD[@NAME=\"HELPDESKID\"]/attribute::VALUE')) AMI_ID from POZADAVEK";
        Collection<Long> ret = new HashSet<Long>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getAmiConnection();
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Long id = rs.getLong("AMI_ID");
                ret.add(id);
            }
        } finally {
            closeConnection(conn, stmt, rs);
        }
        return ret;
    }

    @SessionCache
    private Long getAmiUserNumber(final User user) throws Exception {
        Long userId = getAmiUserNumber(user.getRealId());
        if (userId != null) {
            logger.info("Got AMI user=" + userId + " for user=" + user);
            return userId;
        }
        throw new AmiUserNotFoundException(user.getRealId());
    }

    @SessionCache
    private Long getAmiUserNumber(final String user) throws Exception {
        String query = "select USR_ID "
                + "from ZADAVATEL "
                + "where UPPER(USR_NAME)=UPPER(?)";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getAmiConnection();
            stmt = conn.prepareStatement(query);
            logger.info("AMI [query=[" + query + "], usr_name=[" + user + "]]");
            stmt.setString(1, user);
            rs = stmt.executeQuery();
            if (rs.next()) {
                Long ret = rs.getLong("USR_ID");
                return ret;
            }
        } finally {
            closeConnection(conn, stmt, rs);
        }
        return null;
    }

    @SessionCache
    private Long getAmiTicketNumber(final Long idByYear, final Long year) throws Exception {
        String query = "select POZADAVEK_ID "
                + "from POZADAVEK "
                + "where POZADAVEK_CISLO=? AND POZADAVEK_ROK=?";
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getAmiConnection();
            stmt = conn.prepareStatement(query);
            stmt.setLong(1, idByYear);
            stmt.setLong(2, year);
            rs = stmt.executeQuery();
            if (rs.next()) {
                Long ret = rs.getLong("POZADAVEK_ID");
                logger.info("Got AMI ID=" + ret + " for AMI ticket " + idByYear + "/" + year);
                return ret;
            }
        } finally {
            closeConnection(conn, stmt, rs);
        }
        throw AmiTicketNotFoundException.getAmiIdNotFound(idByYear, year);
    }

    private void changeAmiStatus(final Ticket ticket, final Action action) throws Exception {
        Long newStatus = null;
        if (actionMapping.containsKey(action.getActionType())) {
            newStatus = actionMapping.get(action.getActionType());
        } else {
            newStatus = statusMapping.get(ticket.getStatus());
        }
        if ((newStatus == null) || (newStatus < 0)) { //nic se nemeni
            return;
        }
        Long amiTicketId = getAmiTicketNumber(ticket);
        String query = "update POZADAVEK "
                + "set C_POZ_STAV_ID=? "
                + "where C_POZ_STAV_ID<>? and POZADAVEK_ID=?";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = getAmiConnection();
            stmt = conn.prepareStatement(query);
            stmt.setLong(1, newStatus);
            stmt.setLong(2, newStatus);
            stmt.setLong(3, amiTicketId);
            logger.info("Changing AMI status to " + newStatus + " for AMI ID=" + amiTicketId + " (" + ticket + ")");
            stmt.execute();
        } finally {
            closeConnection(conn, stmt);
        }
    }

    private void setNewAmiDetailCommentId(final Long amiId) throws Exception {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getAmiConnection();
            String query = "select DETAIL from POZADAVEK where POZADAVEK_ID=?";
            stmt = conn.prepareStatement(query);
            stmt.setLong(1, amiId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw AmiTicketNotFoundException.getAmiIdNotFound(amiId);
            }
            AmiDetail detail = new AmiDetail(this, rs.getString("DETAIL"));
            closeConnection(stmt, rs);

            query = "select max(POZ_KOMUNIKACE_ID) as ID from POZ_KOMUNIKACE"
                    + " where POZADAVEK_ID=? and"
                    + " C_TYP_KOMUNIKACE_ID=?";
            stmt = conn.prepareStatement(query);
            stmt.setLong(1, amiId);
            stmt.setLong(2, 1);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw AmiTicketNotFoundException.getAmiIdNotFound(amiId);
            }
            Long newId = rs.getLong("ID");
            if (newId > 0) {
                closeConnection(stmt, rs);

                detail.setField(AMICOMMID, "ID komunikace", newId.toString(), "Number");

                query = "update POZADAVEK set DETAIL=? where POZADAVEK_ID=?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, detail.getXmlAsString());
                stmt.setLong(2, amiId);
                logger.info("Setting comment ID to " + newId + " on AMI detail");
                stmt.execute();
            }
        } finally {
            closeConnection(conn, stmt, rs);
        }
    }

    private void addAmiMessage(final Ticket ticket, final Action action) throws Exception {
        if ((action == null) || (action.getMessage() == null) || ("".equals(action.getMessage()))) {
            return;
        }
        Long amiTicketId = getAmiTicketNumber(ticket);
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            String query = "insert into POZ_KOMUNIKACE("
                    + "TEXT, "
                    + "POZADAVEK_ID, "
                    + "C_TYP_KOMUNIKACE_ID, "
                    + "ZADAVATEL_ID) "
                    + "VALUES (?, ?, ?, ?)";
            conn = getAmiConnection();
            stmt = conn.prepareStatement(query);
            stmt.setString(1, cleanupText(action.getMessage()));
            stmt.setLong(2, amiTicketId);
            stmt.setLong(3, 1);
            stmt.setLong(4, getAmiUserNumber(action.getUser()));

            logger.info("Adding AMI message ["
                    + "ticketID=" + amiTicketId + "(" + ticket + "),"
                    + "author=" + getAmiUserNumber(action.getUser()) + " (" + action.getUser() + ")"
                    + "]");
            stmt.execute();
        } finally {
            closeConnection(conn, stmt);
        }
    }

    public Boolean processMessageSubject(final String subject, final DomainService domainService, final ErrorHolder errorHolder) throws Exception {
        Boolean ret;
        String yearIdStr = subject.replaceFirst(".*:(\\d*)/(\\d*).*", "$1");
        String yearStr = subject.replaceFirst(".*:(\\d*)/(\\d*).*", "$2");
        if ("".equals(yearIdStr) || "".equals(yearStr)) {
            errorHolder.addInfo("AMI header not found...");
            return true;
        }
        Long yearId = Long.parseLong(yearIdStr);
        Long year = Long.parseLong(yearStr);
        Long amiId = getAmiTicketNumber(yearId, year);

        errorHolder.addInfo("Found AMI ticket [" + yearId + "/" + year + " => " + amiId + "]");

        if (subject.startsWith("komentar:")) {
            errorHolder.addInfo("Have komentar");
            ret = processNewComments(amiId, domainService, errorHolder);
            return ret;
        }
        if (subject.startsWith("stav:")) {
            errorHolder.addInfo("Have stav");
            ret = processStateChange(amiId, domainService, errorHolder);
            return ret;
        }
        if (subject.startsWith("ukon:")) {
            errorHolder.addInfo("Have ukon");
            ret = processClose(amiId, domainService, errorHolder);
            return ret;
        }
        throw new UnsupportedOperationException("Email header was not recognized: " + subject);
    }

    public Boolean processAllMessages(DomainService domainService, ErrorHolder errorHolder) throws Exception {
        List<Long> amiIds = getAmiTicketIds();
        for (Long amiId : amiIds) {
            synchronizeTicket(domainService, errorHolder, amiId);
        }
        return Boolean.TRUE;
    }

    public boolean synchronizeTicket(DomainService domainService, ErrorHolder errorHolder, long amiId) {
        errorHolder.addInfo("Synchronizing AMI ticket id=" + amiId);
        try {
            processNewComments(amiId, domainService, errorHolder);
        } catch (Exception e) {
            errorHolder.addInfo("Error processing comment ID=" + amiId);
            return false;
        }
        try {
            processStateChange(amiId, domainService, errorHolder);
        } catch (Exception e) {
            errorHolder.addInfo("Error processing state ID=" + amiId);
            return false;
        }
        return true;
    }

    private Collection<ArchivedTicket> getArchivedTickets(final DomainService domainService, final Department department, Collection<Long> cats) {
        List<ArchivedTicket> tickets = domainService.getArchivedTickets(0, Integer.MAX_VALUE);
        Collection<ArchivedTicket> ret = new HashSet<ArchivedTicket>();
        for (ArchivedTicket archivedTicket : tickets) {
            if (archivedTicket.getDepartment().equals(department)
                    || archivedTicket.getCreationDepartment().equals(department)) {
                Category category = getCategoryByLabel(domainService, archivedTicket.getCategoryLabel());
                if (cats.contains(category.getId())) {
                    ret.add(archivedTicket);
                }
            }
        }
        return ret;
    }

    @SessionCache
    private Category getCategoryByLabel(DomainService domainService, String label) {
        List<Category> categories = domainService.getCategories();
        for (Category category : categories) {
            if (category.getLabel().equals(label)) {
                return category;
            }
        }
        return null;
    }

    private void syncMissingTicket(DomainService domainService, Ticket ticket, List<Action> actions, boolean postSync) throws Exception {
        System.out.println("Synchronizing ticket #" + ticket.getId() + " to AMI");

        Action createAction = null;
        List<Action> syncActions = new LinkedList<Action>();
        Collections.sort(actions, new Comparator<Action>() {

            public int compare(Action o1, Action o2) {
                long id1 = o1.getId();
                long id2 = o2.getId();
                if (id1 == id2) {
                    return 0;
                }
                if (id1 < id2) {
                    return -1;
                }
                return 1;
            }
        });
        User creator = null;
        for (Action action : actions) {
            if (ActionType.CREATE.equals(action.getActionType())) {
                createAction = action;
            } else {
                syncActions.add(action);
            }
            if ((creator == null) && (ActionType.CREATE.equals(action.getActionType())
                    || ActionType.CHANGE_DEPARTMENT.equals(action.getActionType()))) {
                try {
                    Long userNumber = getAmiUserNumber(action.getUser());
                    creator = action.getUser();
                } catch (AmiUserNotFoundException ex) {
                    //Ignore
                }
            }
        }
        ticket.setCreator(creator);
        createTicketAction(ticket, createAction.getMessage(), postSync);
        for (Action action : syncActions) {
            addAction(ticket, action);
        }
    }

    public void syncMissingTicket(DomainService domainService, Ticket ticket, boolean postSync) throws Exception {
        syncMissingTicket(domainService, ticket, domainService.getActions(ticket), postSync);
    }

    private void syncMissingTicket(DomainService domainService, ArchivedTicket ticket) throws Exception {
        Category category = getCategoryByLabel(domainService, ticket.getCategoryLabel());
        Ticket tempTicket = new Ticket(ticket.getOwner(), ticket.getOrigin(),
                ticket.getCreationDepartment(), category, ticket.getLabel(),
                ticket.getComputer(), ticket.getPriorityLevel(),
                ticket.getEffectiveScope());
        tempTicket.setId(ticket.getTicketId());
        tempTicket.setStatus(TicketStatus.ARCHIVED);

        List<ArchivedAction> actions = domainService.getArchivedActions(ticket);
        List<Action> tempActions = new LinkedList<Action>();
        for (ArchivedAction archivedAction : actions) {
            Action tempAction = new Action(archivedAction.getUser(), tempTicket,
                    archivedAction.getActionType(), archivedAction.getStatusAfter(),
                    archivedAction.getScopeAfter(), archivedAction.getMessage());
            tempAction.setId(archivedAction.getId());
            tempActions.add(tempAction);
        }

        syncMissingTicket(domainService, tempTicket, tempActions, true);
    }

    public Boolean syncMissingToAmi(DomainService domainService, ErrorHolder errorHolder, boolean includeArchived) throws Exception {
        Collection<AbstractTicket> toTickets = new HashSet<AbstractTicket>();
        Collection<Department> departments = new HashSet<Department>();
        Collection<Long> cats = new HashSet<Long>(categoryTemplateMapping.keySet());
        System.out.println("Getting tickets...");
        for (Long catId : cats) {
            Category category = domainService.getCategory(catId);
            toTickets.addAll(domainService.getTickets(category));
            departments.add(category.getDepartment());
        }
        if (includeArchived) {
            System.out.println("Getting archived tickets...");
            for (Department department : departments) {
                toTickets.addAll(getArchivedTickets(domainService, department, cats));
            }
        }

        System.out.println("Getting AMI IDs");
        Collection<Long> ticketIds = getTicketIdsFromAmi();

        for (AbstractTicket ticket : toTickets) {
            if ((ticket instanceof Ticket)
                    && !ticketIds.contains(((Ticket) ticket).getId())) {
                syncMissingTicket(domainService, (Ticket) ticket, true);
            }
            if ((ticket instanceof ArchivedTicket)
                    && !ticketIds.contains(((ArchivedTicket) ticket).getTicketId())) {
                syncMissingTicket(domainService, (ArchivedTicket) ticket);
            }
        }

        return Boolean.TRUE;
    }

    public Boolean syncMissingCommunication(DomainService domainService, ErrorHolder errorHolder) throws Exception {
        Boolean ret = Boolean.TRUE;
        for (Long amiTicketId : getAmiTicketIds()) {
            if (ret) {
                ret = processNewComments(amiTicketId, domainService, errorHolder);
            }
            if (ret) {
                ret = processStateChange(amiTicketId, domainService, errorHolder);
            }
        }
        return ret;
    }

    synchronized private Boolean processNewComments(final Long amiId, final DomainService domainService, final ErrorHolder errorHolder) throws Exception {
        Boolean ret = Boolean.FALSE;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getAmiConnection();
            String query = "";

            query = "select DETAIL, ZADAVATEL_ID from POZADAVEK where POZADAVEK_ID=?";
            stmt = conn.prepareStatement(query);
            stmt.setLong(1, amiId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw AmiTicketNotFoundException.getAmiIdNotFound(amiId);
            }
            AmiDetail amiDetail = new AmiDetail(this, rs.getString("DETAIL"));
            Long zadavatelId = rs.getLong("ZADAVATEL_ID");
            Long pozKomunikaceId = amiDetail.getFieldAsLong(AMICOMMID);
            Long ticketId = amiDetail.getFieldAsLong(HELPDESKID);
            if (ticketId == null) {
                errorHolder.addInfo("No Helpdesk ticket number found in detail");
                return true;
            }

            Ticket ticket = null;
            try {
                ticket = domainService.getTicket(ticketId);
            } catch (TicketNotFoundException ex) {
                errorHolder.addInfo("Helpdesk ticket # " + ticketId + " not found, possibly is archived (" + ex.getMessage() + ")");
                return true;
            }

            if (ticket == null) {
                errorHolder.addInfo("Helpdesk ticket # " + ticketId + " not found");
                return true;
            }

            closeConnection(stmt, rs);

            errorHolder.addInfo("Ticket comment for ticket: " + ticket);

            query = "select POZ_KOMUNIKACE.POZ_KOMUNIKACE_ID as ID,"
                    + " POZ_KOMUNIKACE.TEXT as TEXT,"
                    + " ZADAVATEL.USR_EMAIL as EMAIL"
                    + " from POZ_KOMUNIKACE"
                    + " inner join ZADAVATEL on (POZ_KOMUNIKACE.ZADAVATEL_ID=ZADAVATEL.USR_ID)"
                    + " where POZ_KOMUNIKACE.POZADAVEK_ID=? and"
                    + " POZ_KOMUNIKACE.POZ_KOMUNIKACE_ID>? and"
                    + " POZ_KOMUNIKACE.C_TYP_KOMUNIKACE_ID=?";
            /*
             * TODO - tohle půjde pryč, komunikace jen pro nezadavatele
             */
//                    + " POZ_KOMUNIKACE.ZADAVATEL_ID<>?";
            stmt = conn.prepareStatement(query);
            stmt.setLong(1, amiId);
            stmt.setLong(2, pozKomunikaceId);
            stmt.setLong(3, 1);
//            stmt.setLong(4, zadavatelId);

            rs = stmt.executeQuery();
            Long maxID = -1L;
            while (rs.next()) {
                String message = rs.getString("TEXT").replaceAll("\n", "\n<br/>");
                String email = rs.getString("EMAIL");
                maxID = Math.max(maxID, rs.getLong("ID"));
                User user = domainService.getUserStore().getUserWithEmail(email);
                errorHolder.addInfo("   Author: " + user);
                errorHolder.addInfo("   Message: " + message);
                if (domainService instanceof DomainServiceImplAmi) {
                    ((DomainServiceImplAmi) domainService).suspendAmiMessages();
                }
                domainService.giveInformation(user, ticket, message, ActionScope.DEFAULT, true);
            }
            closeConnection(stmt, rs);

            if (maxID > 0) {
                setNewAmiDetailCommentId(amiId);
            }
            ret = Boolean.TRUE;
        } finally {
            if (domainService instanceof DomainServiceImplAmi) {
                ((DomainServiceImplAmi) domainService).resumeAmiMessages();
            }
            closeConnection(conn, stmt, rs);
        }
        return ret;
    }

    private Boolean processClose(final Long amiId, final DomainService domainService, final ErrorHolder errorHolder) throws Exception {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (domainService instanceof DomainServiceImplAmi) {
                ((DomainServiceImplAmi) domainService).suspendAmiMessages();
            }
            String query = "select DETAIL, ZADAVATEL.USR_EMAIL "
                    + "from POZADAVEK "
                    + "  inner join ZADAVATEL on (POZADAVEK.SPRAVCE_ID=ZADAVATEL.USR_ID)"
                    + "where POZADAVEK_ID=?";
            conn = getAmiConnection();
            stmt = conn.prepareStatement(query);
            stmt.setLong(1, amiId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw AmiTicketNotFoundException.getAmiIdNotFound(amiId);
            }
            AmiDetail detail = new AmiDetail(this, rs.getString("DETAIL"));
            if (detail == null) {
                errorHolder.addInfo("AMI ticket is not handled in helpdesk (detail=null)");
                return true;
            }
            Long ticketId = detail.getFieldAsLong(HELPDESKID);
            if (ticketId == null) {
                errorHolder.addInfo("Helpdesk ticket number not found :-(");
                return true;
            }
            Ticket ticket = null;
            try {
                ticket = domainService.getTicket(ticketId);
            } catch (TicketNotFoundException ex) {
                errorHolder.addInfo("Helpdesk ticket # " + ticketId + " not found, possibly is archived (" + ex.getMessage() + ")");
                return true;
            }
            if (ticket == null) {
                errorHolder.addInfo("Helpdesk ticket # " + ticketId + " not found");
                return true;
            }
            if (!ticket.isOpened()) {
                errorHolder.addInfo("Ticket is already not openned...quitting");
                return true;
            }
            String email = rs.getString("USR_EMAIL");
            User manager = domainService.getUserStore().getUserWithEmail(email);
            domainService.closeTicket(manager, ticket, "", ActionScope.DEFAULT, false);
            return true;
        } finally {
            if (domainService instanceof DomainServiceImplAmi) {
                ((DomainServiceImplAmi) domainService).resumeAmiMessages();
            }
            closeConnection(conn, stmt, rs);
        }

    }

    private String inverseMapStatus(Long amiState, AmiDetail detail) {

        String state = null;
        if ("Ano".equals(detail.getFieldAsString(POZADAVEK_FYZICKY_VYRESEN))) {
            state = TicketStatus.CLOSED;
        } else {
            state = statusInverseMapping.get(amiState);
        }
        return state;
    }

    private Boolean processStateChange(final Long amiId, final DomainService domainService, final ErrorHolder errorHolder) throws Exception {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            String query = "select C_POZ_STAV_ID, DETAIL, ZADAVATEL.USR_EMAIL "
                    + "from POZADAVEK "
                    + "  inner join ZADAVATEL on (POZADAVEK.SPRAVCE_ID=ZADAVATEL.USR_ID)"
                    + "where POZADAVEK_ID=?";
            conn = getAmiConnection();
            stmt = conn.prepareStatement(query);
            stmt.setLong(1, amiId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw AmiTicketNotFoundException.getAmiIdNotFound(amiId);
            }
            Long state = rs.getLong("C_POZ_STAV_ID");
            AmiDetail detail = new AmiDetail(this, rs.getString("DETAIL"));
            if (detail == null) {
                errorHolder.addInfo("AMI ticket is not handled in helpdesk (detail=null)");
                return true;
            }
            String email = rs.getString("USR_EMAIL");
            User manager = domainService.getUserStore().getUserWithEmail(email);
            String newHelpdeskStatus = inverseMapStatus(state, detail);
            Long ticketId = detail.getFieldAsLong(HELPDESKID);
            if (ticketId == null) {
                errorHolder.addInfo("Helpdesk ticket number not found :-(");
                return true;
            }
            Ticket ticket = null;
            try {
                ticket = domainService.getTicket(ticketId);
            } catch (TicketNotFoundException ex) {
                errorHolder.addInfo("Helpdesk ticket # " + ticketId + " not found, possibly is archived (" + ex.getMessage() + ")");
                return true;
            }

            if (ticket == null) {
                errorHolder.addInfo("Helpdesk ticket # " + ticketId + " not found");
                return true;
            }

            errorHolder.addInfo("Ticket change for ticket: " + ticket);
            errorHolder.addInfo("  Original state: " + ticket.getStatus());
            errorHolder.addInfo("  New state: " + newHelpdeskStatus);
            errorHolder.addInfo("  Manager: " + manager);

            if (!domainService.isDepartmentManager(ticket.getDepartment(), manager)) {
                errorHolder.addInfo("Adding new manager...");
                domainService.addDepartmentManager(ticket.getDepartment(), manager);
            }
            setManagerMinimalAttributes(domainService, ticket.getDepartment(), manager);

            if (ticket.getStatus().equals(newHelpdeskStatus)) {
                errorHolder.addInfo("No status change...quitting");
                return true;
            }

            boolean returnStatus = false;
            if (domainService instanceof DomainServiceImplAmi) {
                ((DomainServiceImplAmi) domainService).suspendAmiMessages();
            }
            if (TicketStatus.FREE.equals(newHelpdeskStatus)) {
                errorHolder.addInfo("Freeing ticket...");
                domainService.freeTicket(manager, ticket, "", ActionScope.DEFAULT);
                returnStatus = true;
            }
            if (TicketStatus.INPROGRESS.equals(newHelpdeskStatus)) {
                if (TicketStatus.CLOSED.equals(ticket.getStatus())
                        || TicketStatus.CANCELLED.equals(ticket.getStatus())
                        || TicketStatus.REFUSED.equals(ticket.getStatus())
                        || TicketStatus.EXPIRED.equals(ticket.getStatus())
                        || TicketStatus.APPROVED.equals(ticket.getStatus())) {
                    errorHolder.addInfo("Reopenning ticket...");
                    domainService.reopenTicket(manager, ticket, "", ActionScope.DEFAULT);
                } else {
                    errorHolder.addInfo("Taking ticket...");
                    domainService.takeTicket(manager, ticket, "", ActionScope.DEFAULT);
                }
                returnStatus = true;
            }
            if (TicketStatus.CANCELLED.equals(newHelpdeskStatus)) {
                errorHolder.addInfo("Canceling ticket...");
                domainService.cancelTicket(manager, ticket, "", ActionScope.DEFAULT);
                returnStatus = true;
            }
            if (TicketStatus.REFUSED.equals(newHelpdeskStatus)) {
                errorHolder.addInfo("Refusing ticket...");
                domainService.refuseTicket(manager, ticket, "", ActionScope.DEFAULT);
                returnStatus = true;
            }
            if (TicketStatus.CLOSED.equals(newHelpdeskStatus)) {
                errorHolder.addInfo("Closing ticket...");
                domainService.closeTicket(manager, ticket, "", ActionScope.DEFAULT, false);
                returnStatus = true;
            }
            if (TicketStatus.APPROVED.equals(newHelpdeskStatus)) {
                errorHolder.addInfo("Approving ticket...");
                domainService.approveTicketClosure(manager, ticket, "", ActionScope.DEFAULT);
                returnStatus = true;
            }
            if (!returnStatus) {
                errorHolder.addError("Unknown status: " + newHelpdeskStatus);
            }
            return returnStatus;
        } finally {
            if (domainService instanceof DomainServiceImplAmi) {
                ((DomainServiceImplAmi) domainService).resumeAmiMessages();
            }
            closeConnection(conn, stmt, rs);
        }
    }

    public boolean getTesting() {
        return testing;
    }

    public void setTesting(boolean testing) {
        AmiConnector.testing = testing;
    }
}
