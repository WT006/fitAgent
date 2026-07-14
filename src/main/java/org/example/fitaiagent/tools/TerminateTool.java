package org.example.fitaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 终结工具（显式带 JSON 参数字段，避免 DashScope 对空 function.arguments 报 400）
 */
public class TerminateTool {

    @Tool(description = """
            Terminate the interaction when the request is met OR if the assistant cannot proceed further with the task.
            When you have finished all the tasks, call this tool to end the work.
            """)
    public String doTerminate(
            @ToolParam(description = "Brief reason for termination, e.g. task completed") String reason) {
        return "任务结束";
    }
}
