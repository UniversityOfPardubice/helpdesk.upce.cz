/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.upce.helpdesk.domain.ami.exceptions;

/**
 *
 * @author lusl0338
 */
public class AmiUserNotFoundException extends Exception {
    private static final long serialVersionUID = 3305786801988405786L;

    public AmiUserNotFoundException(String username) {
        super("AMI user " + username + " nebyl nalezen");
    }

}
