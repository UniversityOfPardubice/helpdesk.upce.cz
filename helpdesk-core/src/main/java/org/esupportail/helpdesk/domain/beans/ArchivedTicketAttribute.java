/**
 * ESUP-Portail Helpdesk - Copyright (c) 2004-2009 ESUP-Portail consortium.
 */
package org.esupportail.helpdesk.domain.beans;

import java.io.Serializable;


/**
 * The class that represents archived ticket attributes.
 */
public class ArchivedTicketAttribute implements Serializable {
    
	/**
	 * The serialization id.
	 */
	private static final long serialVersionUID = 12008878157382272L;

	/**
     * The ID.
     */
    private long id;
    
    /**
     * The archived ticket.
     */
    private ArchivedTicket archivedTicket;
    
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
    public ArchivedTicketAttribute() {
    	super();
    }

    /**
     * Copy constructor.
     * @param c the archived ticket attribute to copy
     */
    public ArchivedTicketAttribute(final ArchivedTicketAttribute c) {
        this.id = c.id;
        this.archivedTicket = c.archivedTicket;
        this.order = c.order;
        this.name = c.name;
    	this.label = c.label;
        this.value = c.value;
    }

    /**
     * Create archived ticket attribute as a copy of ticket attribute 
     * @param ticketAttribute source ticket attribute
     * @param archivedTicket target archived ticket
     */
	public ArchivedTicketAttribute(
			TicketAttribute ticketAttribute,
			ArchivedTicket archivedTicket) {
		setArchivedTicket(archivedTicket);
		setOrder(ticketAttribute.getOrder());
                setName(ticketAttribute.getName());
		setLabel(ticketAttribute.getLabel());
		setValue(ticketAttribute.getValue());
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ArchivedTicketAttribute)) {
			return false;
		}
		return ((ArchivedTicketAttribute) obj).getId() == getId();
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
		return getClass().getSimpleName() + "#" + hashCode() + "[" +
				"id=[" + id + "]"+
                ", name=[" + name + "]" +
                ", label=[" + label + "]" +
                ", value=[" + value + "]" +
                "]";
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
     * @return the archived ticket
     */
    public ArchivedTicket getArchivedTicket() {
        return archivedTicket;
    }

    /**
     * @param archivedTicket the archived ticket
     */
    public void setArchivedTicket(final ArchivedTicket archivedTicket) {
        this.archivedTicket = archivedTicket;
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

}
