/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.upce.helpdesk.batch;

import cz.upce.helpdesk.services.feed.imap.ami.TicketAccountReaderImplAmi;
import org.esupportail.commons.batch.BatchException;
import org.esupportail.commons.services.application.ApplicationService;
import org.esupportail.commons.services.application.ApplicationUtils;
import org.esupportail.commons.services.application.VersionningUtils;
import org.esupportail.commons.services.database.DatabaseUtils;
import org.esupportail.commons.services.exceptionHandling.ExceptionUtils;
import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.esupportail.commons.utils.BeanUtils;
import org.esupportail.helpdesk.batch.Batch;
import org.esupportail.helpdesk.services.feed.ErrorHolder;

/**
 *
 * @author lusl0338
 */
public class BatchAmi {

    /**
     * The name of the feeder bean.
     */
    private static final String AMI_READER_BEAN = "amiAccountReader";
    /**
     * A logger.
     */
    private static final Logger LOG = new LoggerImpl(BatchAmi.class);

    /**
     * Bean constructor.
     */
    private BatchAmi() {
        throw new UnsupportedOperationException();
    }

    private static void amiFeedAll() {
        try {
            boolean versionChecked = false;
            TicketAccountReaderImplAmi reader = (TicketAccountReaderImplAmi) BeanUtils.getBean(AMI_READER_BEAN);
            ErrorHolder errorHolder = new ErrorHolder();
            DatabaseUtils.open();
            DatabaseUtils.begin();
            if (!versionChecked) {
                VersionningUtils.checkVersion(true, false);
                versionChecked = true;
            }
            boolean commit = reader.readAll(errorHolder);
            if (commit) {
                DatabaseUtils.commit();
            } else {
                DatabaseUtils.rollback();
            }
            DatabaseUtils.close();
            if (errorHolder.hasErrors()) {
                errorHolder.addInfo(errorHolder.getErrorNumber() + " total error(s) found");
                LOG.error(errorHolder.getStrings());
            } else {
                errorHolder.addInfo("no error found");
                LOG.info(errorHolder.getStrings());
            }
        } catch (Throwable t) {
            DatabaseUtils.rollback();
            DatabaseUtils.close();
            throw new BatchException(t);
        }
    }

    private static void syncMissingToAmi(Long ticketNumber) {
        try {
            TicketAccountReaderImplAmi reader = (TicketAccountReaderImplAmi) BeanUtils.getBean(AMI_READER_BEAN);
            ErrorHolder errorHolder = new ErrorHolder();
            DatabaseUtils.open();
            DatabaseUtils.begin();
            boolean commit = reader.syncMissingToAmi(ticketNumber, errorHolder);
            if (commit) {
                DatabaseUtils.commit();
            } else {
                DatabaseUtils.rollback();
            }
            DatabaseUtils.close();
            if (errorHolder.hasErrors()) {
                errorHolder.addInfo(errorHolder.getErrorNumber() + " total error(s) found");
                LOG.error(errorHolder.getStrings());
            } else {
                errorHolder.addInfo("no error found");
                LOG.info(errorHolder.getStrings());
            }
        } catch (Throwable t) {
            DatabaseUtils.rollback();
            DatabaseUtils.close();
            throw new BatchException(t);
        }
    }

    private static void syncMissingToAmi(boolean includeArchived) {
        try {
            TicketAccountReaderImplAmi reader = (TicketAccountReaderImplAmi) BeanUtils.getBean(AMI_READER_BEAN);
            ErrorHolder errorHolder = new ErrorHolder();
            DatabaseUtils.open();
            DatabaseUtils.begin();
            boolean commit = reader.syncMissingToAmi(errorHolder, includeArchived);
            if (commit) {
                DatabaseUtils.commit();
            } else {
                DatabaseUtils.rollback();
            }
            DatabaseUtils.close();
            if (errorHolder.hasErrors()) {
                errorHolder.addInfo(errorHolder.getErrorNumber() + " total error(s) found");
                LOG.error(errorHolder.getStrings());
            } else {
                errorHolder.addInfo("no error found");
                LOG.info(errorHolder.getStrings());
            }
        } catch (Throwable t) {
            DatabaseUtils.rollback();
            DatabaseUtils.close();
            throw new BatchException(t);
        }
    }

    private static void syncMissingCommunication() {
        try {
            TicketAccountReaderImplAmi reader = (TicketAccountReaderImplAmi) BeanUtils.getBean(AMI_READER_BEAN);
            ErrorHolder errorHolder = new ErrorHolder();
            DatabaseUtils.open();
            DatabaseUtils.begin();
            boolean commit = reader.syncMissingCommunication(errorHolder);
            if (commit) {
                DatabaseUtils.commit();
            } else {
                DatabaseUtils.rollback();
            }
            DatabaseUtils.close();
            if (errorHolder.hasErrors()) {
                errorHolder.addInfo(errorHolder.getErrorNumber() + " total error(s) found");
                LOG.error(errorHolder.getStrings());
            } else {
                errorHolder.addInfo("no error found");
                LOG.info(errorHolder.getStrings());
            }
        } catch (Throwable t) {
            DatabaseUtils.rollback();
            DatabaseUtils.close();
            throw new BatchException(t);
        }
    }

    /**
     * Dispatch depending on the arguments.
     *
     * @param args
     */
    protected static void dispatch(final String[] args) {
        switch (args.length) {
            case 1:
                if ("ami-fetchall".equals(args[0])) {
                    ApplicationService applicationService = ApplicationUtils.createApplicationService();
                    LOG.info(applicationService.getName() + " v" + applicationService.getVersion());
                    amiFeedAll();
                    return;
                }
                if ("ami-syncmissing-all".equals(args[0])) {
                    ApplicationService applicationService = ApplicationUtils.createApplicationService();
                    LOG.info(applicationService.getName() + " v" + applicationService.getVersion());
                    syncMissingToAmi(true);
                    return;
                }
                if ("ami-syncmissing".equals(args[0])) {
                    ApplicationService applicationService = ApplicationUtils.createApplicationService();
                    LOG.info(applicationService.getName() + " v" + applicationService.getVersion());
                    syncMissingToAmi(false);
                    return;
                }
                if ("ami-synccommunication".equals(args[0])) {
                    ApplicationService applicationService = ApplicationUtils.createApplicationService();
                    LOG.info(applicationService.getName() + " v" + applicationService.getVersion());
                    syncMissingCommunication();
                    return;
                }
                break;
        }
        Batch.dispatch(args);
    }

    /**
     * The main method, called by ant.
     *
     * @param args
     */
    public static void main(final String[] args) {
        try {
            dispatch(args);
        } catch (Throwable t) {
            ExceptionUtils.catchException(t);
        }
    }
}
