package org.intelehealth.unicef.activities.presription;

/**
 * Created by Prajwal Maruti Waingankar on 23-12-2021, 01:25
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

public class PrescDataModel {
    private String uuid;
    private String value;

    public PrescDataModel(String uuid, String value) {
        this.uuid = uuid;
        this.value = value;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
