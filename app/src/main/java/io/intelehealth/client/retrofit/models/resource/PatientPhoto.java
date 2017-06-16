/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package io.intelehealth.client.retrofit.models.resource;


import android.graphics.Bitmap;
import android.util.Base64;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import io.intelehealth.client.retrofit.models.resource.Resource;

public class PatientPhoto extends Resource implements Serializable {
    @SerializedName("person")
    @Expose
    private String person;

    @SerializedName("base64EncodedImage")
    @Expose
    private String base64EncodedImage;

    public void setPhoto(Bitmap image) {
        byte[] byteArray = bitmapToByteArray(image);
        base64EncodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public void setPerson(String person) {
        this.person = person;
    }

    private byte[] bitmapToByteArray(Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}
