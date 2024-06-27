package org.intelehealth.app.utilities

import com.google.gson.Gson
import org.intelehealth.app.activities.identificationActivity.model.Block
import org.intelehealth.app.activities.identificationActivity.model.DistData
import org.intelehealth.app.activities.identificationActivity.model.GramPanchayat
import org.intelehealth.app.activities.identificationActivity.model.StateData
import org.intelehealth.app.activities.identificationActivity.model.StateDistMaster
import org.intelehealth.app.activities.identificationActivity.model.Village
import org.intelehealth.app.app.IntelehealthApplication

/**
 * Created by Vaghela Mithun R. on 26-06-2024 - 20:19.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
object LanguageUtils {
    private const val STATE_DISTRICT_JSON = "state_district_tehsil.json"

    @JvmStatic
    fun getLocalLang(): String {
        val context = IntelehealthApplication.getAppContext()
        return SessionManager(context).appLanguage
    }

    @JvmStatic
    fun getState(state: String): StateData? {
        val context = IntelehealthApplication.getAppContext()
        val jsonObject = FileUtils.encodeJSON(context, STATE_DISTRICT_JSON)
        val stateDistMaster = Gson().fromJson(
            jsonObject.toString(),
            StateDistMaster::class.java
        )

        return stateDistMaster.stateDataList.find { it.state == state }
    }

    @JvmStatic
    fun getDistrict(state: StateData?, district: String): DistData? {
        return state?.distDataList?.find { it.name == district }
    }

    @JvmStatic
    fun getBlock(district: DistData?, block: String): Block? {
        return district?.blocks?.find { it.name == block }
    }

    @JvmStatic
    fun getGramPanchayat(block: Block?, gramPanchayat: String): GramPanchayat? {
        return block?.gramPanchayats?.find { it.name == gramPanchayat }
    }

    @JvmStatic
    fun getVillage(gramPanchayat: GramPanchayat?, village: String): Village? {
        return gramPanchayat?.villages?.find { it.name == village }
    }

    @JvmStatic
    fun getStateLocal(state: StateData): String {
        if (getLocalLang().equals("hi")) return state.stateHindi
        return state.state
    }

    @JvmStatic
    fun getDistrictLocal(district: DistData): String {
        if (getLocalLang().equals("hi")) return district.nameHindi
        return district.name
    }

    @JvmStatic
    fun getBlockLocal(block: Block): String {
        if (getLocalLang().equals("hi")) return block.nameHindi ?: block.name ?: ""
        return block.name ?: ""
    }

    @JvmStatic
    fun getGramPanchayatLocal(gramPanchayat: GramPanchayat): String {
        if (getLocalLang().equals("hi")) return gramPanchayat.nameHindi ?: gramPanchayat.name ?: ""
        return gramPanchayat.name ?: ""
    }

    @JvmStatic
    fun getVillageLocal(village: Village): String {
        if (getLocalLang().equals("hi")) return village.nameHindi ?: village.name ?: ""
        return village.name ?: ""
    }
}