package org.example.fitaiagent.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 30 天健康计划
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("plan")
public class Plan implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    @Column("userId")
    private Long userId;

    @Column("startDate")
    private LocalDate startDate;

    /**
     * 0=进行中, 1=已完成, 2=已作废
     */
    @Column("status")
    private Integer status;

    @Column("createTime")
    private LocalDateTime createTime;
}
