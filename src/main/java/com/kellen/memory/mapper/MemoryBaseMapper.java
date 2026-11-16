package com.kellen.memory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kellen.entity.EntityBase;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Optional;

/**
 * ai-memory MyBatis-Plus Mapper 基础接口。
 *
 * @param <T> 实体类型。
 */
public interface MemoryBaseMapper<T extends EntityBase> extends BaseMapper<T> {

    /**
     * 插入或按主键更新实体。
     *
     * @param entity 实体
     * @return 保存后的实体
     */
    default T save(T entity) {
        if (!StringUtils.hasText(entity.getId())) {
            insert(entity);
            return entity;
        }
        updateById(entity);
        return entity;
    }

    /**
     * 按主键查询实体。
     *
     * @param id 主键
     * @return 匹配实体
     */
    default Optional<T> findById(Serializable id) {
        return Optional.ofNullable(selectById(id));
    }
}
