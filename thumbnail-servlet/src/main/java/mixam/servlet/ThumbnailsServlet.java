package mixam.servlet;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import mixam.webtools.Logger;

public class ThumbnailsServlet extends HttpServlet {
	private static final long serialVersionUID = -3245125614979916673L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String pi = request.getPathInfo();

		if (pi == null)
			throw new FileNotFoundException();
		String arr[] = pi.split("/");

		String filename = null;
		String scaleInfo = null;
		if (arr.length == 2) {
			filename = arr[1];
		} else if (arr.length == 3) {
			filename = arr[2];
			scaleInfo = arr[1];
		} else {
			throw new FileNotFoundException();
		}

		try {
			String base = (String) new InitialContext().lookup("java:comp/env/images/base");
			File file = new File(base, filename);

			if (!file.exists()) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			if (scaleInfo != null) {
				BufferedImage img = ImageIO.read(file);
				img = scale(img, scaleInfo);
				writeImage(img, response, "png");
			} else {
				writeFile(file, response);
			}

		} catch (NamingException e) {
			throw new ServletException(e);
		}
	}

	private BufferedImage scale(BufferedImage img, String scaleInfo) {

		int width = img.getWidth();
		int height = img.getHeight();
		if (scaleInfo.endsWith("x"))
			width = Integer.parseInt(scaleInfo.substring(0, scaleInfo.length() - 1));
		else if (scaleInfo.startsWith("x"))
			height = Integer.parseInt(scaleInfo.substring(1));

		double scale = Math.min((double) width / (double) img.getWidth(), (double) height / (double) img.getHeight());

		Logger.debug(this, "scale: " + scale);

		if (scale < 1) {

			int scaledW = (int) (scale * img.getWidth());
			int scaledH = (int) (scale * img.getHeight());

			// BufferedImage outImage = new BufferedImage(scaledW,
			// scaledH,
			// BufferedImage.TYPE_INT_RGB);

			ColorModel dstCM = img.getColorModel();
			BufferedImage dst = new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(scaledW, scaledH),
					dstCM.isAlphaPremultiplied(), null);

			// Paint image.
			Graphics2D g = dst.createGraphics();

			// Scale down to half the size
			g.drawImage(img, AffineTransform.getScaleInstance(scale, scale), null);

			// img.getScaledInstance(width, height,
			// Image.SCALE_AREA_AVERAGING);
			// g.drawImage(img, 0, 0, scaledW, scaledH, null);

			if (scale < 0.8)
				blur(dst, g);

			g.dispose();

			return dst;
		}

		return img;
	}

	private void blur(BufferedImage img, Graphics2D g2d) {

		int blurMagnitude = 2;
		float[] data = new float[blurMagnitude * blurMagnitude];
		for (int i = 0, n = data.length; i < n; i++) {
			data[i] = 1f / n;
		}

		ConvolveOp cop = new ConvolveOp(new Kernel(blurMagnitude, blurMagnitude, data), ConvolveOp.EDGE_NO_OP, null);
		g2d.drawImage(img, cop, 0, 0);
	}

	private void writeImage(BufferedImage img, HttpServletResponse response, String type) throws IOException {

		if (type.equals("jpg"))
			type = "jpeg";
		if (type.equals("gif"))
			type = "png";

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(img, type, out);
		out.close();

		response.setHeader("Cache-Control", "private,no-cache,no-store");
		response.setContentType("image/" + type);
		response.setHeader("Accept-Ranges", "bytes");
		response.setIntHeader("Content-Length", out.size());

		response.getOutputStream().write(out.toByteArray());
		response.getOutputStream().close();
	}

	private void writeFile(File file, HttpServletResponse response) throws IOException {

		// ByteArrayOutputStream out = new ByteArrayOutputStream();
		FileInputStream in = new FileInputStream(file);

		byte buff[] = IOUtils.toByteArray(in);

		response.setHeader("Cache-Control", "private,no-cache,no-store");
		response.setContentType("image/png");
		response.setHeader("Accept-Ranges", "bytes");
		response.setIntHeader("Content-Length", (int) buff.length);

		response.getOutputStream().write(buff);
		response.getOutputStream().close();
	}
}
