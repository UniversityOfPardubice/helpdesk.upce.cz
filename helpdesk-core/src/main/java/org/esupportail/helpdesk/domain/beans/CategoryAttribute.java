/**
 * ESUP-Portail Helpdesk - Copyright (c) 2004-2009 ESUP-Portail consortium.
 */
package org.esupportail.helpdesk.domain.beans;

import java.io.Serializable;

import org.esupportail.commons.utils.strings.StringUtils;


/**
 * The class that represents categories.
 */
public class CategoryAttribute implements Serializable {

	/** category attribute - text input. */
	public static final String TEXT = "TEXT";
	/** category attribute - select from list. */
	public static final String SELECT = "SELECT";
	/** category attribute - DB query. */
	public static final String DB = "DB";
        /** category attribute - Date input. */
        public static final String DATE = "DATE";
        /** category attribute - label. */
        public static final String LABEL = "LABEL";
    
	/**
	 * The serialization id.
	 */
//    private static final long serialVersionUID = 4258345851003073808L;
    
    /**
     * The ID of category attribute.
     */
    private long id;
    
    /**
     * The category.
     */
    private Category category;
    
    /**
     * The order number of the attribute.
     */
    private Integer order;

    /**
     * The name of the attribute.
     */
    private String name;
    
    /**
     * The label of the attribute.
     */
    private String label;
    
    /**
     * The type of the attribute.
     */
    private String type;
    
    /**
     * List of values for the attribute.
     */
    private String values;
    
    /**
     * JNDI context for the DB connection.
     */
    private String dbConnectionContext;
    
    /**
     * JNDI source for the DB connection.
     */
    private String dbConnectionJndi;
    
    /**
     * SQL query.
     */
    private String dbConnectionSql;

	/**
     * Bean constructor.
     */
    public CategoryAttribute() {
    	super();
        this.dbConnectionContext = "java:comp/env";
    }

    /**
     * Copy constructor.
     * @param c category to copy
     */
    public CategoryAttribute(final CategoryAttribute c) {
        this.id = c.id;
        this.category = c.category;
        this.order = c.order;
        this.name = c.name;
    	this.label = c.label;
        this.type = c.type;
        this.values = c.values;
        this.dbConnectionContext = c.dbConnectionContext;
        this.dbConnectionJndi = c.dbConnectionJndi;
        this.dbConnectionSql = c.dbConnectionSql;
    }

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CategoryAttribute)) {
			return false;
		}
		return ((CategoryAttribute) obj).getId() == getId();
	}

	/**
	 * @see java.lang.Object#hashCode() 
	 */
	@Override
	public int hashCode() {
		return (int) getId();
	}

	/**
	 * @return the object converted to string.
	 */
	@Override
    public String toString() {
    	return getClass().getSimpleName() + "#" + hashCode() + "[" +
    			"id=[" + id + "]" +
                ", name=[" + name + "]" +
                ", label=[" + label + "]" +
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
     * @return the category
     */
    public Category getCategory() {
        return category;
    }

    /**
     * @param category the category
     */
    public void setCategory(final Category category) {
        this.category = category;
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
     * @return the JNDI context
     */
    public String getDbConnectionContext() {
        return dbConnectionContext;
    }

    /**
     * Sets the JNDI connection context - on null or "" parameter sets default
     * "java:comp/env".
     * @param dbConnectionContext the JNDI context
     */
    public void setDbConnectionContext(String dbConnectionContext) {
        if ((dbConnectionContext == null) || ("".equals(dbConnectionContext))) {
            this.dbConnectionContext = "java:comp/env";
        } else {
            this.dbConnectionContext = dbConnectionContext;
        }
    }

    /**
     * @return the JNDI source
     */
    public String getDbConnectionJndi() {
        return dbConnectionJndi;
    }

    /**
     * @param dbConnectionJndi the JNDI source
     */
    public void setDbConnectionJndi(String dbConnectionJndi) {
        this.dbConnectionJndi = dbConnectionJndi;
    }

    /**
     * @return the SQL query
     */
    public String getDbConnectionSql() {
        return dbConnectionSql;
    }

    /**
     * @param dbConnectionSql the SQL query
     */
    public void setDbConnectionSql(String dbConnectionSql) {
        this.dbConnectionSql = dbConnectionSql;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the values
     */
    public String getValues() {
        return values;
    }

    /**
     * @param values the values
     */
    public void setValues(String values) {
        this.values = StringUtils.nullIfEmpty(values);
    }
}
