# Keep rules shipped to the consuming app's R8 run.
# Needed because the host app minifies release builds and abdm's wire models are
# only ever instantiated reflectively by Gson.

# Generic signatures and annotations must survive for Retrofit's return types
# (Response<T>, Map<String, SearchProfileResponseDto>) and Gson's @SerializedName.
-keepattributes Signature, InnerClasses, *Annotation*, EnclosingMethod

# Request/response models: constructed by Gson via reflection, never by name in code.
-keep class org.intelehealth.abdm.data.remote.dto.** { *; }

# Types crossing the module boundary to the host as Parcelables.
-keep class org.intelehealth.abdm.result.** { *; }
-keep class org.intelehealth.abdm.config.LocalPatientRecord { *; }
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# AbdmOutcomes and AbhaChoiceDialogFragment.Choice are resolved by name
# (Choice.valueOf) across the boundary.
-keepclassmembers enum org.intelehealth.abdm.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Retrofit service interfaces are implemented by a runtime proxy.
-keep,allowobfuscation interface org.intelehealth.abdm.data.remote.api.**
