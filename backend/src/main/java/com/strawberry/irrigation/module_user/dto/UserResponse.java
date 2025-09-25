package com.strawberry.irrigation.module_user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户响应DTO
 * 用于向前端返回用户信息（不包含敏感信息）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String userType;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String remark;

    // 从User实体转换为Response DTO的构造方法
    public UserResponse(com.strawberry.irrigation.module_user.entity.User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.realName = user.getRealName();
        this.phone = user.getPhoneNumber();
        this.email = user.getEmail();
        this.userType = user.getUserType();
        this.status = user.getStatus();
        this.createTime = user.getCreateTime();
        this.updateTime = user.getUpdateTime();
        this.remark = user.getRemark();
    }
}