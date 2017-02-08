package ru.com.m74.xml.transform;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mixam
 * @since 08.02.17 23:06
 */
public class FilterHelper {
    private URIResolver uriResolver;

    private TransformerFactory transformerFactory;

    private ErrorListenerImpl errorListener = new ErrorListenerImpl();

    private DocumentBuilder builder;

    public void init(ServletContext context) throws ServletException {
        try {
            uriResolver = new URIResolver(context);
            transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setURIResolver(uriResolver);
            transformerFactory.setErrorListener(errorListener);
            builder = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new ServletException(e);
        }
    }


    public void transform(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain chain) throws IOException, ServletException {
        BufferedResponse bufferedResponse = new BufferedResponse(httpServletResponse);
        chain.doFilter(httpServletRequest, bufferedResponse);

        if (bufferedResponse.isEmpty()) {
            return;
        }

        Pattern pattern = Pattern.compile("<\\?xml-stylesheet.*\\?>", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(bufferedResponse.getContentAsString("UTF-8"));

        String stylesheet = null;
        while (matcher.find()) {
            stylesheet = matcher.group(0).replaceAll("(^.*href=)|\"|\\?>", "");
        }

        try {
            Document document = builder.parse(
                    new ByteArrayInputStream(bufferedResponse.getContent()),
                    httpServletRequest.getRequestURI());

            httpServletResponse.setHeader("ETag", null);
            httpServletResponse.setHeader("Last-Modified", null);
            httpServletResponse.setCharacterEncoding("UTF-8");
            httpServletResponse.setContentLength(-1);
            if (httpServletRequest.getParameter("xml") != null || stylesheet == null) {
                httpServletResponse.setContentType("text/plain");

                Transformer transformer = transformerFactory.newTransformer();
                Source source = new DOMSource(document);
                StreamResult result = new StreamResult(httpServletResponse.getOutputStream());
                transformer.transform(source, result);
            } else {
                httpServletResponse.setContentType("text/html");

                Source xslt = uriResolver.resolve(stylesheet, null);
                Transformer transformer = transformerFactory.newTransformer(xslt);
                // проверка на предмет ошибок
                TransformerException errors[] = errorListener.flash();
                if (errors.length > 0) {
                    throw errors[0];
                }

                Source source = new DOMSource(document);
                StreamResult result = new StreamResult(httpServletResponse.getOutputStream());
                transformer.transform(source, result);
            }

        } catch (TransformerException | SAXException e) {
            throw new ServletException(e);
        }
    }
}
