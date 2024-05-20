package org.intelehealth.ekalarogya.knowledgeEngine.ncd;

import org.intelehealth.ekalarogya.app.IntelehealthApplication;
import org.intelehealth.ekalarogya.database.dao.PatientsDAO;
import org.intelehealth.ekalarogya.knowledgeEngine.Node;
import org.intelehealth.ekalarogya.utilities.DateAndTimeUtils;

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
                String age = DateAndTimeUtils.getAgeInYearMonth(dob, IntelehealthApplication.getAppContext());
                sourceDataInfoList.get(i).setValue(age);
            } else if (sourceData.getDataName().equals(ValidationConstants.SOURCE_DATA_NAME_GENDER)) {
                // get eh gender of the patient from the db
                String gender = PatientsDAO.fetch_gender(patientUUID);
                sourceDataInfoList.get(i).setValue(gender);
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
}
