/*
 * Decompiled with CFR 0.152.
 */
package com.netease.mc.mod.helper;

import com.netease.mc.mod.helper.CoreModMethodData;
import java.util.ArrayList;

public class CoreModClassData {
    private String className;
    private ArrayList<CoreModMethodData> methodList = new ArrayList();

    public CoreModClassData(String name) {
        this.className = name;
    }

    public void AddCoreModMethodData(CoreModMethodData data) {
        for (CoreModMethodData methodData : this.methodList) {
            if (!methodData.getMethodName().equals(data.getMethodName()) || !methodData.getDesc().equals(data.getDesc())) continue;
            return;
        }
        this.methodList.add(data);
    }

    public String getClassName() {
        return this.className;
    }

    public ArrayList<CoreModMethodData> getCoreModMethodDataList() {
        return this.methodList;
    }
}

