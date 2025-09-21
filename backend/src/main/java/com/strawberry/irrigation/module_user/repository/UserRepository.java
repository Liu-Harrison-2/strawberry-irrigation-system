package com.strawberry.irrigation.module_user.repository;

import com.strawberry.irrigation.module_user.entity.User;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 用户数据访问层（内存实现）
 * 模拟数据库操作，便于学习和测试
 */
@Repository
public class UserRepository {

    // 模拟数据库表，使用线程安全的Map
    private final Map<Long, User> userTable = new ConcurrentHashMap<>();

    // 模拟自增主键
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * 保存用户（新增或更新）
     */
    public User save(User user) {
        if (user.getId() == null) {
            // 新增用户，分配ID
            user.setId(idGenerator.getAndIncrement());
        }
        userTable.put(user.getId(), user);
        return user;
    }

    /**
     * 根据ID查找用户
     */
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(userTable.get(id));
    }

    /**
     * 根据用户名查找用户
     */
    public Optional<User> findByUsername(String username) {
        return userTable.values().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }

    /**
     * 根据手机号查找用户
     */
    public Optional<User> findByPhone(String phone) {
        return userTable.values().stream()
                .filter(user -> user.getPhone().equals(phone))
                .findFirst();
    }

    /**
     * 查询所有用户
     */
    public List<User> findAll() {
        return new ArrayList<>(userTable.values());
    }

    /**
     * 分页查询用户
     */
    public List<User> findPage(int page, int size) {
        return userTable.values().stream()
                .skip((long) (page - 1) * size)
                .limit(size)
                .collect(Collectors.toList());
    }

    /**
     * 统计用户总数
     */
    public long count() {
        return userTable.size();
    }

    /**
     * 删除用户
     */
    public void deleteById(Long id) {
        userTable.remove(id);
    }

    /**
     * 检查用户是否存在
     */
    public boolean existsById(Long id) {
        return userTable.containsKey(id);
    }

    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsername(String username) {
        return userTable.values().stream()
                .anyMatch(user -> user.getUsername().equals(username));
    }

    /**
     * 检查手机号是否存在
     */
    public boolean existsByPhone(String phone) {
        return userTable.values().stream()
                .anyMatch(user -> user.getPhone().equals(phone));
    }
}