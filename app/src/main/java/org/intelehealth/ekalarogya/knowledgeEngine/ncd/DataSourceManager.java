package org.intelehealth.ekalarogya.knowledgeEngine.ncd;

import org.intelehealth.ekalarogya.app.IntelehealthApplication;
import org.intelehealth.ekalarogya.database.dao.PatientsDAO;
import org.intelehealth.ekalarogya.knowledgeEngine.Node;
import org.intelehealth.ekalarogya.utilities.DateAndTimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class DataSourceManager {
    /**
     * @param sourceDataInfoList
     * @param patientUUID
     * @return
     */
    public static List<SourceData> getValuesForDataSourceFromPatientAttributes(List<SourceData> sourceDataInfoList, String patientUUID) {
        for (int i = 0; i < sourceDataInfoList.size(); i++) {
            SourceData sourceData = sourceDataInfoList.get(i);
            if (sourceData.getDataName().equals(ValidationConstants.SOURCE_DATA_NAME_AGE)) {
                // get eh age of the patient from the db
                String dob = PatientsDAO.fetchDateOfBirth(patientUUID);
                int age = DateAndTimeUtils.getAgeInYear(dob, IntelehealthApplication.getAppContext());
                sourceDataInfoList.get(i).setValue(String.valueOf(age));
            } else if (sourceData.getDataName().equals(ValidationConstants.SOURCE_DATA_NAME_GENDER)) {
                // get eh gender of the patient from the db
                String gender = PatientsDAO.fetch_gender(patientUUID);
                sourceDataInfoList.get(i).setValue(gender.equalsIgnoreCase("M") ? "MALE" : "FEMALE");
            }
        }
        return sourceDataInfoList;
    }

    public static List<SourceData> getValuesForDataSourceFromCurrentNodeRecurringPairDataList(List<SourceData> sourceDataInfoList, Node selectedNode) {
        // get the last value "120/8", "125/89"
        // here two data are saved by separated by "/"
        String lastVal = selectedNode.getRecurringCapturedDataList().get(selectedNode.getRecurringCapturedDataList().size() - 1);
        if (lastVal != null && lastVal.contains("/")) {
            String[] temp = lastVal.split("/");
            if (temp.length >= 2)
                for (int i = 0; i < sourceDataInfoList.size(); i++) {
                    //SourceData sourceData = sourceDataInfoList.get(i);
                    sourceDataInfoList.get(i).setValue(temp[i]);


                }
        }

        return sourceDataInfoList;
    }

    public static List<SourceData> getValuesForDataSourceFromTargetNode(List<SourceData> sourceDataInfoList, Node rootNode, String nodeText) {
        List<Node> questionsNode = rootNode.getOptionsList();
        for (int i = 0; i < questionsNode.size(); i++) {
            Node tempNode = questionsNode.get(i);
            if (tempNode.getText().equals(nodeText)) {
                for (int j = 0; j < sourceDataInfoList.size(); j++) {
                    SourceData tempSourceData = sourceDataInfoList.get(j);
                    if (tempSourceData.getDataName().equals(nodeText)) {
                        sourceDataInfoList.get(j).setValue(tempNode.getLanguage());
                    }
                }

                break;
            }
        }
        return sourceDataInfoList;
    }

    public static SourceData getValuesForDataSourceFromTargetNode(SourceData sourceDataInfo, Node rootNode) {
        List<Node> questionsNode = rootNode.getOptionsList();
        for (int i = 0; i < questionsNode.size(); i++) {
            Node tempNode = questionsNode.get(i);
            if (tempNode.getText().equals(sourceDataInfo.getDataName())) {

                if (tempNode.isTerminal()) {
                    sourceDataInfo.setValue(tempNode.getLanguage());
                } else {
                    if (tempNode.getOptionsList().size() == 1) {
                        // expected here values are in language key
                        sourceDataInfo.setValue(tempNode.getOptionsList().get(0).getLanguage());

                    } else {
                        // expected the selected option is the final value
                        // expected only single choice
                        for (int j = 0; j < tempNode.getOptionsList().size(); j++) {
                            if (tempNode.getOptionsList().get(j).isSelected()) {
                                sourceDataInfo.setValue(tempNode.getOptionsList().get(j).getLanguage());
                            }
                        }
                    }
                }


                break;
            }
        }
        return sourceDataInfo;
    }

    public static String getValuesForDataSourceFromTargetNode(String targetNodeText, Node rootNode) {
        List<Node> questionsNode = rootNode.getOptionsList();
        for (int i = 0; i < questionsNode.size(); i++) {
            Node tempNode = questionsNode.get(i);
            if (tempNode.getText().equals(targetNodeText)) {

                if (tempNode.isTerminal()) {
                    return tempNode.getLanguage();
                } else {
                    if (tempNode.getOptionsList().size() == 1) {
                        // expected here values are in language key
                        return tempNode.getOptionsList().get(0).getLanguage();

                    } else {
                        // expected the selected option is the final value
                        // expected only single choice
                        String result = "";
                        for (int j = 0; j < tempNode.getOptionsList().size(); j++) {
                            if (tempNode.getOptionsList().get(j).isSelected()) {
                                result = tempNode.getOptionsList().get(j).getLanguage();
                                break;
                            }
                        }
                        return result;
                    }
                }


            }
        }
        return "0";
    }

    public static List<SourceData> getValuesForDataSourceFromTargetNestedNode(String rulesType, String sourceDataType, String sourceDataNameType, Node mmRootNode) {
        List<SourceData> resultSourceDataList = new ArrayList<>();
        String[] tempNodeNames = sourceDataNameType.split("#=>>"); // node & node separator
        Node node = null;
        for (int i = 0; i < tempNodeNames.length; i++) {
            String tempText = tempNodeNames[i].trim();
            boolean isLastNode = tempText.contains("<=>") || i == tempNodeNames.length - 1; // node & value separator
            List<Node> options = node == null ? mmRootNode.getOptionsList() : node.getOptionsList();
            for (int j = 0; j < options.size(); j++) {
                if (isLastNode) {
                    String nodeText = tempText.split("<=>")[0];
                    String nodeVal = tempText.split("<=>")[1];
                    // resultSourceDataList = parseDataNameTypeByUnderscore
                    if (options.get(j).getText().equals(nodeText)) {
                        node = options.get(j);
                        if(node.isRecurring()){
                            List<SourceData> sourceDataInfoList = ValidationRulesParser.getSourceDataInfoList(rulesType, sourceDataType, nodeVal);
                            //String lastVal
                            if (sourceDataInfoList != null) {
                                resultSourceDataList = DataSourceManager.getValuesForDataSourceFromCurrentNodeRecurringPairDataList(sourceDataInfoList, node);
                            }
                        }

                        break;
                    }
                } else {
                    if (options.get(j).getText().equals(tempText)) {
                        node = options.get(j);

                        break;
                    }
                }
            }
        }
        return resultSourceDataList;
    }
}
