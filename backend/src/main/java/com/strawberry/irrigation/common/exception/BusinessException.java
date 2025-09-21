package com.strawberry.irrigation.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 简单的业务异常承载 code/message，便于统一返回
 */
@Getter
@AllArgsConstructor
public class BusinessException extends RuntimeException {
    private final int code;

    // Lombok 会帮生成这个构造方法：
    // public BusinessException(int code, String message) { super(message); this.code = code; }

    // 额外保留一个只传 message 的构造方法，默认 code=400
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }
}
