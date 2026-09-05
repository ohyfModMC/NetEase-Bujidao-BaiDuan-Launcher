/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  cpw.mods.modlauncher.ArgumentHandler
 *  cpw.mods.modlauncher.Launcher
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 *  org.jetbrains.annotations.Nullable
 */
package com.netease.mc.mod.helper;

import com.google.gson.Gson;
import cpw.mods.modlauncher.ArgumentHandler;
import cpw.mods.modlauncher.Launcher;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ReflectionHelper {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final boolean Debug = false;
    public static final boolean Studio = false;
    public static UserPropertiesEx userPropertiesEx = null;

    @Nullable
    public static Object GetField(Class targetClass, Object target, String fieldName) {
        try {
            Field[] field = targetClass.getDeclaredFields();
            for (int j = 0; j < field.length; ++j) {
                String name = field[j].getName();
                if (!fieldName.isEmpty() && !name.equals(fieldName)) continue;
                field[j].setAccessible(true);
                Object obj = field[j].get(target);
                return obj;
            }
        }
        catch (Exception e) {
            LOGGER.error("GetField", (Throwable)e);
        }
        return null;
    }

    public static Object GetField(Class targetClass, Object target, Type fieldType) {
        try {
            Field[] field = targetClass.getDeclaredFields();
            for (int j = 0; j < field.length; ++j) {
                field[j].setAccessible(true);
                Object obj = field[j].get(target);
                if (obj.getClass() != fieldType) continue;
                return obj;
            }
        }
        catch (Exception e) {
            LOGGER.error("GetField", (Throwable)e);
        }
        return null;
    }

    public static void SetField(Class targetClass, Object target, String fieldName, Object value) {
        try {
            Field[] field = targetClass.getDeclaredFields();
            for (int j = 0; j < field.length; ++j) {
                String name = field[j].getName();
                if (!fieldName.isEmpty() && !name.equals(fieldName)) continue;
                field[j].setAccessible(true);
                field[j].set(target, value);
            }
        }
        catch (Exception e) {
            LOGGER.error("SetField", (Throwable)e);
        }
    }

    public static Method GetMethod(Class targetClass, String name) {
        try {
            Method[] methods = targetClass.getDeclaredMethods();
            for (int j = 0; j < methods.length; ++j) {
                methods[j].setAccessible(true);
                if (!methods[j].getName().equals(name)) continue;
                return methods[j];
            }
        }
        catch (Exception e) {
            LOGGER.error("GetField", (Throwable)e);
        }
        return null;
    }

    public static Object Invoke(Object target, String name, Object ... args) {
        try {
            Method method = ReflectionHelper.GetMethod(target.getClass(), name);
            if (null == method) {
                LOGGER.error(String.format("Method %s not found in class %s", name, target.getClass().toString()));
                return null;
            }
            return method.invoke(target, args);
        }
        catch (Exception e) {
            LOGGER.error("Invoke", (Throwable)e);
            return null;
        }
    }

    public static String[] GetCmdArgs() {
        ArgumentHandler argHandler = (ArgumentHandler)ReflectionHelper.GetField(Launcher.class, (Object)Launcher.INSTANCE, "argumentHandler");
        if (argHandler != null) {
            return argHandler.buildArgumentList();
        }
        return null;
    }

    public static String GetChannel() {
        if (userPropertiesEx == null) {
            String[] args = ReflectionHelper.GetCmdArgs();
            if (args != null) {
                for (int i = 0; i < args.length; ++i) {
                    if (!args[i].equals("--userPropertiesEx")) continue;
                    Gson gson = new Gson();
                    userPropertiesEx = (UserPropertiesEx)gson.fromJson(args[i + 1], UserPropertiesEx.class);
                    break;
                }
            }
            if (userPropertiesEx == null) {
                return "netease";
            }
        }
        return ReflectionHelper.userPropertiesEx.channel;
    }

    public class UserPropertiesEx {
        public int GameType = 0;
        public boolean isFilter = false;
        public String channel = "netease";
    }
}

