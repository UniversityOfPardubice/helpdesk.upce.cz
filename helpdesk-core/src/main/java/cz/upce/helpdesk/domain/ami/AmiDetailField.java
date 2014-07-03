/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.upce.helpdesk.domain.ami;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 *
 * @author lusl0338
 */
public class AmiDetailField {

    private static final Logger logger = new LoggerImpl(AmiDetailField.class);
    private String caption;
    private String name;
    private Attr nodeValue;
    private Attr nodeOptionText;
    private AmiType type;
    private Element fieldElement;
    private Element optionsElement;
    private AmiConnector amiConnector;

    private enum AmiType {

        OPTIONS, OPTIONSSQL, OPTIONSVALUES, STRING, NUMBER, DATE
    }

    public AmiDetailField(AmiConnector amiConnector, Element fieldElement) {
        this.fieldElement = fieldElement;
        this.amiConnector = amiConnector;
        NamedNodeMap attrs = fieldElement.getAttributes();
        Attr nodeName = (Attr) attrs.getNamedItem("NAME");
        nodeValue = (Attr) attrs.getNamedItem("VALUE");
        Attr nodeType = (Attr) attrs.getNamedItem("TYPE");
        if (nodeValue == null) {
            nodeValue = fieldElement.getOwnerDocument().createAttribute("VALUE");
            attrs.setNamedItem(nodeValue);
        }
        name = nodeName.getNodeValue();
        type = AmiType.valueOf(nodeType.getValue().toUpperCase());

        if (AmiType.OPTIONS.equals(type)) {
            NodeList optionsNodes = fieldElement.getElementsByTagName("OPTIONS");
            optionsElement = (Element) optionsNodes.item(0);
            String optionsType = optionsElement.getAttribute("TYPE").toUpperCase();
            if ("SQL".equals(optionsType)) {
                type = AmiType.OPTIONSSQL;
            }
            if ("VALUES".equals(optionsType)) {
                type = AmiType.OPTIONSVALUES;
            }
            if (AmiType.OPTIONS.equals(type)) {
                throw new IllegalArgumentException("Options element is not Sql or Values typed");
            }
            nodeOptionText = (Attr) attrs.getNamedItem("OPTION_TEXT");
            if (nodeOptionText == null) {
                nodeOptionText = fieldElement.getOwnerDocument().createAttribute("OPTION_TEXT");
                attrs.setNamedItem(nodeOptionText);
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setValue(String value) {
        switch (type) {
            case STRING:
            case NUMBER:
            case DATE:
                nodeValue.setNodeValue(value);
                break;
            case OPTIONSVALUES:
                nodeValue.setNodeValue(value);
                nodeOptionText.setNodeValue(value);
                break;
            case OPTIONSSQL:
                nodeValue.setNodeValue(reverseMapOptionsField(value));
                nodeOptionText.setNodeValue(value);
                break;
        }
    }

    public String getValue() {
        switch (type) {
            case STRING:
            case NUMBER:
            case DATE:
            case OPTIONSVALUES:
                return nodeValue.getValue();
            case OPTIONSSQL:
                return mapOptionsField(nodeValue.getValue());
        }
        return null;
    }
    
    private String reverseMapOptionsField(String value) {
        return mapField(optionsElement.getAttribute("TEXTFIELD"),
                optionsElement.getAttribute("VALUEFIELD"),
                value);
    }

    private String mapOptionsField(String id) {
        return mapField(optionsElement.getAttribute("VALUEFIELD"),
                optionsElement.getAttribute("TEXTFIELD"),
                id);
    }

    private String mapField(String from, String to, String value) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            connection = amiConnector.getAmiConnection();
            String query = optionsElement.getTextContent() + " WHERE " + from + "=?";
            stmt = connection.prepareStatement(query);
            stmt.setString(1, value);
            result = stmt.executeQuery();
            if (!result.next()) {
                return value;
            }
            return result.getString(to);
        } catch (Exception ex) {
            logger.error("Error mapping field (from=" + from + ", to=" + to + ", value=" + value + ")", ex);
            return value;
        } finally {
            AmiConnector.closeConnection(connection, stmt, result);
        }
    }
}
