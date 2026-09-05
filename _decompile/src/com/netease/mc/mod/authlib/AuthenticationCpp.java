/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.exceptions.AuthenticationException
 *  com.netease.mc.mod.network.common.Common
 *  com.netease.mc.mod.network.common.Library
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.authlib;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.netease.mc.mod.authlib.ErrorCode;
import com.netease.mc.mod.network.common.Common;
import com.netease.mc.mod.network.common.Library;
import java.io.File;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AuthenticationCpp {
    private static final Logger LOGGER = LogManager.getLogger();

    private Boolean SafeLoadLibrary(String path) {
        try {
            System.load(path);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public Boolean LoadLibrary() throws AuthenticationException {
        try {
            String javaLibPath = System.getProperty("java.library.path");
            File runtime = new File(javaLibPath, "runtime");
            File[] files = runtime.listFiles();
            ArrayList<File> failedFiles = new ArrayList<File>();
            for (int i = 0; i < 10 && (i <= 0 || files.length > 0); ++i) {
                for (File file : files) {
                    if (!file.isFile() || !file.getName().contains("dll") || this.SafeLoadLibrary(file.getPath()).booleanValue()) continue;
                    failedFiles.add(file);
                }
                files = failedFiles.toArray(new File[failedFiles.size()]);
                failedFiles.clear();
                LOGGER.info((Object)files.length);
            }
        }
        catch (Exception e) {
            throw new AuthenticationException(e.getMessage());
        }
        return true;
    }

    public Boolean Authentication(int port, String serverId) throws AuthenticationException {
        try {
            int code = Library.AuthenticationAccessToken((int)port, (String)serverId);
            ErrorCode error = ErrorCode.GetErrorCode(code);
            if (error != ErrorCode.SUCCESS) {
                throw new AuthenticationException(error.getDescription());
            }
            return Boolean.TRUE;
        }
        catch (Exception e) {
            Common.CatchException((Throwable)e);
            throw new AuthenticationException(e.getMessage());
        }
    }

    public static void main(String[] args) throws AuthenticationException {
        AuthenticationCpp test = new AuthenticationCpp();
        String port = System.getProperty("launcherControlPort");
        LOGGER.info(port);
        test.Authentication(Integer.parseInt(port), "");
    }
}

