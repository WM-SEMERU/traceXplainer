package io.jenkins.plugins.traceability.helper;

import java.io.File;

public class PermissionHelper {

    public static boolean canExecute(String file){
        return canExecute(new File(file));
    }

    private static boolean canExecute(File file) {
        return file.canExecute();
    }

    public static boolean canRead(String file){
        return canRead(new File(file));
    }

    private static boolean canRead(File file) {
        return file.canRead();
    }

    public static boolean canWrite(String file){
        return canWrite(new File(file));
    }

    private static boolean canWrite(File file) {
        return file.canWrite();
    }
}
