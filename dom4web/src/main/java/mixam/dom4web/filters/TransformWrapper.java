package mixam.dom4web.filters;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

public class TransformWrapper extends CharResponseWrapper {

    public TransformWrapper(HttpServletResponse response) {
        super(response);
    }

    public Document getDocument(HttpServletRequest request)
            throws ServletException {
        try {
            String content = getContent();
            try {

                if (content.length() == 0)
                    return null;

                Document doc = DocumentHelper.parseText(content);
                // для совместимости с сервлетом default
                // получение кодировки для статических страниц
                String enc = doc.getXMLEncoding();
                if (enc == null)
                    enc = "utf-8";
                doc = DocumentHelper.parseText(getContent(enc));
                // Использовать имя относительно контекста
                // doc.setName(request.getContextPath() +
                // request.getServletPath());
                doc.setName(request.getServletPath());
                Logger.getLogger("transformer").debug(
                        "set document name: " + doc.getName());
                return doc;
            } catch (DocumentException e) {
                Logger.getLogger("transformer").error(
                        "worng content: " + content);
                throw new ServletException(e);
            }
        } catch (UnsupportedEncodingException e) {
            throw new ServletException(e);
        }

    }

    public void debug(Object message) {
        Logger.getLogger("transform").debug(message);
    }

}
