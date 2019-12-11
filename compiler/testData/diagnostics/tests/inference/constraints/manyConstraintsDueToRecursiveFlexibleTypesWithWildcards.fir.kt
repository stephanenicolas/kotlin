// !WITH_NEW_INFERENCE
// SKIP_JAVAC

// FILE: MySettings.java

import java.util.Collection

class MySettings<
        SS extends MySettings<SS, PS, L>,
        PS extends MyComparableSettings,
        L extends MySettingsListener<PS>
    >
{
    public Collection<PS> getLinkedProjectsSettings() {
        return null;
    }

    public static MySettings<?, ?, ?> getSettings() {
        return null;
    }
}

abstract class MyComparableSettings implements Comparable<MyComparableSettings> {}
abstract class MySettingsListener<S extends MyComparableSettings> {}

// FILE: test.kt

fun test() {
    val a = MySettings.getSettings()
    a.getLinkedProjectsSettings()
    a.linkedProjectsSettings
}