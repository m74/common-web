package mixam.dom4web;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mixam.webtools.Logger;

import org.dom4j.Document;

public class ResponseResult extends ResultHandler {
	private HttpServletRequest request;
	private HttpServletResponse response;
	private String fileName;

	public ResponseResult(HttpServletRequest request, HttpServletResponse response) throws IOException {
		this.request = request;
		this.response = response;
	}

	@Override
	public void write(Document doc) throws IOException {
		if (!isTransformEnabled()) {
			response.setContentType("text/plain");
		}
		response.setCharacterEncoding(doc.getXMLEncoding());
		super.write(doc);
	}

	@Override
	public boolean isTransformEnabled() {
		return request.getParameter("xml") == null;
	}

	@Override
	protected void beforeTransform() {

		if (getEncoding() != null) {
			Logger.debug(this, "setEncoding: " + getEncoding());
			response.setCharacterEncoding(getEncoding());
		}

		if (isTransformEnabled()) {
			if (getContentType() != null) {
				Logger.debug(this, "setContentType: " + getContentType());
				response.setContentType(getContentType());
			}

			if (this.fileName != null) {
				Logger.debug(this, "setFileName: " + this.fileName);
				response.setHeader("Content-Disposition", "attachment; filename=\"" + this.fileName + "\"");
			}
		} else {
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/xml;charset=UTF-8");
		}

	}

	@Override
	public Writer getWriter() throws IOException {
		return response.getWriter();
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}
}
