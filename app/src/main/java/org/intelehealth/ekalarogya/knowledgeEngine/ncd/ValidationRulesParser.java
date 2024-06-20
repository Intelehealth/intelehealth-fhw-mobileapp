package org.intelehealth.ekalarogya.knowledgeEngine.ncd;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationRulesParser {

    /**
     * @param type
     * @param data
     * @return
     */
    public static List<SourceData> getSourceDataInfoList(String type, String dataType, String data) {
        if (type.equals(ValidationConstants.TYPE_NAVIGATE)) {
            return parseDataNameTypeByUnderscore(data);
        } else if (type.equals(ValidationConstants.TYPE_RANGE_TEXT)) {
            if (dataType.equals(ValidationConstants.SOURCE_DATA_TYPE_NODE_VAL_INT_SET)) {
                return parseDataNameTypeByUnderscore(data);
            }
        } else if (type.equals(ValidationConstants.TYPE_TEXT_DATE)) {

        } else if (type.equals(ValidationConstants.RECURRING_NUMBER_SET)) {
            return parseDataNameTypeByUnderscore(data);
        } else if (type.equals(ValidationConstants.TYPE_WIGHT_SUM)) {
            return parseDataNameTypeForMultipleNodes(data);
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
    public static List<SourceData> parseDataNameTypeForMultipleNodes(String data) {
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
        // C1(SBP[GREATER_THAN]-OR-DBP[GREATER_THAN]),C2(SBP[LESS_THAN]-OR-DBP[LESS_THAN])
        String[] checkSection = checkSection = check.split(",");

        List<CheckInfoData> checkInfoDataList = new ArrayList<CheckInfoData>();
        for (String checkSecItem : checkSection) {
            String prefix = checkSecItem.substring(0, 3);
            String regex = "^C\\d\\($";
            // Compile the pattern
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(prefix);
            boolean matches = matcher.matches();
            String checkSec = "";
            String finalCheckString = checkSecItem;
            if (matches) {
                checkSec = prefix.substring(0, 2);
                finalCheckString = checkSecItem.substring(3, checkSecItem.length() - 1);
            }


            String[] tempLevel1 = finalCheckString.split("-");

            for (String tempItem : tempLevel1) {

                CheckInfoData checkInfoData = new CheckInfoData();
                if (tempItem.contains("[") && tempItem.contains("]")) {
                    //"source-data": "AGE[INT]_GENDER[STR]",
                    String[] temp1 = tempItem.split("\\[");
                    String dataName = temp1[0];
                    String condition = temp1[1].substring(0, temp1[1].length() - 1);
                    checkInfoData.setDataName(dataName);
                    checkInfoData.setCondition(condition);
                    checkInfoData.setCheckSectionName(checkSec);
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
        }
        return checkInfoDataList;


    }

    public static String getTheDateFromEncodedString(String targetDate) {
        //"THEN": "+1M"
        boolean isAddType = targetDate.startsWith("+");
        char type = targetDate.charAt(targetDate.length() - 1);
        int val = Integer.parseInt(targetDate.substring(1, targetDate.length() - 1));
        Calendar cal = Calendar.getInstance();


        switch (type) {
            case 'M' -> cal.add(Calendar.MONTH, val);
            case 'D' -> cal.add(Calendar.DAY_OF_MONTH, val);
            case 'Y' -> cal.add(Calendar.YEAR, val);
        }
        Date date = cal.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MMM/yyyy", Locale.ENGLISH);
        return simpleDateFormat.format(date);

    }

}
