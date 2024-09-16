package org.intelehealth.abdm.data.network

object ErrorMessage {
    const val MESSAGE_500: String =
        "Oops! Something went wrong on our end. Our team has been notified, and we're working to fix it as soon as possible."
    const val MESSAGE_501: String =
        "We're sorry, but this feature isn't available right now. We're working to add it soon!"
    const val MESSAGE_502: String =
        "Uh-oh! It seems there's a hiccup connecting to our servers. Please try again in a few moments."
    const val MESSAGE_503: String =
        "Our servers are currently overloaded or undergoing maintenance. "
    const val MESSAGE_504: String =
        "It looks like our servers are taking too long to respond. Please check your internet connection and try again."
    const val MESSAGE_505: String =
        "We're sorry, but the version of the website you're trying to access isn't supported. Please make sure you're using the latest version of your browser."
    const val UNEXPECTED_ERROR: String = "An unexpected error occurred"
    const val JSON_PARSE_EXCEPTION: String = "JSON parsing error"
    const val IO_EXCEPTION: String = "Couldn't reach server. Check your internet connection."
    const val DEFAULT_MESSAGE: String = "Something went wrong"

}