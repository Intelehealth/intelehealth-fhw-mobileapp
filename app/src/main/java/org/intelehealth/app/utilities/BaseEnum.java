package org.intelehealth.app.utilities;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Prajwal Maruti Waingankar on 04-07-2022, 18:06
 * Copyright (c) 2021 . All rights reserved.
 * Email: prajwalwaingankar@gmail.com
 * Github: prajwalmw
 */

public class BaseEnum {

    public static final int NONE = -1;
    public static final int CMD_ESC = 1, CMD_TSC = 2, CMD_CPCL = 3, CMD_ZPL = 4, CMD_PIN = 5;
    public static final int CON_BLUETOOTH = 1, CON_BLUETOOTH_BLE = 2, CON_WIFI = 3, CON_USB = 4, CON_COM = 5;
    public static final int NO_DEVICE = -1, HAS_DEVICE = 1;

    @IntDef({CMD_ESC, CMD_TSC, CMD_CPCL, CMD_ZPL, CMD_PIN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CmdType {
    }

    @IntDef({CON_BLUETOOTH, CON_WIFI, CON_USB, CON_COM, NONE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ConnectType {
    }


    @IntDef({NO_DEVICE, HAS_DEVICE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ChooseDevice {
    }



}
