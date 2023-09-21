package org.intelehealth.ekalarogya.utilities;

import java.util.UUID;

public class UuidGenerator {

    public String generateUuid() {
        return UUID.randomUUID().toString();
    }
}
