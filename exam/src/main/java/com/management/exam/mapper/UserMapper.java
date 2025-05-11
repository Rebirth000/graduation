package com.management.exam.mapper;

import com.management.exam.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {

    /**
     * 分页查询管理员列表
     */
    List<User> findAdminsByPage(@Param("offset") int offset, @Param("size") int size);

    /**
     * 统计管理员总数
     */
    long countAdmins();

    /**
     * 根据ID查询用户
     */
    Optional<User> findById(@Param("id") Integer id);

    /**
     * 根据用户名查询用户
     */
    Optional<User> findByUsername(@Param("username") String username);

    List<User> findAllAdmins();

    /**
     * 插入用户
     */
    int insert(User user);

    /**
     * 更新用户
     */
    int update(User user);

    /**
     * 删除用户
     */
    int deleteById(@Param("id") Integer id);
} 