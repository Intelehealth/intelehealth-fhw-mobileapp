package org.intelehealth.app.utilities

import org.intelehealth.app.activities.visit.model.PrescribedMedicineModel

class ParserUtils {
    companion object {
        fun parseMedication(data: String): Any? {
            //possible format
            //1. Acetazolamide: 250mg, Tablet 30 minutes before food (Subcutaneous) 0 - 0 - 1 for 12 days
            //2. Artesunate + Sulphadoxine Pyrimethamine: 250, Tablet 1 - 0 - 0 for 2 days
            //3. Artesunate + Sulphadoxine Pyrimethamine: 250, Tablet 1 - 0 - 0
            val regex = Regex(
                """^([^:]+):\s*([^,]+),\s*(.*?)(?:\s*\(([^)]+)\))?\s*([\d\s-]+)(?:\s*for\s*(.+))?$""",
                RegexOption.IGNORE_CASE
            )
            val match = regex.find(data.trim())
            if(match!=null){
                match.let {
                    val medicine = PrescribedMedicineModel()
                    medicine.medicineName = it.groupValues[1].trim()
                    medicine.strength = it.groupValues[2].trim()
                    medicine.remark = it.groupValues[3].trim()
                    //medicine.route = it.groupValues[4].trim()
                    medicine.timing = it.groupValues[5].trim()
                    medicine.noOfDays = it.groupValues[6].trim()

                    return medicine
                }
            }else{
               return data
            }
        }
    }
}