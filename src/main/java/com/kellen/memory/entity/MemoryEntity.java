package com.kellen.memory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.Version;
import com.kellen.entity.TenantEntityBase;
import lombok.Getter;
import lombok.Setter;

/**
 * ai-memory 七张表共用的实体基类：字符串雪花主键与乐观锁版本号；审计、逻辑删除与租户字段继承自 {@link TenantEntityBase}。
 */
@Getter
@Setter
public abstract class MemoryEntity extends TenantEntityBase {

    /**
     * 主键，字符串雪花ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * MyBatis-Plus 乐观锁版本号。
     */
    @Version
    private Integer version;
}
