/**
 * ESUP-Portail Helpdesk - Copyright (c) 2004-2009 ESUP-Portail consortium.
 */
package org.esupportail.helpdesk.domain.beans;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.esupportail.commons.utils.BeanUtils;
import org.esupportail.helpdesk.web.beans.CategoryAttributeTypeFormatter;

/**
 * The class that represents ticket attributes.
 */
public class TicketAttribute implements Serializable {

    /**
     * The serialization id.
     */
    private static final long serialVersionUID = 7061904911180071468L;
    private static final String CATEGORY_ATTRIBUTE_TYPE_FORMATTER_NAME = "categoryAttributeTypeFormatter";
    private static final DateFormat UNIVERSAL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private final CategoryAttributeTypeFormatter categoryAttributeTypeFormatter = (CategoryAttributeTypeFormatter) BeanUtils.getBean(CATEGORY_ATTRIBUTE_TYPE_FORMATTER_NAME);

    /**
     * The ID.
     */
    private long id;

    /**
     * The ticket.
     */
    private Ticket ticket;

    /**
     * The order of the attribute.
     */
    private Integer order;

    /**
     * The name.
     */
    private String name;

    /**
     * The label.
     */
    private String label;

    /**
     * The value.
     */
    private String value;

    /**
     * Bean constructor.
     */
    public TicketAttribute() {
        super();
    }

    /**
     * Copy constructor.
     *
     * @param c the ticket attribute to copy
     */
    public TicketAttribute(final TicketAttribute c) {
        this.id = c.id;
        this.ticket = c.ticket;
        this.order = c.order;
        this.name = c.name;
        this.label = c.label;
        this.value = c.value;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TicketAttribute)) {
            return false;
        }
        return ((TicketAttribute) obj).getId() == getId();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return (int) getId();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "#" + hashCode() + "["
                + "id=[" + id + "]"
                + ", name=[" + name + "]"
                + ", label=[" + label + "]"
                + ", value=[" + value + "]"
                + "]";
    }

    /**
     * @return the ID
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the ID
     */
    public void setId(final long id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label
     */
    public void setLabel(final String label) {
        this.label = label;
    }

    /**
     * @return the ticket
     */
    public Ticket getTicket() {
        return ticket;
    }

    /**
     * @param ticket the ticket
     */
    public void setTicket(final Ticket ticket) {
        this.ticket = ticket;
    }

    /**
     * @return the order
     */
    public Integer getOrder() {
        return order;
    }

    /**
     * @param order the order
     */
    public void setOrder(Integer order) {
        this.order = order;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public String getDateValue() {
        DateFormat dateFormat = new SimpleDateFormat(categoryAttributeTypeFormatter.get("DATE_FORMAT"));
        if (value == null) {
            return null;
        }
        try {
            return dateFormat.format(UNIVERSAL_DATE_FORMAT.parse(value));
        } catch (ParseException ex) {
            return null;
        }
    }

    /**
     * @param value the value
     */
    public void setDateValue(String value) {
        DateFormat dateFormat = new SimpleDateFormat(categoryAttributeTypeFormatter.get("DATE_FORMAT"));
        try {
            this.value = UNIVERSAL_DATE_FORMAT.format(dateFormat.parse(value));
        } catch (ParseException ex) {
        }
    }

}
