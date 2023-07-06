package org.intelehealth.ezazi.core;

import org.intelehealth.ezazi.core.data.BaseDataSource;

/**
 * Created by Kaveri Zaware on 06-07-2023
 * email - kaveri@intelehealth.org
 **/
public class BaseRepository {
    public final BaseDataSource serviceDataSource;

    public BaseRepository(BaseDataSource serviceDataSource) {
        this.serviceDataSource = serviceDataSource;
    }
}
