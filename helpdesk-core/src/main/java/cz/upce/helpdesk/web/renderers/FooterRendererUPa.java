/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.upce.helpdesk.web.renderers;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.apache.myfaces.shared_impl.renderkit.html.HTML;
import org.apache.myfaces.shared_impl.renderkit.html.HtmlTextRendererBase;
import org.esupportail.commons.web.renderers.FooterRenderer;
import org.esupportail.commons.web.tags.config.TagsConfigurator;

/**
 *
 * @author lusl0338
 */
public class FooterRendererUPa extends FooterRenderer {

    public FooterRendererUPa() {
        super();
    }

    private static void encodeFooterBegin(
            final FacesContext facesContext,
            final UIComponent uiComponent)
            throws IOException {
        TagsConfigurator tagsConfigurator = TagsConfigurator.getInstance();
        ResponseWriter writer = facesContext.getResponseWriter();
        writer.startElement(HTML.DIV_ELEM, uiComponent);
        writer.writeAttribute(HTML.CLASS_ATTR, tagsConfigurator.getFooterStyleClass(), null);
    }

    private static void encodeFooterEnd(
            final FacesContext facesContext)
            throws IOException {
        ResponseWriter writer = facesContext.getResponseWriter();
        writer.endElement(HTML.DIV_ELEM);
    }

	public static void encodeFooter(
			final FacesContext facesContext,
			final UIComponent uiComponent,
			final String value)
	throws IOException {
			encodeFooterBegin(facesContext, uiComponent);
			HtmlTextRendererBase.renderOutputText(facesContext, uiComponent, value, false);
			encodeFooterEnd(facesContext);
	}
}
