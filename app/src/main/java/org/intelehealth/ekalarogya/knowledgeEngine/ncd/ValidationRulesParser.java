package org.intelehealth.ekalarogya.knowledgeEngine.ncd;

import java.util.ArrayList;
import java.util.List;

public class ValidationRulesParser {

    /**
     * @param type
     * @param data
     * @return
     */
    public static List<SourceData> getSourceDataInfoList(String type, String data) {
        if (type.equals(ValidationConstants.TYPE_NAVIGATE)) {
            return parseDataNameTypeByUnderscore(data);
        } else if (type.equals(ValidationConstants.TYPE_RANGE_TEXT)) {

        } else if (type.equals(ValidationConstants.TYPE_TEXT_DATE)) {

        }
        return null;
    }

    /**
     * @param data
     * @return
     */
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

    /**
     * @param data
     * @return
     */
    public static String[] parseByHyphen(String data) {
        return data.split("-"); // 1st :
    }

    public static void translatingCheckInfo() {

    }

    public static List<CheckInfoData> getCheckInfoList(String check) {
        //  "check": "AGE[GREATER_THAN]-AND-GENDER[EQUAL]",

        List<CheckInfoData> checkInfoDataList = new ArrayList<CheckInfoData>();
        String[] tempLevel1 = check.split("-");

        for (String tempItem : tempLevel1) {

            CheckInfoData checkInfoData = new CheckInfoData();
            if (tempItem.contains("[") && tempItem.contains("]")) {
                //"source-data": "AGE[INT]_GENDER[STR]",
                String[] temp1 = tempItem.split("\\[");
                String dataName = temp1[0];
                String condition = temp1[1].substring(0, temp1[1].length() - 1);
                checkInfoData.setDataName(dataName);
                checkInfoData.setCondition(condition);
            } else {
                //checkInfoData.setCondition(tempItem);
                // this
                int lastIndex = checkInfoDataList.size() - 1;

                checkInfoDataList.get(lastIndex).setAssociateOperator(tempItem);
                checkInfoDataList.get(lastIndex).setHavingAssociateCondition(true);
                continue;
            }
            checkInfoDataList.add(checkInfoData);

        }
        return checkInfoDataList;


    }

}
