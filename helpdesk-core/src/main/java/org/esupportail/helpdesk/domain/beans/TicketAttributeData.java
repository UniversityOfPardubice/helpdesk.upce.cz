 /**
 * ESUP-Portail Helpdesk - Copyright (c) 2004-2009 ESUP-Portail consortium.
 */
package org.esupportail.helpdesk.domain.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.esupportail.commons.services.logging.Logger;
import org.esupportail.commons.services.logging.LoggerImpl;

/**
 * A class to store ticket attribute for displaying.
 */
public class TicketAttributeData extends TicketAttribute {
	/**
	 * The serialization id.
	 */
	private static final long serialVersionUID = 7462809945180303544L;

	/**
	 * The logger.
	 */
	private static final Logger LOG = new LoggerImpl(TicketAttributeData.class);

	/**
	 * The type of ticket attribute based on according category attribute.
	 */
	private String type;

	/**
	 * Possible values for attribute.
	 */
	private List<SelectItem> values;

	/**
	 * Default constructor.
	 */
	public TicketAttributeData() {
		super();
	}

	/**
	 * Copy constructor to copy from category attribute.
	 * 
	 * @param categoryAttribute source category attribute
	 */
	TicketAttributeData(CategoryAttribute categoryAttribute) {
                this.setName(categoryAttribute.getName());
		this.setLabel(categoryAttribute.getLabel());
		this.setOrder(categoryAttribute.getOrder());
		this.setType(categoryAttribute.getType());

		if (CategoryAttribute.DB.equals(categoryAttribute.getType())) {
			this.setValuesDb(categoryAttribute);
		}
		if (CategoryAttribute.SELECT.equals(categoryAttribute.getType())) {
			this.setValuesSelect(categoryAttribute);
		}
	}

	/**
	 * Copy constructor.
	 * 
	 * @param ticketAttribute source ticket attribute
	 */
	public TicketAttributeData(TicketAttribute ticketAttribute) {
		this.setId(ticketAttribute.getId());
                this.setName(ticketAttribute.getName());
		this.setLabel(ticketAttribute.getLabel());
		this.setOrder(ticketAttribute.getOrder());
		this.setTicket(ticketAttribute.getTicket());
		this.setValue(ticketAttribute.getValue());
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the value
	 */
	public List<SelectItem> getValues() {
		return values;
	}

	/**
	 * Set possible attributes according to DB query. On exception returns empty
	 * list.
	 * 
	 * @param categoryAttribute the category attribute with the JNDI and SQL
	 * details
	 */
	private void setValuesDb(CategoryAttribute categoryAttribute) {
		values = new ArrayList<SelectItem>();
		Connection connection = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			Context ic = new InitialContext();
			Context context = (Context) ic.lookup(categoryAttribute
					.getDbConnectionContext());
			DataSource dataSource = (DataSource) context
					.lookup(categoryAttribute.getDbConnectionJndi());
			connection = dataSource.getConnection();
			stmt = connection.prepareStatement(categoryAttribute
					.getDbConnectionSql());
			rs = stmt.executeQuery();
			while (rs.next()) {
				SelectItem value = new SelectItem(rs.getString(1), rs
						.getString(1));
				values.add(value);
			}
		} catch (Exception ex) {
			LOG.error(ex);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException ex) {
					// nothing to close
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException ex) {
					// nothing to close
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ex) {
					// nothing to close
				}
			}
		}
	}

	/**
	 * Set possible attributes according to comma-separated values list
	 * 
	 * @param categoryAttribute the category attribute with the list
	 */
	private void setValuesSelect(CategoryAttribute categoryAttribute) {
		String[] attributeValues = categoryAttribute.getValues().split(",");
		values = new ArrayList<SelectItem>(attributeValues.length);
		for (String attributeValue : attributeValues) {
			SelectItem value = new SelectItem(attributeValue.trim(),
					attributeValue.trim());
			values.add(value);
		}
	}
}
