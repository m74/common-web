package mixam.dom4web;

import java.io.IOException;
import java.io.Writer;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import mixam.webtools.Logger;

import org.dom4j.Document;
import org.dom4j.io.DocumentSource;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public abstract class ResultHandler {
	// protected final Logger logger = LoggerFactory.getLogger(this);

	private String encoding = null, contentType = null;

	public abstract Writer getWriter() throws IOException;

	public void write(Document doc) throws IOException {
		OutputFormat f = OutputFormat.createPrettyPrint();
		// OutputFormat f = OutputFormat.createCompactFormat();
		// f.setLineSeparator("<!-- new line -->\r\n");

		f.setEncoding(doc.getXMLEncoding());
		XMLWriter w = new XMLWriter(getWriter(), f);
		w.write(doc);
	}

	public void transform(TransformerFactory factory, DocumentSource xml) throws TransformerException, IOException {

		Source xsl = null;
		ErrorListener err = (ErrorListener) factory.getErrorListener();

		Logger.debug(this, "isTransformEnabled(): ", isTransformEnabled());
		if (isTransformEnabled()) {
			xsl = factory.getAssociatedStylesheet(xml, null, null, null);
		}

		if (xsl == null) {
			Logger.info(this, "XSL is NULL!");
			beforeTransform();
			write(xml.getDocument());
		} else {
			Logger.debug(this, "xsl: " + xsl.getSystemId());

			Transformer transformer = factory.newTransformer(xsl);
			Logger.debug(this, "transformer: " + transformer);

			err.checkException();

			if (getEncoding() == null)
				setEncoding(transformer.getOutputProperty("encoding"));
			if (getContentType() == null)
				setContentType(contentType(transformer));

			beforeTransform();
			transformer.transform(xml, new StreamResult(getWriter()));
			err.checkException();
			afterTransform(transformer);
		}

	}

	protected void afterTransform(Transformer transformer) {

	}

	protected void beforeTransform() {

	}

	public boolean isTransformEnabled() {
		return true;
	}

	protected static String contentType(Transformer transformer) {
		String method = transformer.getOutputProperty("method");
		String encoding = transformer.getOutputProperty("encoding");

		if (method.equals("text"))
			method = "plain";
		return "text/" + method + "; charset=" + encoding;
	}

	public String getEncoding() {
		return encoding;
	}

	public String getContentType() {
		return contentType;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
}
