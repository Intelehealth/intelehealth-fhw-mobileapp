package org.intelehealth.ezazi.partogram;

import org.intelehealth.ezazi.partogram.model.ParamInfo;

public class PartogramAlertEngine {
    public static String getAlertName(ParamInfo paramInfo) {
        if (paramInfo.getCapturedValue() == null || paramInfo.getCapturedValue().isEmpty()) {
            return "";
        }
        String alert = "G";
        if (paramInfo.getParamSectionName().equalsIgnoreCase("Supportive care")) {
            if (paramInfo.getParamName().equalsIgnoreCase("Companion")) {
                if (paramInfo.getCapturedValue().equalsIgnoreCase("Y") || paramInfo.getCapturedValue().equalsIgnoreCase("D")) {
                    alert = "G";
                } else if (paramInfo.getCapturedValue().equalsIgnoreCase("N")) {
                    alert = "Y";
                }
            } else if (paramInfo.getParamName().equalsIgnoreCase("Pain relief")) {
                if (paramInfo.getCapturedValue().equalsIgnoreCase("Y")) {
                    alert = "G";
                } else if (paramInfo.getCapturedValue().equalsIgnoreCase("N")) {
                    alert = "R";
                }
            } else if (paramInfo.getParamName().equalsIgnoreCase("Oral fluid")) {
                if (paramInfo.getCapturedValue().equalsIgnoreCase("Y")) {
                    alert = "G";
                } else if (paramInfo.getCapturedValue().equalsIgnoreCase("N")) {
                    alert = "R";
                }
            } else if (paramInfo.getParamName().equalsIgnoreCase("Posture")) {
                if (paramInfo.getCapturedValue().equalsIgnoreCase("MO")) {
                    alert = "G";
                } else if (paramInfo.getCapturedValue().equalsIgnoreCase("SP")) {
                    alert = "Y";
                }
            }
        } else if (paramInfo.getParamSectionName().equalsIgnoreCase("BABY")) {

            if (paramInfo.getParamName().equalsIgnoreCase("Baseline FHR")) {
                int val = Integer.parseInt(paramInfo.getCapturedValue());
                if (val >= 160) {
                    alert = "Y";
                } else if (val < 110) {
                    alert = "R";
                } else {
                    alert = "G";
                }
            } else if (paramInfo.getParamName().equalsIgnoreCase("FHR deceleration")) {
                if (paramInfo.getCapturedValue().equalsIgnoreCase("N") || paramInfo.getCapturedValue().equalsIgnoreCase("E")) {
                    alert = "G";
                } else if (paramInfo.getCapturedValue().equalsIgnoreCase("V")) {
                    alert = "Y";
                } else if (paramInfo.getCapturedValue().equalsIgnoreCase("L")) {
                    alert = "R";
                }
            } else if (paramInfo.getParamName().equalsIgnoreCase("Amniotic fluid meconium")) {
                if (paramInfo.getCapturedValue().equalsIgnoreCase("I") || paramInfo.getCapturedValue().equalsIgnoreCase("C")) {
                    alert = "G";
                } else if (paramInfo.getCapturedValue().equalsIgnoreCase("M+") || paramInfo.getCapturedValue().equalsIgnoreCase("M++")) {
                    alert = "Y";
                } else if (paramInfo.getCapturedValue().equalsIgnoreCase("M+++") || paramInfo.getCapturedValue().equalsIgnoreCase("B")) {
                    alert = "R";
                }
            } else if (paramInfo.getParamName().equalsIgnoreCase("Fetal position")) {
                if (paramInfo.getCapturedValue().equalsIgnoreCase("OA")) {
                    alert = "G";
                } else if (paramInfo.getCapturedValue().equalsIgnoreCase("OP") || paramInfo.getCapturedValue().equalsIgnoreCase("OT")) {
                    alert = "R";
                }
//                else if (paramInfo.getCapturedValue().equalsIgnoreCase("M+++") || paramInfo.getCapturedValue().equalsIgnoreCase("B")) {
//                    alert = "R";
//                }
            } else if (paramInfo.getParamName().equalsIgnoreCase("Caput")) {
                if (paramInfo.getCapturedValue().equalsIgnoreCase("N") || paramInfo.getCapturedValue().equalsIgnoreCase("+") || paramInfo.getCapturedValue().equalsIgnoreCase("++")) {
                    alert = "G";
                } else if (paramInfo.getCapturedValue().equalsIgnoreCase("+++")) {
                    alert = "R";
                }
            } else if (paramInfo.getParamName().equalsIgnoreCase("Moulding")) {
                if (paramInfo.getCapturedValue().equalsIgnoreCase("N") || paramInfo.getCapturedValue().equalsIgnoreCase("+") || paramInfo.getCapturedValue().equalsIgnoreCase("++")) {
                    alert = "G";
                } else if (paramInfo.getCapturedValue().equalsIgnoreCase("+++")) {
                    alert = "R";
                }
            }
        } else if (paramInfo.getParamSectionName().equalsIgnoreCase("Woman")) {
            if (paramInfo.getParamName().equalsIgnoreCase("Pulse")) {
                int val = Integer.parseInt(paramInfo.getCapturedValue());
                if (val < 60) {
                    alert = "Y";
                } else if (val >= 120) {
                    alert = "R";
                } else {
                    alert = "G";
                }
            } else if (paramInfo.getParamName().equalsIgnoreCase("Systolic BP")) {
                int val = Integer.parseInt(paramInfo.getCapturedValue());
                if (val < 80 || val >= 140) {
                    alert = "R";
                } else {
                    alert = "G";
                }
            } else if (paramInfo.getParamName().equalsIgnoreCase("Diastolic BP")) {
                int val = Integer.parseInt(paramInfo.getCapturedValue());
                if (val >= 90) {
                    alert = "R";
                } else {
                    alert = "G";
                }
            } else if (paramInfo.getParamName().equalsIgnoreCase("Temperature(C)")) {
                double val = Double.parseDouble(paramInfo.getCapturedValue());
                if (val < 35) {
                    alert = "Y";
                } else if (val >= 37.5) {
                    alert = "R";
                } else {
                    alert = "G";
                }
            } else if (paramInfo.getParamName().equalsIgnoreCase("Urine protein")) {
                if (paramInfo.getCapturedValue().equalsIgnoreCase("P-") || paramInfo.getCapturedValue().equalsIgnoreCase("P")
                        || paramInfo.getCapturedValue().equalsIgnoreCase("P1")) {
                    alert = "G";
                } else if (paramInfo.getCapturedValue().equalsIgnoreCase("P2")) {
                    alert = "Y";
                } else if (paramInfo.getCapturedValue().equalsIgnoreCase("P3")) {
                    alert = "R";
                }
            } else if (paramInfo.getParamName().equalsIgnoreCase("Urine Acetone")) {
                if (paramInfo.getCapturedValue().equalsIgnoreCase("A-") || paramInfo.getCapturedValue().equalsIgnoreCase("A")
                        || paramInfo.getCapturedValue().equalsIgnoreCase("A1")) {
                    alert = "G";
                } else if (paramInfo.getCapturedValue().equalsIgnoreCase("A2")) {
                    alert = "Y";
                } else if (paramInfo.getCapturedValue().equalsIgnoreCase("A3")) {
                    alert = "R";
                }
            }
        } else if (paramInfo.getParamSectionName().equalsIgnoreCase("Labour Progress")) {
            if (paramInfo.getParamName().equalsIgnoreCase("Contractions per 10 min")) {
                int val = Integer.parseInt(paramInfo.getCapturedValue());
                if (val > 5) {
                    alert = "R";
                } else if (val == 1 || val == 2) {
                    alert = "Y";
                } else {
                    alert = "G";
                }
            } else if (paramInfo.getParamName().equalsIgnoreCase("Duration of contractions")) {
                int val = Integer.parseInt(paramInfo.getCapturedValue());
                if (val > 60) {
                    alert = "R";
                } else if (val < 20) {
                    alert = "Y";
                } else {
                    alert = "G";
                }
            }
        } else if (paramInfo.getParamSectionName().equalsIgnoreCase("Medication")) {

        } else if (paramInfo.getParamSectionName().equalsIgnoreCase("Shared Decision Making")) {

        } else if (paramInfo.getParamSectionName().equalsIgnoreCase("Initials")) {

        }
        return alert;
    }
}
