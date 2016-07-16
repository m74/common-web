package mixam.app.server;

import mixam.dom4web.ErrorListener;
import mixam.dom4web.ResponseResult;
import mixam.dom4web.URIResolver;
import mixam.dom4web.filters.TransformWrapper;
import mixam.webtools.Logger;
import mixam.webtools.LoggerName;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.DocumentSource;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

/**
 * @author mixam
 */
@WebFilter(filterName = "transformer", urlPatterns = {"*.xml"})
@LoggerName("transformer")
public class TransformFilter implements Filter {
    private TransformerFactory factory = TransformerFactory.newInstance();
    private ErrorListener err = new ErrorListener();
    private URIResolver resolver;

    public void init(FilterConfig config) throws ServletException {
        Logger.setContext(config.getServletContext());
        Logger.info(this, "init: ", config);
        setUriResolver(new URIResolver(config.getServletContext()));
        factory.setURIResolver(getUriResolver());
        factory.setErrorListener(getErrorListener());
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        TransformWrapper wrapper = new TransformWrapper((HttpServletResponse) servletResponse);

        chain.doFilter(request, wrapper);
        wrapper.setHeader("ETag", null);
        wrapper.setHeader("Last-Modified", null);

        writeDocument(factory, wrapper.getDocument(request), request, (HttpServletResponse) servletResponse);

    }

    /**
     * Добавление в документ элементов контекста сервлета.
     *
     * @param root
     * @param request
     */
    public static void processDocument(Element root, HttpServletRequest request) {
        root.addAttribute("contextPath", request.getContextPath());

        HttpSession session = request.getSession();

        // Session attributes
        Element el = root.addElement("session");
        Enumeration<String> en = session.getAttributeNames();
        while (en.hasMoreElements()) {
            String name = en.nextElement();
            DOM.addTextElement(el, name, session.getAttribute(name));
        }

    }

    protected static String getDocumentURI(HttpServletRequest request) {
        String uri = request.getServletPath();
        try {
            return new String(uri.getBytes("iso-8859-1"), "utf-8");
        } catch (UnsupportedEncodingException e) {
            return uri;
        }
    }

    public static void writeDocument(TransformerFactory factory, Document doc, HttpServletRequest request,
                                     HttpServletResponse response) throws IOException, ServletException {

        if (doc == null) {
            Logger.debug(TransformFilter.class, "skip content");
            return;
        }

        // String uri = request.getRequestURI();
        String uri = request.getServletPath();
        // String uri = request.getPathInfo();
        int i = uri.lastIndexOf('/');
        if (i != -1) {
            uri = uri.substring(0, i) + '/';
        }

        if (doc.getName() == null)
            doc.setName(getDocumentURI(request));
        Logger.debug(TransformFilter.class, "document name: ", doc.getName());

        Element root = doc.getRootElement();
        root.addAttribute("uri", uri);

        Element path = root.addElement("path");

        String arr[] = uri.split("\\/");
        uri = "";
        if (arr.length == 0) {
            Element elem = path.addElement("elem");
            elem.addAttribute("href", "/");
        } else {
            for (String id : arr) {
                Element elem = path.addElement("elem");
                uri += id + "/";
                // elem.addAttribute("id", id);
                elem.addAttribute("href", uri);
            }
        }
        processDocument(doc.getRootElement(), request);

        response.setCharacterEncoding(doc.getXMLEncoding());
        response.setContentLength(-1);

        ResponseResult result = new ResponseResult(request, (HttpServletResponse) response);
        try {
            result.transform(factory, new DocumentSource(doc));
        } catch (TransformerException e) {
            throw new ServletException(e);
        }

    }

    public void debug(Object message) {
        Logger.debug(this, message);
    }

    public void destroy() {
    }

    public void setErrorListener(ErrorListener err) {
        this.err = err;
    }

    public ErrorListener getErrorListener() {
        return err;
    }

    public void setUriResolver(URIResolver resolver) {
        this.resolver = resolver;
    }

    public URIResolver getUriResolver() {
        return resolver;
    }

}
