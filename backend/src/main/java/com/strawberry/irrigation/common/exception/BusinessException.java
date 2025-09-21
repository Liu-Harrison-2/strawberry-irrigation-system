package com.strawberry.irrigation.common.exception;

import lombok.Getter;

/**
 * 简单的业务异常承载 code/message，便于统一返回
 */
@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    /**
     * 构造函数：传入错误码和错误信息
     * @param code 错误码
     * @param message 错误信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造函数：只传错误信息，默认 code=400
     * @param message 错误信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }
}