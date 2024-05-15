package org.intelehealth.ekalarogya.knowledgeEngine.ncd;

import java.util.ArrayList;
import java.util.List;

public class ValidationRulesParser {


    public static List<SourceData> getSourceDataInfoList(String type, String data) {
        if (type.equals(ValidationConstants.TYPE_NAVIGATE)) {
            return parseDataNameTypeByUnderscore(data);
        } else if (type.equals(ValidationConstants.TYPE_RANGE_TEXT)) {

        } else if (type.equals(ValidationConstants.TYPE_TEXT_DATE)) {

        }
        return null;
    }

    public static List<SourceData> parseDataNameTypeByUnderscore(String data) {
        List<SourceData> sourceDataList = new ArrayList<>();
        String[] tempLevel1 = data.split("_");
        for (String tempItem : tempLevel1) {
            SourceData sourceData = new SourceData();
            if (tempItem.contains("[") && tempItem.contains("]")) {
                //"source-data": "AGE[INT]_GENDER[STR]",
                String[] temp1 = tempItem.split("\\[");
                String dataName = temp1[0];
                String dataType = temp1[1].substring(0, temp1[1].length() - 1);
                sourceData.setDataName(dataName);
                sourceData.setDataType(dataType);
            } else {
                sourceData.setDataName(tempItem);
            }
            sourceDataList.add(sourceData);
        }
        return sourceDataList; // 1st :
    }

    public static String[] parseByHyphen(String data) {
        return data.split("-"); // 1st :
    }
}
