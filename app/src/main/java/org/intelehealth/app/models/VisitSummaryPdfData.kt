package org.intelehealth.app.models

import org.intelehealth.config.room.entity.FeatureActiveStatus
import java.io.File

/**
 * Created By Tanvir Hasan on 5/30/24 4:28 AM
 * Email: tanvirhasan553@gmail.com
 */
data class VisitSummaryPdfData(
    var patientImage: String,
    var patientName: String,
    var patientId: String,
    var genderAge: String,
    var chwName: String,
    var visitId: String,

    var height: String,
    var weight: String,
    var bmi: String,
    var bp: String,
    var pulse: String,
    var temp: String,
    var spoTwo: String,
    var respiratory: String,
    var blGroup: String,
    var tempHeader: String,
    var severity: String,
    var facility: String,
    var followUpDate: String,

    var chiefComplain: String,
    var associateSymptoms: String,
    var physicalExam: String,
    var medicalHistory: String,
    var additionalNote: String,
    var doctorSpeciality: String,
    var priorityVisit: String,
    var additionalDocList: MutableList<DocumentObject>,
    var chiefComplaintList: MutableList<String>,
    var physicalExamImageList: MutableList<File>,
    var activeStatus: FeatureActiveStatus?
) {

    constructor() : this("","","",
        "",
        "","","",
        "","","",
        "","","",
        "", "", "",
        "", "", "",
        "", "", "",
        "", "", "", "",
        mutableListOf(), mutableListOf(), mutableListOf(),null
    )
}
