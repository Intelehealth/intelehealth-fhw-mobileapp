package org.intelehealth.app.utilities

/**
 * The ABHA profile carries a single free-text address, while tbl_patient stores it across columns.
 * Used both when prefilling registration from a verified profile and when saving a record merged on
 * the ABHA compare screen, so the split is defined once here.
 *
 * address2, address3 and address6 are deliberately absent: the ABHA string has no reliable
 * equivalent for them, so callers leave those columns untouched rather than overwriting local data
 * with a positional guess.
 */
data class BifurcatedAbhaAddress(
    val address1: String,
    val cityVillage: String,
    val countyDistrict: String,
    val stateProvince: String,
)

/**
 * Splits "addr…, city, district, state" from the tail, since the trailing three components are the
 * predictable ones. Anything shorter than three parts is treated as a single unstructured line.
 */
fun bifurcateAbhaAddress(address: String?): BifurcatedAbhaAddress {
    val raw = address.orEmpty()
    val parts = raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    if (parts.size < 3) {
        return BifurcatedAbhaAddress(
            address1 = raw,
            cityVillage = "",
            countyDistrict = "",
            stateProvince = "",
        )
    }
    return BifurcatedAbhaAddress(
        address1 = parts.dropLast(3).joinToString(", "),
        cityVillage = parts[parts.size - 3],
        countyDistrict = parts[parts.size - 2],
        stateProvince = parts.last(),
    )
}
