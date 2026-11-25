# The plugin implementation is looked up by class name (see AbdmPlugins.kt),
# so it must survive minification in every app that bundles it.
-keep class * implements org.intelehealth.abdm.api.AbdmPlugin { <init>(); }
-keep class org.intelehealth.abdm.api.** { *; }
