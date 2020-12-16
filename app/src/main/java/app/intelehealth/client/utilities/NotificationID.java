package app.intelehealth.client.utilities;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Sagar Shimpi
 * Github - sagarS23
 */
public class NotificationID {
    private final static AtomicInteger c = new AtomicInteger(5);

    public static int getID() {
        return c.incrementAndGet();
    }
}
