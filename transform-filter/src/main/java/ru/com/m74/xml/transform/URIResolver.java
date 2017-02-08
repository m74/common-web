package ru.com.m74.xml.transform;

import javax.servlet.ServletContext;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Maxim
 */
public class URIResolver implements javax.xml.transform.URIResolver {

    private ServletContext ctx;

    public URIResolver(ServletContext ctx) {
        this.ctx = ctx;
//        Logger.debug(this, "setup context: " + ctx);
    }

    @Override
    public Source resolve(String href, String pageBase) throws TransformerException {
//        Logger.debug(this, "base: " + pageBase);

        String systemId = href;
//        Logger.debug(this, "href: " + systemId);


        if (href.charAt(0) != '/' && pageBase != null) {

            // Отбрасываем файл и получаем текущую директорию
            if (pageBase.charAt(pageBase.length() - 1) != '/')
                pageBase = pageBase.substring(0, pageBase.lastIndexOf('/') + 1);

            systemId = pageBase + href;
        }

//        Logger.debug(this, "systemId: " + systemId);

        // Преобразовать отностельный путь в абсолютный
        if (systemId.charAt(0) == '/') {
            File f = new File(ctx.getRealPath(systemId));
            if (f.exists())
                systemId = "file:" + f.getAbsolutePath();
            else {
                try {
                    URL url = ctx.getResource(systemId);
                    if (url == null) throw new TransformerException("Resource not found: " + systemId);
                    systemId = url.toString();
                } catch (MalformedURLException e) {
                    throw new TransformerException(e);
                }
            }
        }


//        Logger.debug(this, "resolve: " + systemId);

        return new StreamSource(systemId);
    }
}
