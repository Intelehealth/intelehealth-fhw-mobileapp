package org.intelehealth.ezazi.executor;

import java.util.concurrent.Callable;

/**
 * Created by Vaghela Mithun R. on 18-06-2023 - 12:30.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
public interface TaskCompleteListener<T> extends Callable<T> {
    default void onComplete(T result) {
    }
}
