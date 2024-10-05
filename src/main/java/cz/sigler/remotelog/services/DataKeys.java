package cz.sigler.remotelog.services;

import com.intellij.openapi.actionSystem.DataKey;

//For some reason, when implemented as kotlin object, the plugin fails
// to initialize in some Idea versions due to not being able to find the Companion object.
// Possibly a side effect of idea's dependency injection framework.
public class DataKeys {

    public static DataKey<String> LOG_SOURCE_ID = DataKey.create("RemoteLogConsole_SourceId");

    private DataKeys() {
    }
}
