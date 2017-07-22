package mixam.dom4web.filters;

import org.apache.log4j.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;

/**
 * Вспомогательный класс для работы фильтра с содержимым страницы в виде строки.
 */

public class CharResponseWrapper extends HttpServletResponseWrapper {
	private final ByteArrayOutputStream bout = new ByteArrayOutputStream();

	private ServletOutputStream stream;
	private PrintWriter writer;

	public String getContent() throws UnsupportedEncodingException {
		return getContent(getCharacterEncoding());
	}

	public String getContent(String encoding)
			throws UnsupportedEncodingException {
		if (writer != null)
			writer.close();

		return new String(bout.toByteArray(), encoding).trim();
	}

	public CharResponseWrapper(HttpServletResponse response) {
		super(response);
	}

	public PrintWriter getWriter() throws IOException {
		if (writer == null) {
			writer = new PrintWriter(new OutputStreamWriter(getOutputStream(),
					getCharacterEncoding()));
			debug("create writer: " + writer);
		}
		return writer;
	}

	public ServletOutputStream getOutputStream() throws IOException {
		if (stream == null) {
			stream = new ServletOutputStream() {
				@Override
				public boolean isReady() {
					return false;
				}

				@Override
				public void setWriteListener(WriteListener writeListener) {

				}

				public void write(int b) throws IOException {
					bout.write((byte) b);
				}
			};
			debug("create stream: " + stream);
		}
		return stream;
	}

	public void debug(Object message) {
		Logger.getLogger("char").debug(message);
	}
}