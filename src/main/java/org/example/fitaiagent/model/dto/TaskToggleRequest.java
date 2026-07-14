package org.example.fitaiagent.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 切换任务完成状态请求
 */
@Data
public class TaskToggleRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long taskId;
}
