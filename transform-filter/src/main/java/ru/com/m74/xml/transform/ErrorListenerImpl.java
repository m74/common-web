package ru.com.m74.xml.transform;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mixam
 * @since 08.01.17 23:59
 */
public class ErrorListenerImpl implements ErrorListener {
    private final List<TransformerException> buffer = new ArrayList<>();

    @Override
    public void warning(TransformerException exception) throws TransformerException {
//        buffer.add(exception);
    }

    @Override
    public void error(TransformerException exception) throws TransformerException {
//        buffer.add(exception);
    }

    @Override
    public void fatalError(TransformerException exception) throws TransformerException {
        buffer.add(exception);
    }

    public TransformerException[] flash() {
        TransformerException arr[] = buffer.toArray(new TransformerException[buffer.size()]);
        buffer.clear();
        return arr;
    }
}
