package mixam.dom4web;

import java.io.File;

import javax.servlet.ServletContext;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import mixam.webtools.Logger;

/**
 * 
 * @author Maxim
 */
public class URIResolver implements javax.xml.transform.URIResolver {

	private ServletContext ctx;

	// private String defaultBase;

	public URIResolver(ServletContext ctx) {
		this.ctx = ctx;
		// baseDir = ctx.getInitParameter("base");

		Logger.debug(this, "setup context: " + ctx);
	}

	@Override
	public Source resolve(String href, String pageBase) throws TransformerException {
		Logger.debug(this, "base: " + pageBase);

		String systemId = href;
		Logger.debug(this, "href: " + systemId);

		if (pageBase != null) {

			if (href.charAt(0) != '/') {

				// Отбрасываем файл и получаем текущую директорию
				if (pageBase.charAt(pageBase.length() - 1) != '/')
					pageBase = pageBase.substring(0, pageBase.lastIndexOf('/') + 1);

				systemId = pageBase + href;
			}

			Logger.debug(this, "systemId: " + systemId);

			// Преобразовать отностельный путь в абсолютный
			if (systemId.charAt(0) == '/') {
				// Убрать contextPath из systemId, иначе getRealPath() сработает
				// не корректно.
				// !!! Непонятно зачем убирать, может изначально указывать путь
				// с учетом контекста
				// systemId = systemId.replaceFirst(
				// "/" + ctx.getServletContextName(), "");

				File f = new File(ctx.getRealPath(systemId));
				systemId = "file:" + f.getAbsolutePath();
			}
		}

		Logger.debug(this, "resolve: " + systemId);

		return new StreamSource(systemId);
	}

	// public void setDefaultBase(String defaultBase) {
	// this.defaultBase = defaultBase;
	// Logger.debug(this, "defaultBase: " + defaultBase);
	// }
}
