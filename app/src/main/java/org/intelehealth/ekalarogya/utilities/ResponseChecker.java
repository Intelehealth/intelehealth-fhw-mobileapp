package org.intelehealth.ekalarogya.utilities;

import retrofit2.Response;

public class ResponseChecker<T> {
    private final Response<T> response;

    public ResponseChecker(Response<T> response) {
        this.response = response;
    }

    public boolean isNotAuthorized() {
        return response.code() == 401;
    }
}