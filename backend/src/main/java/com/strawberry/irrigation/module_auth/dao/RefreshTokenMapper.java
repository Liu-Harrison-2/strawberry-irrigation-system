package com.strawberry.irrigation.module_auth.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.strawberry.irrigation.module_auth.entity.RefreshToken;

/**
 * 刷新令牌数据访问层
 * 
 * 通俗解释：这就像"档案管理员"，专门负责刷新令牌的存取、查找、更新等档案操作
 * 专业解释：基于MyBatis-Plus的数据访问层接口，提供刷新令牌的CRUD操作
 * 项目中怎么用：Service层通过这个接口和QueryWrapper操作refresh_tokens表，实现令牌的持久化管理
 * 
 * 设计理念：
 * - 与用户管理模块保持一致，使用MyBatis-Plus的标准方式
 * - 继承BaseMapper即可获得完整的CRUD功能
 * - 复杂查询使用Service层的QueryWrapper实现，保持代码一致性
 * - 移除自定义SQL注解，降低维护复杂度
 */
public interface RefreshTokenMapper extends BaseMapper<RefreshToken> {
    // 继承BaseMapper即可获得完整的CRUD功能
    // 复杂查询使用Service层的QueryWrapper实现，与用户管理模块保持一致
}