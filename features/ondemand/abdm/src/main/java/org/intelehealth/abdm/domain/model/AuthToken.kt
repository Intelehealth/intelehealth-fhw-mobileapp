package org.intelehealth.abdm.domain.model

/**The wrapper model for TokenDto. It uses only those properties from TokenDto which are
 * actually used in the app*/
internal data class AuthToken(
    val accessToken: String,
    val expiresInSeconds: Int,
    val refreshToken: String?
)