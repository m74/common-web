package mixam.dom4web;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class StringResult extends ResultHandler {
	private Writer out = null;

	public StringBuffer getContent() {
		return ((StringWriter) out).getBuffer();
	}

	@Override
	public Writer getWriter() throws IOException {
		if (out == null)
			out = new StringWriter();
		return out;
	}
}
