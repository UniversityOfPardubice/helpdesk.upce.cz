/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.upce.helpdesk.web.tags;

import cz.upce.helpdesk.web.renderers.FooterRendererUPa;
import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import org.apache.myfaces.shared_impl.renderkit.html.HTML;
import org.esupportail.commons.web.tags.config.TagsConfigurator;
import org.esupportail.commons.web.tags.PageTag;

/**
 *
 * @author lusl0338
 */
public class PageTagUPa extends PageTag {

    @Override
    protected void addFooter(FacesContext facesContext, UIComponent uiComponent) throws IOException {
        if (getFooter() != null) {
            FooterRendererUPa.encodeFooter(facesContext, uiComponent, getFooter());
        } else {
            FooterRendererUPa.encodeFooter(
                    facesContext, getComponentInstance(),
                    TagsConfigurator.getInstance().getFooterText());
        }
    }

    @Override
    protected void addBodyStyleInformation(TagsConfigurator tagsConfigurator, ResponseWriter responseWriter)
            throws IOException {
        super.addBodyStyleInformation(tagsConfigurator, responseWriter);
        responseWriter.startElement("script", null);
        responseWriter.writeAttribute(HTML.TYPE_ATTR, "text/javascript", null);
        responseWriter.write("\n"
                + "var _gaq = _gaq || [];\n"
                + "  _gaq.push(['_setAccount', 'UA-1621542-8']);\n"
                + "  _gaq.push(['_trackPageview']);\n"
                + "  (function() {\n"
                + "    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;\n"
                + "    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';\n"
                + "    (document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(ga);\n"
                + "  })();\n");
        responseWriter.endElement("script");
    }
}
