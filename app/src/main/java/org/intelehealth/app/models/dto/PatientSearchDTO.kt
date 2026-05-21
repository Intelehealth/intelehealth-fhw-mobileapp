package org.intelehealth.app.models.dto

data class PatientSearchDTO (
    val resourceType: String? = null,
    val id: String? = null,
    val meta: Meta? = null,
    val type: String? = null,
    val total: Int? = 0,
    val entry: List<Entry>? = null
)
data class Entry(
    val resource: Resource? = null,
    val search: Search? = null
)
data class Meta(
    val versionId: String? = null,
    val lastUpdated: String? = null,
    val profile: List<String>? = null,
    val tag: List<Tag>? = null
)
data class Tag(
    val system: String? = null,
    val code: String? = null,
    val display: String? = null
)
data class Resource(
    val id: String? = null,
    val resourceType: String? = null,
    val active: Boolean? = null,
    val meta: Meta? = null,
    val identifier: List<Identifier>? = null,
    val name: List<Name>? = null,
    val telecom: List<Telecom>? = null,
    val gender: String? = null,
    val birthDate: String? = null,
    val deceasedBoolean: Boolean? = null,
    val address: List<Address>? = null,
    val extension: List<Extension>? = null
)
data class Name(
    val text: String? = null,
    val family: String? = null,
    val given: List<String>? = null
) {
    fun getGivenFirst(): String {
        return given?.firstOrNull() ?: ""
    }
}
data class Telecom(
    val system: String? = null,
    val value: String? = null,
    val use: String? = null,
    val rank: Int? = null
)
data class Identifier(
    val use: String? = null,
    val value: String? = null,
    val system: String? = null,
    val id: String? = null,
)
data class Address(
    val use: String? = null,
    val text: String? = null,
    val line: List<String>? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val postalCode: String? = null
)
data class Search(
    val mode: String? = null,
    val score: Double? = null,
    val extension: List<Extension>? = null
)
data class Extension(
    val url: String? = null,
    val valueString: String? = null,
    val valueCode: String? = null
)
