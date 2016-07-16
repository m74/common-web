package mixam.app.server;

import org.dom4j.Element;

public class DOM {
	public static void addTextElement(Element node, String name, Object text) {
		if (text != null) {
			node.addElement(name).setText(text.toString().replaceAll("<[^>]*>", "").replaceAll("&#\\d*;", ""));
		}
	}

	public static void setAttribute(Element el, String name, Object value) {
		if (value != null) {
			el.addAttribute(name, value.toString());
		}
	}
}
