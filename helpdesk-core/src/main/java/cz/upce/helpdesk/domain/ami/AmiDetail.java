/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.upce.helpdesk.domain.ami;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.esupportail.helpdesk.domain.beans.Ticket;
import org.esupportail.helpdesk.domain.beans.TicketAttribute;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author lusl0338
 */
public class AmiDetail {

    private Document document;
    private Map<String, AmiDetailField> fieldsCache = null;
    private AmiConnector amiConnector;

    public AmiDetail(AmiConnector amiConnector, String detail) throws SAXException, IOException, ParserConfigurationException {
        this.amiConnector = amiConnector;
        parse(detail);
    }

    private void parse(String detail) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
        if ((detail == null) || ("".equals(detail))) {
            document = builder.newDocument();
            document.appendChild(document.createElement("DETAIL"));
        } else {
            InputStream is = new ByteArrayInputStream(detail.getBytes("UTF-8"));
            document = builder.parse(is);
        }
    }

    public String getXmlAsString() throws TransformerException {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        DOMSource source = new DOMSource(document);
        StringWriter writer = new StringWriter();

        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
        return writer.toString();
    }

    private Map<String, AmiDetailField> getFields() {
        if (fieldsCache == null) {
            NodeList fields = document.getElementsByTagName("FIELD");
            fieldsCache = new HashMap<String, AmiDetailField>();

            for (int i = 0; i < fields.getLength(); i++) {
                Element element = (Element) fields.item(i);
                AmiDetailField field = new AmiDetailField(amiConnector, element);
                fieldsCache.put(field.getName(), field);
            }
        }
        return fieldsCache;
    }

    public void mergeTicketAttributes(Ticket ticket) {
        List<TicketAttribute> ticketAttributes = ticket.getTicketAttributes();
        if (ticketAttributes != null) {
            for (TicketAttribute ticketAttribute : ticketAttributes) {
                setField(ticketAttribute);
            }
        }
    }

    public void setField(TicketAttribute attr) {
        setField(attr.getName(), attr.getLabel(), attr.getValue(), "String");
    }

    public void setField(String name, String caption, String value) {
        setField(name, caption, value, "String");
    }

    public void setField(String name, String caption, String value, String type) {
        Map<String, AmiDetailField> fields = getFields();
        if (fields.containsKey(name)) {
            AmiDetailField field = fields.get(name);
            field.setValue(value);
        } else {
            createField(name, caption, value, type);
        }
    }

    private void createField(String name, String caption, String value, String type) {
        Element field = document.createElement("FIELD");
        field.setAttribute("NAME", name);
        field.setAttribute("CAPTION", caption);
        field.setAttribute("VALUE", value);
        field.setAttribute("TYPE", type);
        document.getDocumentElement().appendChild(field);
        fieldsCache = null;
    }

    public String getFieldAsString(String name) {
        Map<String, AmiDetailField> fields = getFields();
        if (fields.containsKey(name)) {
            return fields.get(name).getValue();
        }
        return null;
    }

    public Long getFieldAsLong(String name) {
        String value = getFieldAsString(name);
        if (value != null) {
            return Long.parseLong(value);
        }
        return null;
    }
}
