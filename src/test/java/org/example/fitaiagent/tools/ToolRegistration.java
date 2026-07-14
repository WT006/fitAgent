package org.example.fitaiagent.tools;

/**
 * 测试侧不再单独注册 allTools，避免与主配置冲突；
 * PDF 工具通过主工程 {@link ToolRegistration} + Spring 注入使用。
 */
public class ToolRegistration {
}
