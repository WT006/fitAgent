package org.example.fitaiagent.agent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamResponse {
    private String type;
    private String content;
    private String chatId;
    private AgentState state;
    private Integer step;
}
