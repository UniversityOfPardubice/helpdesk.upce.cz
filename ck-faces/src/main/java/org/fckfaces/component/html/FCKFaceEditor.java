package org.fckfaces.component.html;

import java.io.IOException;

import javax.faces.component.html.HtmlInputTextarea;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.commons.lang.StringUtils;
import org.fckfaces.taglib.html.FCKFaceEditorTag;
import org.fckfaces.util.Util;

/**
 * 
 * @author srecinto
 *
 */
public class FCKFaceEditor extends HtmlInputTextarea {
	public static final String CUSTOM_CONFIGURATION_PATH = "org.fckfaces.CUSTOM_CONFIGURATIONS_PATH";
	public static final String COMPONENT_FAMILY = "org.fckfaces.FCKFacesFamily";
	private String toolbarSet;
	private String height;
	private String width;

	/**
	 * 
	 */
	public String getComponentType() { 
		return FCKFaceEditorTag.COMPONENT_TYPE; 
	}
	
	/**
	 * 
	 */
	public String getRendererType() { 
		return FCKFaceEditorTag.RENDERER_TYPE;
	} 
	
	/**
	 * Moved to encode end so that the inline java script will run after the textArea was rendered before this script is run
	 * @param context
	 * @throws IOException
	 */
	public void encodeEnd(FacesContext context) throws IOException {
		super.encodeEnd(context);
		
		ResponseWriter writer = context.getResponseWriter();
		
		//Initial Configuration
		final ExternalContext external = context.getExternalContext();
		String cstConfigPathParam = external.getInitParameter(CUSTOM_CONFIGURATION_PATH);
		
		//Initial JS link
		writer.startElement("script", this.getParent());
		writer.writeAttribute("type", "text/javascript", null);
		writer.writeAttribute("src", "//www.upce.cz/libs/ckeditor/ckeditor.js", null);
		writer.endElement("script");
		
		writer.startElement("script", this.getParent());
		
		String toolBar = "Default";
		if(StringUtils.isNotBlank(toolbarSet)) {
			toolBar = toolbarSet;
		}
		
		String heightJS = "";
		String widthJS = "";
		String configPathJS = "";
		
		if(StringUtils.isNotBlank(height)) {
			heightJS = "height: '" + height + "',\r\n";
		}
		
		if(StringUtils.isNotBlank(width)) {
			 widthJS = "width: '" + width + "',\r\n";
		}
		
		if (StringUtils.isNotBlank(cstConfigPathParam) ) {
			cstConfigPathParam = Util.externalPath(cstConfigPathParam);
			configPathJS = "customConfig: '" + cstConfigPathParam + "',\r\n";
		}

                String js = "function applyEditor" + this.getId() + "() { \n" +
                    " var config = {" +
                    heightJS +
                    widthJS +
                    configPathJS +
                    " toolbar = '" + toolBar + "'\r\n" +
                    " };" +
                    " var oCKEditor = CKEDITOR.replace('" + this.getClientId(context) + "', config);" +
                    //??? " var sBasePath = '" + Util.internalPath("/FCKeditor/") + "';\r\n" +
                    "}\r\n"+
                "applyEditor" + this.getId() +"();";
		
		writer.writeText(js, null);
		writer.endElement("script");
	}
	
	/**
	 * 
	 * @return
	 */
	public String getFamily() {
		return COMPONENT_FAMILY;
	}
	
	public Object saveState(FacesContext context) {
		Object values[] = new Object[2];
		values[0] = super.saveState(context);
		values[1] = toolbarSet;
		
		return values;
	}
	
	public void restoreState(FacesContext context, Object state) {
		Object values[] = (Object[]) state;
		super.restoreState(context, values[0]);
		this.toolbarSet = (String)values[1];
	}

	public String getToolbarSet() {
		return toolbarSet;
	}

	public void setToolbarSet(String toolbarSet) {
		this.toolbarSet = toolbarSet;
	}
	
	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}
}
