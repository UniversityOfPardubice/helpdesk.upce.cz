/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.upce.helpdesk.domain.ami.exceptions;

public class AmiTemplateNotFoundException extends Exception {

    private static final long serialVersionUID = 3768344299606201111L;

    public AmiTemplateNotFoundException(Long id) {
        super("AMI sablona id=" + id + " nebyla nalezena");
    }
}

