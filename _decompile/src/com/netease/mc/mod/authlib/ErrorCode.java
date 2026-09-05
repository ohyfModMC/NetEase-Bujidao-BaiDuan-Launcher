/*
 * Decompiled with CFR 0.152.
 */
package com.netease.mc.mod.authlib;

enum ErrorCode {
    SUCCESS(0, "successfully authentication"),
    UNkNOWN_ERROR(-1, "unknown error in authentication"),
    SOCKET_FAILED(1, "Create socket failed"),
    CONNECT_FAILED(2, "Connect failed"),
    SEND_DATA_ERROR(3, "Send data Error!"),
    AUTHENTICATION_FAILED(4, "Authentication response failed"),
    URL_ERROR(5, "URL format failed"),
    CREATE_SSL_ERROR(6, "InitializeSslContext failed"),
    CONNECT_SSL_ERROR(7, "SslConnect failed");

    private final int code;
    private final String description;

    private ErrorCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public int getCode() {
        return this.code;
    }

    public static ErrorCode GetErrorCode(int code) {
        for (ErrorCode error : ErrorCode.values()) {
            if (error.getCode() != code) continue;
            return error;
        }
        return UNkNOWN_ERROR;
    }
}

