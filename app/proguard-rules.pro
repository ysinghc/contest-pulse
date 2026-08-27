# Room DB rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Gson & Model rules
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private static final java.io.ObjectStreamClass* ;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-keep class org.iiitu.acm.contestpulse.data.model.** { *; }

# OkHttp rules
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# WorkManager
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
