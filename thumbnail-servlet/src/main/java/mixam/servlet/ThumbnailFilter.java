package mixam.servlet;

import mixam.webtools.Logger;

import javax.imageio.ImageIO;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;

@WebFilter({ "*.jpg", "*.jpeg", "*.gif", "*.png" })
public class ThumbnailFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		Logger.setContext(filterConfig.getServletContext());
		Logger.debug(this, "install");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		Logger.debug(this, "servletPath: ", ((HttpServletRequest) request).getServletPath());
		String servletPath = ((HttpServletRequest) request).getServletPath();
		File thumbnailFile = new File(request.getServletContext().getRealPath(servletPath));

		if (thumbnailFile.exists()) {
			chain.doFilter(request, response);
		} else {
			Info info = Info.parse(servletPath);
			if (info == null)
				chain.doFilter(request, response);
			else {
				String realPath = request.getServletContext().getRealPath(info.filename);
				File file = new File(realPath);
				BufferedImage img = ImageIO.read(file);
				img = scale(img, info);
				writeCache(img, thumbnailFile);
				writeImage(img, (HttpServletResponse) response);
			}
		}
	}

	private static void writeCache(BufferedImage img, File file) throws IOException {
		Logger.info("thumbnails", "create: ", file);
		file.getParentFile().mkdirs();
		FileOutputStream out = new FileOutputStream(file);
		ImageIO.write(img, "jpeg", out);
		out.close();
	}

	private static void writeImage(BufferedImage img, HttpServletResponse response) throws IOException {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(img, "jpeg", out);
		out.close();

		response.setHeader("Cache-Control", "private,no-cache,no-store");
		response.setContentType("image/jpeg");
		response.setHeader("Accept-Ranges", "bytes");
		response.setIntHeader("Content-Length", out.size());

		response.getOutputStream().write(out.toByteArray());
		response.getOutputStream().close();
	}

	private static double getScale(BufferedImage img, Info info) {
		Logger.debug("scale", "info.width: " + info.width);
		Logger.debug("scale", "info.height: " + info.height);
		Logger.debug("scale", "img.getWidth(): " + img.getWidth());
		Logger.debug("scale", "img.getHeight(): " + img.getHeight());

		if (info.width == null)
			return (double) info.height / (double) img.getHeight();

		if (info.height == null)
			return (double) info.width / (double) img.getWidth();

		double wscale = (double) info.width / (double) img.getWidth();
		Logger.debug("scale", "wscale: " + wscale);

		double hscale = (double) info.height / (double) img.getHeight();
		Logger.debug("scale", "hscale: " + hscale);

		return Math.max(wscale, hscale);
	}

	private static BufferedImage scale(BufferedImage img, Info info) {

		double scale = getScale(img, info);
		BigDecimal bd = new BigDecimal(scale);
		bd = bd.setScale(2, BigDecimal.ROUND_UP);
		scale = bd.doubleValue();
		Logger.debug("scale", "scale:  " + scale);

		// boolean nide2crop = info.width != null && info.height != null;

		// if (info.width == null)
		// info.width = (int) (img.getWidth() * scale);
		//
		// if (info.height == null)
		// info.height = (int) (img.getHeight() * scale);

		int scaledW = (int) (scale * img.getWidth());
		int scaledH = (int) (scale * img.getHeight());

		Logger.debug("scale", "scaledW: " + scaledW);
		Logger.debug("scale", "scaledH: " + scaledH);

		ColorModel dstCM = img.getColorModel();
		BufferedImage dst = new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(scaledW, scaledH),
				dstCM.isAlphaPremultiplied(), null);

		// Paint image.
		Graphics2D g = dst.createGraphics();

		// Scale down to half the size
		g.drawImage(img, AffineTransform.getScaleInstance(scale, scale), null);

		Logger.debug("scale", "dst.width: " + dst.getWidth());
		Logger.debug("scale", "dst.height: " + dst.getHeight());

		// crop
		if (info.width != null && info.height != null) {
			Logger.debug("scale", "x: " + (scaledW - info.width) / 2);
			Logger.debug("scale", "y: " + (scaledH - info.height) / 2);
			dst = dst.getSubimage((scaledW - info.width) / 2, (scaledH - info.height) / 2, info.width, info.height);
		}
		g.dispose();

		return dst;
	}

	static class Info {
		Integer width, height;

		String filename;

		static Info parse(String servletPath) {
			Logger.debug("info", "servletPath: ", servletPath);
			String path[] = servletPath.split("/");
			Logger.debug("info", "path: ", path.length, Arrays.asList(path));

			if (path.length <= 2)
				return null;

			Info info = new Info();
			info.filename = "";
			for (int i = 0; i < path.length; i++) {
				if (i != path.length - 2) {
					info.filename += path[i];
					if (i != path.length - 1) {
						info.filename += "/";
					}
				}
			}

			Logger.debug("info", info.filename);
			String str = path[path.length - 2];

			String arr[] = str.split("x");
			Logger.debug("info", "arr: ", arr.length, Arrays.asList(arr));

			if (str.charAt(0) == 'x') {
				info.height = Integer.parseInt(arr[1]);
			} else if (str.charAt(str.length() - 1) == 'x') {
				info.width = Integer.parseInt(arr[0]);
			} else {
				info.width = Integer.parseInt(arr[0]);
				info.height = Integer.parseInt(arr[1]);
			}

			Logger.debug("info", "width: ", info.width);
			Logger.debug("info", "height: ", info.height);

			return info;
		}
	}

	@Override
	public void destroy() {

	}

}
