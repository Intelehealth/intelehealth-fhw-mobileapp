package org.intelehealth.app.models.auth;
import retrofit2.Response;

/**
 * Created by - Prajwal W. on 04/07/24.
 * Email: prajwalwaingankar@gmail.com
 * Mobile: +917304154312
 **/
public class ResponseChecker<T> {
    private final Response<T> response;

    public ResponseChecker(Response<T> response) {
        this.response = response;
    }

    public boolean isNotAuthorized() {
        return response.code() == 401;
    }
}
