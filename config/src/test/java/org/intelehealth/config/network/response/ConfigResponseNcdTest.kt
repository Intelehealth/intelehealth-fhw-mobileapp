package org.intelehealth.config.network.response

import com.google.gson.Gson
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// The published config's ncd_section switch, read the way the config sync reads it
class ConfigResponseNcdTest {

    private fun parse(json: String) = Gson().fromJson(json, ConfigResponse::class.java)

    @Test
    fun ncdOnWhenPublishedOn() {
        assertTrue(parse("""{"ncd_section": true, "version": 1}""").ncdSection)
    }

    @Test
    fun ncdOffWhenPublishedOff() {
        assertFalse(parse("""{"ncd_section": false, "version": 1}""").ncdSection)
    }

    @Test
    fun ncdOffWhenServerHasNoSetting() {
        assertFalse(parse("""{"abha_section": true, "version": 1}""").ncdSection)
    }
}
