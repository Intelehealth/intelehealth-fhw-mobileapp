package org.intelehealth.ezazi.ui.visit.model;

/**
 * Created by Kaveri Zaware on 14-09-2023
 * email - kaveri@intelehealth.org
 **/
public class ConceptsDetailsModel {
    private String label;

    public ConceptsDetailsModel(String label, String conceptUuid) {
        this.label = label;
        this.conceptUuid = conceptUuid;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getConceptUuid() {
        return conceptUuid;
    }

    public void setConceptUuid(String conceptUuid) {
        this.conceptUuid = conceptUuid;
    }

    private String conceptUuid;
}
