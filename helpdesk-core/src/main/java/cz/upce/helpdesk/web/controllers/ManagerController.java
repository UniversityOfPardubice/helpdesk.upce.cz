/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.upce.helpdesk.web.controllers;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.Level;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.esupportail.commons.services.i18n.I18nService;
import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.esupportail.commons.utils.Assert;
import org.esupportail.helpdesk.domain.beans.User;
import org.esupportail.helpdesk.web.controllers.SessionController;
import org.springframework.beans.factory.InitializingBean;

/**
 *
 * @author lusl0338
 */
public class ManagerController extends HashMap<User, String> implements InitializingBean {

    private I18nService i18nService;
    private SessionController sessionController;
    private String jndiContext;
    private String jndiSource;
    private String query;
    private String dovolenaOdAttribute;
    private String dovolenaDoAttribute;
    private String sluzebniCestaOdAttribute;
    private String sluzebniCestaDoAttribute;
    private DataSource jndiDataSource = null;
    private final Logger logger = new LoggerImpl(getClass());

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(this.i18nService,
                "property i18nService of class " + this.getClass().getName()
                + " can not be null");
        Assert.notNull(sessionController,
                "property sessionController of class " + this.getClass().getName()
                + " can not be null");
        Assert.hasText(this.jndiContext,
                "property jndiContext of class " + this.getClass().getName()
                + " can not be null");
        Assert.hasText(this.jndiSource,
                "property jndiSource of class " + this.getClass().getName()
                + " can not be null");
        Assert.hasText(this.query,
                "property query of class " + this.getClass().getName()
                + " can not be null");
        Assert.hasText(this.dovolenaOdAttribute,
                "property dovolenaOdAttribute of class " + this.getClass().getName()
                + " can not be null");
        Assert.hasText(this.dovolenaDoAttribute,
                "property dovolenaDoAttribute of class " + this.getClass().getName()
                + " can not be null");
        Assert.hasText(this.sluzebniCestaOdAttribute,
                "property sluzebniCestaOdAttribute of class " + this.getClass().getName()
                + " can not be null");
        Assert.hasText(this.sluzebniCestaDoAttribute,
                "property sluzebniCestaDoAttribute of class " + this.getClass().getName()
                + " can not be null");
    }

    public I18nService getI18nService() {
        return i18nService;
    }

    public void setI18nService(I18nService i18nService) {
        this.i18nService = i18nService;
    }

    public SessionController getSessionController() {
        return sessionController;
    }

    public void setSessionController(SessionController sessionController) {
        this.sessionController = sessionController;
    }

    public String getJndiContext() {
        return jndiContext;
    }

    public void setJndiContext(String jndiContext) {
        this.jndiContext = jndiContext;
    }

    public String getJndiSource() {
        return jndiSource;
    }

    public void setJndiSource(String jndiSource) {
        this.jndiSource = jndiSource;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getDovolenaDoAttribute() {
        return dovolenaDoAttribute;
    }

    public void setDovolenaDoAttribute(String dovolenaDoAttribute) {
        this.dovolenaDoAttribute = dovolenaDoAttribute;
    }

    public String getDovolenaOdAttribute() {
        return dovolenaOdAttribute;
    }

    public void setDovolenaOdAttribute(String dovolenaOdAttribute) {
        this.dovolenaOdAttribute = dovolenaOdAttribute;
    }

    public String getSluzebniCestaDoAttribute() {
        return sluzebniCestaDoAttribute;
    }

    public void setSluzebniCestaDoAttribute(String sluzebniCestaDoAttribute) {
        this.sluzebniCestaDoAttribute = sluzebniCestaDoAttribute;
    }

    public String getSluzebniCestaOdAttribute() {
        return sluzebniCestaOdAttribute;
    }

    public void setSluzebniCestaOdAttribute(String sluzebniCestaOdAttribute) {
        this.sluzebniCestaOdAttribute = sluzebniCestaOdAttribute;
    }

    private void initDbConnection() throws NamingException {
        Context ic = new InitialContext();
        Context context = (Context) ic.lookup(jndiContext);
        jndiDataSource = (DataSource) context.lookup(jndiSource);
    }

    private Connection getConnection() throws Exception {
        if (jndiDataSource == null) {
            initDbConnection();
        }
        return jndiDataSource.getConnection();
    }

    @Override
    public String get(final Object o) {
        if (o == null) {
            return null;
        }
        if (!(o instanceof User)) {
            return null;
        }

        User user = (User) o;

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        Date dovolenaOd = null;
        Date dovolenaDo = null;
        Date sluzebniCestaOd = null;
        Date sluzebniCestaDo = null;

        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setString(1, user.getRealId());
            result = stmt.executeQuery();

            if (!result.next()) {
                return null;
            }
            dovolenaOd = result.getDate(dovolenaOdAttribute);
            dovolenaDo = result.getDate(dovolenaDoAttribute);
            sluzebniCestaOd = result.getDate(sluzebniCestaOdAttribute);
            sluzebniCestaDo = result.getDate(sluzebniCestaDoAttribute);
        } catch (Exception ex) {
            logger.error("Error querying Vacation and Bussines trips", ex);
            return null;
        } finally {
            if (result != null) {
                try {
                    result.close();
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

        if ((dovolenaOd == null) && (dovolenaDo == null)
                && (sluzebniCestaOd == null) && (sluzebniCestaDo == null)) {
            return null;
        }
        Locale locale = sessionController.getCurrentUserLocale();
        SimpleDateFormat dateFormat = new SimpleDateFormat(i18nService.getString("MANAGER_VACATION.DATE_FORMAT", locale), locale);

        String dovolenaRet = formatDateSpan(dateFormat, locale, dovolenaOd, dovolenaDo);
        String sluzebniCestaRet = formatDateSpan(dateFormat, locale, sluzebniCestaOd, sluzebniCestaDo);
        if ((dovolenaRet == null) && (sluzebniCestaRet == null)) {
            return null;
        }
        if ((dovolenaRet == null) && (sluzebniCestaRet != null)) {
            return i18nService.getString("MANAGER_VACATION.TRIP", locale, sluzebniCestaRet);
        }
        if ((dovolenaRet != null) && (sluzebniCestaRet == null)) {
            return i18nService.getString("MANAGER_VACATION.VACATION", locale, dovolenaRet);
        }
        return i18nService.getString("MANAGER_VACATION.VACATION_TRIP", locale, dovolenaRet, sluzebniCestaRet);
    }

    protected String formatDateSpan(DateFormat format, Locale locale, Date from, Date to) {
        if (from == null) {
            return null;
        }
        if ((to == null) || (from.equals(to))) {
            return i18nService.getString("MANAGER_VACATION.DATE_1", locale, format.format(from));
        }
        return i18nService.getString("MANAGER_VACATION.DATE_2", locale, format.format(from), format.format(to));
    }
}
