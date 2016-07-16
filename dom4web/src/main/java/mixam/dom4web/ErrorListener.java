/*
 * ErrorListenerImpl.java
 *
 * Created on 28 январь 2009 г., 9:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package mixam.dom4web;

import mixam.webtools.Logger;

import javax.xml.transform.TransformerException;

/**
 * @author mixam
 */
public class ErrorListener implements javax.xml.transform.ErrorListener {
    private TransformerException exception = null;

    public void warning(TransformerException exception)
            throws TransformerException {
        this.exception = exception;
        Logger.info(this, exception);
    }

    public void error(TransformerException exception)
            throws TransformerException {

        String message = exception.getLocalizedMessage()
                + exception.getLocationAsString();
        this.exception = new TransformerException(message);
    }

    public void fatalError(TransformerException exception)
            throws TransformerException {
        this.exception = exception;
        Logger.error(this, exception);
    }

    public void checkException() throws TransformerException {
        if (exception != null) {
            TransformerException exception = this.exception;
            this.exception = null;
            throw exception;
        }
    }

    public TransformerException getException() {
        return exception;
    }
}