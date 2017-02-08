package ru.com.m74.xml.transform;

import org.apache.log4j.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * Вспомогательный класс для работы фильтра с содержимым страницы.
 */

public class BufferedResponse extends HttpServletResponseWrapper {

    private Buffer stream = null;
    private PrintWriter writer;

    public boolean isEmpty() {
        return stream == null || getContent().length == 0;
    }

    public byte[] getContent() {
        return stream.getContent();
    }

    public String getContentAsString() throws UnsupportedEncodingException {
        return getContentAsString(getCharacterEncoding());
    }

    public String getContentAsString(String encoding)
            throws UnsupportedEncodingException {
        if (writer != null)
            writer.close();

        return new String(stream.getContent(), encoding).trim();
    }

    public BufferedResponse(HttpServletResponse response) {
        super(response);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new PrintWriter(new OutputStreamWriter(getOutputStream(),
                    getCharacterEncoding()));
//            debug("create writer: " + writer);
        }
        return writer;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (stream == null) {
            stream = new Buffer();
//            debug("create stream: " + stream);
        }
        return stream;
    }

    public void debug(Object message) {
        Logger.getLogger(getClass()).debug(message);
    }
}