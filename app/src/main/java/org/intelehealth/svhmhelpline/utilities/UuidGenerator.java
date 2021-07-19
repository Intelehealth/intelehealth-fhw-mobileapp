package org.intelehealth.svhmhelpline.utilities;

import java.util.UUID;

public class UuidGenerator {

    public String UuidGenerator() {
        String uniqueId = UUID.randomUUID().toString();
        return uniqueId;
    }
}
