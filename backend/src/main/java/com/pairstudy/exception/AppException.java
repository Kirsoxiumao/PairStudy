package com.pairstudy.exception;
public class AppException extends RuntimeException {
    public final int code;
    public AppException(int code, String message) { super(message); this.code=code; }
}
