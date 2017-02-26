package mixam.dom4web;

import mixam.webtools.Logger;
import org.dom4j.Document;
import org.dom4j.io.DocumentSource;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public abstract class TransformedServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private TransformerFactory factory = TransformerFactory.newInstance();
	private ErrorListener err = new ErrorListener();

	// private URIResolver resolver;

	@Override
	public void init(ServletConfig config) throws ServletException {
		ServletContext ctx = config.getServletContext();
		factory.setURIResolver(new URIResolver(ctx));

		factory.setErrorListener(err);
		Logger.info(this, "setErrorListener: " + err);

		super.init(config);
	}

	/**
	 * Обработка документа.
	 * 
	 * @param doc
	 *            Документ
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void processDocument(Document doc, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// response.setCharacterEncoding("utf-8");
		// response.setCharacterEncoding(doc.getXMLEncoding());
		ResponseResult result = new ResponseResult(request, response);

		// result.setFileName(doc.getF)
		transform(doc, request, result);
	}

	/**
	 * Трансформировать в строку.
	 * 
	 * @param doc
	 * @param request
	 *            Нужен для получения имени документа, имя используется для
	 *            определения абсолютного пути ресурсов.
	 * @throws ServletException
	 * @throws IOException
	 */
	public StringResult transformToString(Document doc, HttpServletRequest request)
			throws ServletException, IOException {
		doc.setName(getDocumentURI(request));
		StringResult handler = new StringResult();
		try {
			handler.transform(factory, new DocumentSource(doc));
		} catch (TransformerException e) {
			throw new ServletException(e.getMessage());
		}
		return handler;
	}

	protected ResultHandler transform(Document doc, HttpServletRequest request, ResultHandler handler)
			throws ServletException, IOException {

		// Имя документа необходимо для определения резолвером относительного
		// пути подчиненных документов (таблиц стиле в основном..)
		doc.setName(getDocumentURI(request));
		Logger.info(this, "document.setName: " + doc.getName());

		try {
			// logger.info("resolve: "
			// + factory.getURIResolver().resolve("/helo", doc.getName())
			// .getSystemId());

			handler.transform(factory, new DocumentSource(doc));
		} catch (TransformerException e) {
			throw new ServletException(e);
		}
		return handler;
	}

	protected String getDocumentURI(HttpServletRequest request) {
		String uri = request.getServletPath();
		try {
			return new String(uri.getBytes("iso-8859-1"), "utf-8");
		} catch (UnsupportedEncodingException e) {
			return uri;
		}
	}

}
