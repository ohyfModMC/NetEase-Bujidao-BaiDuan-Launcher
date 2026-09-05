/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.apache.logging.log4j.LogManager
 */
package com.netease.mc.mod.helper;

import java.util.ArrayList;
import javax.annotation.Nullable;
import org.apache.logging.log4j.LogManager;

public class CoreModMethodData {
    private String className;
    private String srcMethodName;
    private String methodName;
    private String desc;
    private String oldInterface = null;
    private String tranformerClassName;
    private ArrayList<Integer> paramOpcodelist;
    private int offset = 0;
    private int returnOpcode;

    public CoreModMethodData(String _className, String _srcMethodName, String _methodName, String _desc, String _tranformerClassName) {
        this(_className, _srcMethodName, _methodName, _desc, _tranformerClassName, null);
    }

    public CoreModMethodData(String _className, String _srcMethodName, String _methodName, String _desc, String _tranformerClassName, String _oldInterface) {
        this.className = _className;
        this.srcMethodName = _srcMethodName;
        this.methodName = _methodName;
        this.desc = _desc;
        this.oldInterface = _oldInterface;
        this.tranformerClassName = _tranformerClassName;
        this.initParamOpcodes();
        this.initReturnOpcode();
    }

    public String getClassName() {
        return this.className;
    }

    public String getDescClassName() {
        return this.className.replace('.', '/');
    }

    public String getSrcMethodName() {
        if (this.srcMethodName.isEmpty()) {
            return this.methodName;
        }
        return this.srcMethodName;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public String getDesc() {
        return this.desc;
    }

    @Nullable
    public String getOldInterface() {
        return this.oldInterface;
    }

    public String getTranformerClassName() {
        return this.tranformerClassName;
    }

    public int getReturnOpcode() {
        return this.returnOpcode;
    }

    public ArrayList<Integer> getParamOpcodelist() {
        return this.paramOpcodelist;
    }

    private void initParamOpcodes() {
        String params = this.desc.substring(1, this.desc.indexOf(41));
        this.paramOpcodelist = new ArrayList();
        this.paramOpcodelist.add(25);
        block8: for (int i = 0; i < params.length(); ++i) {
            switch (params.charAt(i)) {
                case 'L': {
                    this.paramOpcodelist.add(25);
                    i = params.indexOf(59, i);
                    continue block8;
                }
                case '[': {
                    this.paramOpcodelist.add(25);
                    while (params.charAt(i) == '[') {
                        ++i;
                    }
                    if (params.charAt(i) != 'L') continue block8;
                    i = params.indexOf(59, i);
                    continue block8;
                }
                case 'D': {
                    this.paramOpcodelist.add(24);
                    continue block8;
                }
                case 'F': {
                    this.paramOpcodelist.add(23);
                    continue block8;
                }
                case 'J': {
                    this.paramOpcodelist.add(22);
                    continue block8;
                }
                case 'B': 
                case 'C': 
                case 'I': 
                case 'S': 
                case 'Z': {
                    this.paramOpcodelist.add(21);
                    continue block8;
                }
                default: {
                    LogManager.getLogger().error("unknown ASM : " + params.charAt(i));
                }
            }
        }
    }

    private void initReturnOpcode() {
        char ret = this.desc.charAt(this.desc.indexOf(41) + 1);
        switch (ret) {
            case 'V': {
                this.returnOpcode = 177;
                break;
            }
            case 'L': 
            case '[': {
                this.returnOpcode = 176;
                break;
            }
            case 'D': {
                this.returnOpcode = 175;
                break;
            }
            case 'F': {
                this.returnOpcode = 174;
                break;
            }
            case 'J': {
                this.returnOpcode = 173;
                break;
            }
            case 'B': 
            case 'C': 
            case 'I': 
            case 'S': 
            case 'Z': {
                this.returnOpcode = 177;
                break;
            }
            default: {
                LogManager.getLogger().error("unknown return ASM : " + ret);
            }
        }
    }
}

