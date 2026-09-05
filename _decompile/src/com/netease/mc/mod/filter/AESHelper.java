/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.filter;

import java.security.Key;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AESHelper {
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/CBC/PKCS5PADDING";
    public static final Logger logger = LogManager.getLogger(AESHelper.class);

    public static byte[] Decrypt(byte[] data, String aesStr) {
        if (null == data) {
            return data;
        }
        byte[] keybyte = aesStr.getBytes();
        if (keybyte == null || keybyte.length != 32) {
            return data;
        }
        byte[] bytePre = Arrays.copyOfRange(keybyte, 0, 16);
        byte[] bytePost = Arrays.copyOfRange(keybyte, 16, 32);
        SecretKeySpec key = new SecretKeySpec(bytePre, "AES");
        IvParameterSpec ivparameter = new IvParameterSpec(bytePost);
        byte[] byte_decode = null;
        try {
            Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
            cipher.init(2, (Key)key, ivparameter);
            byte_decode = cipher.doFinal(Arrays.copyOfRange(data, 0, data.length));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return byte_decode;
    }
}

