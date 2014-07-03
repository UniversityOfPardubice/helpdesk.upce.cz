/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.upce.helpdesk.services.feed.imap.ami;

import java.util.Properties;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import org.esupportail.commons.utils.Assert;
import org.esupportail.helpdesk.services.feed.ErrorHolder;
import org.esupportail.helpdesk.services.feed.imap.AbstractImapAccountReader;

/**
 *
 * @author lusl0338
 */
public class TicketAccountReaderImplAmi extends AbstractImapAccountReader {

    private static final long serialVersionUID = 2238987092051570305L;
    private TicketMessageReaderImplAmi messageReader;

    public TicketMessageReaderImplAmi getMessageReader() {
        return messageReader;
    }

    public void setMessageReader(final TicketMessageReaderImplAmi messageReader) {
        this.messageReader = messageReader;
    }

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        Assert.notNull(messageReader,
                "property messageReader of class " + this.getClass().getName() + " can not be null");
    }

    public boolean readAll(final ErrorHolder errorHolder) {
        Boolean ret = Boolean.FALSE;
        errorHolder.addInfo("Synchronizing all tickets with AMI");
        ErrorHolder readErrorHolder = new ErrorHolder();
        if (messageReader.readAll(readErrorHolder)) {
            ret = Boolean.TRUE;
        } else {
            errorHolder.addInfo("error synchronizing (details follows)");
        }
        errorHolder.add(readErrorHolder);
        return ret;
    }

    public boolean syncMissingToAmi(final Long ticketId, final ErrorHolder errorHolder) {
        Boolean ret = Boolean.FALSE;
        errorHolder.addInfo("Synchronizing missing ticket #" + ticketId + " from TO to AMI");
        ErrorHolder readErrorHolder = new ErrorHolder();
        if (messageReader.syncMissingToAmi(ticketId, readErrorHolder)) {
            ret = Boolean.TRUE;
        } else {
            errorHolder.addInfo("error synchronizing (details follows)");
        }
        errorHolder.add(readErrorHolder);
        return ret;
    }

    public boolean syncMissingToAmi(final ErrorHolder errorHolder, boolean includeArchived) {
        Boolean ret = Boolean.FALSE;
        errorHolder.addInfo("Synchronizing missing tickets from TO to AMI");
        ErrorHolder readErrorHolder = new ErrorHolder();
        if (messageReader.syncMissingToAmi(readErrorHolder, includeArchived)) {
            ret = Boolean.TRUE;
        } else {
            errorHolder.addInfo("error synchronizing (details follows)");
        }
        errorHolder.add(readErrorHolder);
        return ret;
    }

    public boolean syncMissingCommunication(final ErrorHolder errorHolder) {
        Boolean ret = Boolean.FALSE;
        errorHolder.addInfo("Synchronizing missing communication from AMI");
        ErrorHolder readErrorHolder = new ErrorHolder();
        if (messageReader.syncMissingCommunication(readErrorHolder)) {
            ret = Boolean.TRUE;
        } else {
            errorHolder.addInfo("error synchronizing (details follows)");
        }
        errorHolder.add(readErrorHolder);
        return ret;
    }

    public boolean read(final ErrorHolder errorHolder) {
        Store store = null;
        Boolean ret = Boolean.FALSE;
        if (!errorHolder.hasErrors()) {
            try {
                errorHolder.addInfo("Server: " + getServer());
                errorHolder.addInfo("Folder: " + getFolder());
                errorHolder.addInfo("Account: " + getAccount());
                errorHolder.addInfo("Password: " + "xxxxxxxxx");
                Integer timeoutVal = new Integer(getTimeout());
                Properties props = System.getProperties();
                props.put("mail.imap.class", "com.sun.mail.imap.IMAPStore");
                props.put("mail.imap.connectiontimeout", timeoutVal);
                props.put("mail.imap.timeout", timeoutVal);
                Session session = Session.getInstance(props, null);
                URLName urln = new URLName(
                        "imap://" + getAccount() + ":"
                        + getPassword() + "@" + getServer());
                store = session.getStore(urln);
            } catch (NoSuchProviderException e) {
                errorHolder.addError(
                        "invalid IMAP account [imap://" + getAccount()
                        + ":xxxxxxxx@" + getServer() + "]: " + e.getMessage());
            }
        }
        if (!errorHolder.hasErrors()) {
            try {
                errorHolder.addInfo("connecting to the server...");
                store.connect();
            } catch (MessagingException e) {
                errorHolder.addError(
                        "could not connect to [imap://" + getAccount()
                        + ":xxxxxxxx@" + getServer() + "]: " + e.getMessage());
            }
        }
        Folder folder = null;
        if (!errorHolder.hasErrors()) {
            try {
                errorHolder.addInfo("opening folder [" + getFolder() + "]...");
                folder = store.getFolder(getFolder());
                if (!folder.exists()) {
                    String msg = "folder does not exist. Available folders are: ";
                    Folder defaultFolder = store.getDefaultFolder();
                    String separator = "";
                    for (Folder folder2 : defaultFolder.list()) {
                        msg += separator + folder2.getFullName();
                        separator = ", ";
                    }
                    errorHolder.addError(msg + ".");
                }
            } catch (MessagingException e) {
                errorHolder.addError("could not find the folder: " + e.getMessage());
            }
        }
        if (!errorHolder.hasErrors()) {
            try {
                folder.open(Folder.READ_WRITE);
            } catch (MessagingException e) {
                errorHolder.addError("could not open the folder: " + e.getMessage());
            }
        }
        if (!errorHolder.hasErrors()) {
            try {
                errorHolder.addInfo("expunging the folder...");
                folder.expunge();
            } catch (MessagingException e) {
                errorHolder.addError("could not expunge the folder: " + e.getMessage());
            }
        }
        int messageCount = 0;
        if (!errorHolder.hasErrors()) {
            try {
                messageCount = folder.getMessageCount();
                if (messageCount == 0) {
                    errorHolder.addInfo("no message found.");
                } else {
                    errorHolder.addInfo(messageCount + " message(s) found.");
                }
            } catch (MessagingException e) {
                errorHolder.addError("could not get the number of messages: " + e.getMessage());
            }
        }
        if (!errorHolder.hasErrors()) {
            for (int i = 1; i <= messageCount; i++) {
                errorHolder.addInfo("getting message #" + i + "...");
                Message message;
                try {
                    message = folder.getMessage(i);
                } catch (MessagingException e) {
                    errorHolder.addError(
                            "could not get message #" + i + ": " + e.getMessage());
                    break;
                }
                try {
                    if (message.isSet(Flags.Flag.DELETED)) {
                        errorHolder.addInfo("message is marked as deleted, skiping.");
                        continue;
                    }
                } catch (MessagingException e) {
                    errorHolder.addError(
                            "could not get flag for message #" + i + ": " + e.getMessage());
                    break;
                }
                ErrorHolder readErrorHolder = new ErrorHolder();
                if (messageReader.read(message, readErrorHolder)) {
                    ret = Boolean.TRUE;
                    try {
                        message.setFlag(Flags.Flag.DELETED, true);
                    } catch (MessagingException e) {
                        errorHolder.addInfo("could not mark the message as deleted: " + e.getMessage());
                    }
                } else {
                    errorHolder.addInfo("error processing email (details follows)");
                }
                errorHolder.add(readErrorHolder);
            }
        }
        if (folder != null && folder.isOpen()) {
            try {
                errorHolder.addInfo("closing folder...");
                folder.close(true);//expunge on exit
            } catch (MessagingException e) {
                errorHolder.addError("could not close the folder: " + e.getMessage());
            }
        }
        if (store != null && store.isConnected()) {
            try {
                errorHolder.addInfo("closing connection...");
                store.close();
            } catch (MessagingException e) {
                errorHolder.addError("could not close the connection: " + e.getMessage());
            }
        }
        if (errorHolder.hasErrors()) {
            errorHolder.addInfo(
                    errorHolder.getErrorNumber() + " error(s) found for account [imap://"
                    + getAccount() + ":xxxxxxxx@" + getServer() + "]");
        } else {
            errorHolder.addInfo(
                    "no error found for account [imap://"
                    + getAccount() + ":xxxxxxxx@" + getServer() + "]");
        }
        return ret;
    }
}
