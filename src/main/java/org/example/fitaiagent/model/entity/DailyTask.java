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
 * 每日打卡任务
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("daily_task")
public class DailyTask implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    private Long id;

    @Column("planId")
    private Long planId;

    @Column("dayIndex")
    private Integer dayIndex;

    @Column("taskDate")
    private LocalDate taskDate;

    @Column("title")
    private String title;

    /**
     * SPORT / DIET / REST / HABIT
     */
    @Column("type")
    private String type;

    /**
     * 0=未完成, 1=已完成
     */
    @Column("status")
    private Integer status;

    @Column("completedAt")
    private LocalDateTime completedAt;
}
