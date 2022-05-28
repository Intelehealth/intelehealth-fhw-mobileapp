package org.intelehealth.app.partogram.model;

import java.io.Serializable;
import java.util.List;

public class PartogramItemData implements Serializable {
    private String paramSectionName;
    private List<ParamInfo> paramInfoList;

    public List<ParamInfo> getParamInfoList() {
        return paramInfoList;
    }

    public void setParamInfoList(List<ParamInfo> paramInfoList) {
        this.paramInfoList = paramInfoList;
    }

    public String getParamSectionName() {
        return paramSectionName;
    }

    public void setParamSectionName(String paramSectionName) {
        this.paramSectionName = paramSectionName;
    }
}
