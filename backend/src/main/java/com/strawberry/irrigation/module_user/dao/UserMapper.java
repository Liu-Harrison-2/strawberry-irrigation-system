package com.strawberry.irrigation.module_user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.strawberry.irrigation.module_user.entity.User;
import org.apache.ibatis.annotations.Mapper;


/**
 * 用户数据访问层
 * 使用MyBatis-Plus BaseMapper，充分利用MP的自动CRUD功能
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 继承BaseMapper即可获得完整的CRUD功能
    // 复杂查询使用Service层的QueryWrapper实现
}