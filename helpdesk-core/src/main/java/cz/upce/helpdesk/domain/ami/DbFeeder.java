package cz.upce.helpdesk.domain.ami;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.esupportail.commons.utils.Assert;
import org.esupportail.helpdesk.domain.DomainService;
import org.esupportail.helpdesk.services.feed.ErrorHolder;
import org.springframework.beans.factory.InitializingBean;

/**
 *
 * @author lusl0338
 */
public class DbFeeder implements InitializingBean {

    private static final Logger logger = new LoggerImpl(AmiConnector.class);
    private AmiConnector amiConnector;
    private DomainService domainService;

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

    public boolean feedFromDb(final ErrorHolder errorHolder) throws Exception {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            try {
                connection = amiConnector.getAmiConnection();
                connection.setAutoCommit(false);
                stmt = connection.prepareStatement("select distinct POZADAVEK_ID from UPA_HELPDESK_Q");
                rs = stmt.executeQuery();
                if (rs.next()) {
                    Long amiId = rs.getLong("POZADAVEK_ID");
                    if (amiConnector.synchronizeTicket(domainService, errorHolder, amiId)) {
                        PreparedStatement stmtDelete = connection.prepareStatement("delete from UPA_HELPDESK_Q where POZADAVEK_ID=?");
                        stmtDelete.setLong(1, amiId);
                        stmtDelete.executeUpdate();
                    }
                }
            } catch (Exception ex) {
                if (connection != null) {
                    connection.rollback();
                }
                logger.error(ex);
                errorHolder.addError("Error synchronizing ticket: " + ex.getMessage());
                return false;
            } finally {
                AmiConnector.closeConnection(stmt, rs);
            }
            if (connection != null) {
                connection.commit();
            }
        } finally {
            AmiConnector.closeConnection(connection);
        }
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.amiConnector,
                "property amiConnector of class " + this.getClass().getName() + " can not be null");
        Assert.notNull(this.domainService,
                "property domainService of class " + this.getClass().getName() + " can not be null");
    }

}
