package app.intelehealth.client.utilities.exception;

public class ActionException extends Exception {
    private static final long serialVersionUID = 1L;
    private Throwable thwStack;
    private String label = "unable_to_process_request";

    public ActionException(Exception excp) {
        super(excp);
        setThwStack(excp);

    }

    public ActionException(String msg, Throwable e) {
        super(msg, e);
        setThwStack(e);

    }

    public ActionException() {

        super();

    }

    public ActionException(String message) {
        super(message);
    }

    public ActionException(String message, String label, Throwable e) {
        super(message, e);
        setLabel(label);
    }

    public ActionException(String message, String label) {
        super(message);
        setLabel(label);
    }

    public Throwable getThwStack() {
        return thwStack;
    }

    public void setThwStack(Throwable throwable) {
        thwStack = throwable;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }


}