# Types crossing the module boundary to the host as Parcelables.
-keep class org.intelehealth.abdm.result.** { *; }
-keep class org.intelehealth.abdm.config.LocalPatientRecord { *; }
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# AbdmOutcomes and AbhaChoice are resolved by name across the boundary.
-keepclassmembers enum org.intelehealth.abdm.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
