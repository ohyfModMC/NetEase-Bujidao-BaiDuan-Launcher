/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 *  org.objectweb.asm.tree.ClassNode
 *  org.objectweb.asm.tree.MethodNode
 */
package com.netease.mc.mod.helper;

import com.netease.mc.mod.helper.CoreModClassData;
import com.netease.mc.mod.helper.CoreModMethodData;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class CoreModManager {
    private static final Logger logger = LogManager.getLogger();
    private static Map<String, CoreModClassData> classDataList = new HashMap<String, CoreModClassData>();
    private static HashMap<Integer, Integer> Opcode2Size = new HashMap<Integer, Integer>(){
        {
            this.put(25, 1);
            this.put(21, 1);
            this.put(23, 1);
            this.put(24, 2);
            this.put(22, 2);
        }
    };

    public static ArrayList<CoreModClassData> getCoreModClassDataList() {
        return new ArrayList<CoreModClassData>(classDataList.values());
    }

    public static CoreModClassData getClassData(String name) {
        return classDataList.getOrDefault(name.replace('/', '.'), null);
    }

    private static void AddCoreModMethodData(CoreModMethodData data) {
        CoreModClassData newClassData = classDataList.getOrDefault(data.getClassName(), null);
        if (newClassData == null) {
            newClassData = new CoreModClassData(data.getClassName());
        }
        newClassData.AddCoreModMethodData(data);
        classDataList.put(data.getClassName(), newClassData);
        LogManager.getLogger().info("getCoreModClassDataList size:" + CoreModManager.getCoreModClassDataList().size());
    }

    public static void AddCoreModMethodData(String _className, String _srcMethodName, String _methodName, String _desc, String _tranformerClassName, String _oldInterface) {
        CoreModMethodData data = new CoreModMethodData(_className, _srcMethodName, _methodName, _desc, _tranformerClassName, _oldInterface);
        CoreModManager.AddCoreModMethodData(data);
    }

    private static MethodNode getMethod(ClassNode classNode, String methodName, String methodDescriptor) {
        String targetMethodName;
        List methods = classNode.methods;
        try {
            Class<?> asmapi = Class.forName("net.minecraftforge.coremod.api.ASMAPI");
            Method mapMethod = asmapi.getDeclaredMethod("mapMethod", String.class);
            targetMethodName = (String)mapMethod.invoke(null, methodName);
        }
        catch (Throwable e) {
            logger.error("getMethod Failed :" + classNode.name + " " + methodName + " " + methodDescriptor);
            return null;
        }
        for (MethodNode method : methods) {
            if (!method.name.equals(targetMethodName) || !method.desc.equals(methodDescriptor)) continue;
            logger.info("Matched method " + method.name + " " + method.desc);
            return method;
        }
        logger.error("Coremod Fatal Error: Method not found: " + methodName);
        for (MethodNode method : methods) {
            logger.error("Iterate method " + method.name + " " + method.desc);
        }
        return null;
    }

    public static void transformCommon(ClassNode classNode, CoreModMethodData coreModMethodData) {
        logger.info("start  utilcoremod: " + coreModMethodData.getMethodName());
        MethodNode method = CoreModManager.getMethod(classNode, coreModMethodData.getSrcMethodName(), coreModMethodData.getDesc());
        if (method == null) {
            logger.error("utilcoremod : dont find method  : " + coreModMethodData.getMethodName());
            return;
        }
        MethodNode newMethod = new MethodNode(393216);
        newMethod.access = method.access;
        newMethod.name = method.name;
        newMethod.desc = method.desc;
        newMethod.signature = null;
        newMethod.exceptions = method.exceptions;
        method.name = coreModMethodData.getMethodName() + "Old";
        method.access = 1;
        ArrayList<Integer> paramOpcodelist = coreModMethodData.getParamOpcodelist();
        int offset = 0;
        for (Integer Opcode : paramOpcodelist) {
            int size = Opcode2Size.getOrDefault(Opcode, 0);
            if (size > 0) {
                newMethod.visitVarInsn(Opcode.intValue(), offset);
                offset += size;
                continue;
            }
            logger.error("transfer utilcoremod failed: unknown opcode" + Opcode);
        }
        String oldInterface = coreModMethodData.getOldInterface();
        if (oldInterface != null) {
            newMethod.visitMethodInsn(184, coreModMethodData.getTranformerClassName().replace(".", "/"), coreModMethodData.getMethodName() + "Wrapper", "(L" + coreModMethodData.getOldInterface().replace(".", "/") + ";" + coreModMethodData.getDesc().substring(1), false);
        } else {
            newMethod.visitMethodInsn(184, coreModMethodData.getTranformerClassName().replace(".", "/"), coreModMethodData.getMethodName() + "Wrapper", "(L" + coreModMethodData.getClassName().replace(".", "/") + ";" + coreModMethodData.getDesc().substring(1), false);
        }
        newMethod.visitInsn(coreModMethodData.getReturnOpcode());
        classNode.methods.add(newMethod);
        if (oldInterface != null && !classNode.interfaces.contains(oldInterface.replace(".", "/"))) {
            classNode.interfaces.add(oldInterface.replace(".", "/"));
        }
        logger.info("set new method success utilcoremod: " + newMethod.name);
    }
}

