package org.example.fitaiagent.agent;

import com.itextpdf.styledxmlparser.jsoup.internal.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.fitaiagent.agent.model.AgentState;
import org.example.fitaiagent.agent.model.StreamResponse;
import org.example.fitaiagent.tools.AskHumanTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


@Data
@Slf4j
public abstract class BaseAgent {

    //核心属性
    private String name;

    //提示词
    private String systemPrompt;
    private String nextStepPrompt;

    //状态
    private AgentState state = AgentState.IDLE;

    //执行控制
    private int maxStep = 10;
    private int currentStep = 0;

    //LLM
    private ChatClient chatClient;

    //聊天记录
    private List<Message> messageList = new ArrayList<>();

    // 新增：暂停控制
    private CountDownLatch pauseLatch;
    private String pendingHumanResponse;
    private String pendingHumanQuestion;

    /**
     * 执行单个步骤
     * @return
     */
    public abstract String step();

    /**
     * 清理数据
     */
    public void clean(){
        this.pendingHumanResponse = null;
        this.pendingHumanQuestion = null;
        this.pauseLatch = null;
    }

    /**
     * 暂停执行，等待人类回复
     */
    public void pauseForHumanInput(String question) {
        this.pendingHumanQuestion = question;
        this.state = AgentState.WAITING_HUMAN;
        this.pauseLatch = new CountDownLatch(1);

        log.info("Agent 暂停，等待人类回复问题: {}", question);
    }

    /**
     * 恢复执行
     */
    public synchronized void resume(String humanResponse) {
        if (this.state != AgentState.WAITING_HUMAN) {
            throw new IllegalStateException("Agent 当前不在等待人类输入的状态");
        }

        this.pendingHumanResponse = humanResponse;
        this.state = AgentState.RUNNING;

        // 将人类回复作为新的用户消息添加到消息列表
        this.messageList.add(new UserMessage(humanResponse));

        // 释放锁，继续执行
        this.pauseLatch.countDown();

        log.info("Agent 恢复执行，人类回复: {}", humanResponse);
    }


    /**
     * 运行
     * @param userPrompt
     * @return
     */
    public String run(String userPrompt){
        if(state != AgentState.IDLE){
            throw new RuntimeException("Cannot run agent from state:"+ this.state);
        }
        if(StringUtil.isBlank(userPrompt)){
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }
        //更改运行状态
        this.state = AgentState.RUNNING;
        //保存记忆
        messageList.add(new UserMessage(userPrompt));
        //保存结果列表
        List<String> resultList = new ArrayList<>();

        // 设置当前 Agent 到 ThreadLocal
        AskHumanTool.setCurrentAgent(this);

        try{
            while(this.currentStep < this.maxStep && this.state != AgentState.FINISHED){
                this.currentStep++;
                log.info("Running step:"+this.currentStep);
                //单步执行
                String stepResult = step();
                String result ="step"+currentStep+": "+ stepResult;
                resultList.add(result);

                // 检查是否需要等待人类输入
                if (this.state == AgentState.WAITING_HUMAN) {
                    try {
                        // 等待人类回复，最多等待 10 分钟
                        boolean resumed = this.pauseLatch.await(10, TimeUnit.MINUTES);
                        if (!resumed) {
                            return "等待超时，人类未回复";
                        }
                        // 继续循环执行下一步
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return "执行被中断";
                    }
                }
            }

            if(currentStep>=maxStep){
                this.state = AgentState.FINISHED;
                log.info("Terminated: Reached max step("+maxStep+")");
            }
            return String.join("\n",resultList);
        }catch (Exception e){
            state = AgentState.ERROR;
            log.error("Error running agent",e);
            return "执行错误:"+e.getMessage();
        }finally {
            this.clean();
            // 清除 ThreadLocal
            AskHumanTool.clearCurrentAgent();
        }


    }



    // ... existing code ...
    public SseEmitter runStream(String userPrompt, String chatId){

        SseEmitter sseEmitter = new SseEmitter(300000L);

        CompletableFuture.runAsync(()->{
            AskHumanTool.setCurrentAgent(this);
            try {
                if(state != AgentState.IDLE){
                    sendSseEvent(sseEmitter, StreamResponse.builder()
                            .type("error")
                            .content("错误，无法从状态运行代理: " + this.state)
                            .chatId(chatId)
                            .state(this.state)
                            .build());
                    sseEmitter.complete();
                    return;
                }
                if(StringUtil.isBlank(userPrompt)){
                    sendSseEvent(sseEmitter, StreamResponse.builder()
                            .type("error")
                            .content("错误，无法从空用户提示运行代理")
                            .chatId(chatId)
                            .state(this.state)
                            .build());
                    sseEmitter.complete();
                    return;
                }
            }catch (Exception e){
                sseEmitter.completeWithError(e);
            }

            this.state = AgentState.RUNNING;
            messageList.add(new UserMessage(userPrompt));

            try{
                while(this.currentStep < this.maxStep && this.state != AgentState.FINISHED){
                    this.currentStep++;
                    log.info("Running step:" + this.currentStep);

                    String stepResult = step();

                    sendSseEvent(sseEmitter, StreamResponse.builder()
                            .type("step")
                            .content(stepResult)
                            .chatId(chatId)
                            .state(this.state)
                            .step(this.currentStep)
                            .build());

                    if (this.state == AgentState.WAITING_HUMAN) {
                        sendSseEvent(sseEmitter, StreamResponse.builder()
                                .type("waiting_human")
                                .content(this.pendingHumanQuestion)
                                .chatId(chatId)
                                .state(this.state)
                                .step(this.currentStep)
                                .build());

                        try {
                            boolean resumed = this.pauseLatch.await(5, TimeUnit.MINUTES);
                            if (!resumed) {
                                sendSseEvent(sseEmitter, StreamResponse.builder()
                                        .type("timeout")
                                        .content("等待超时，人类未回复")
                                        .chatId(chatId)
                                        .state(this.state)
                                        .step(this.currentStep)
                                        .build());
                                break;
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            sendSseEvent(sseEmitter, StreamResponse.builder()
                                    .type("interrupted")
                                    .content("执行被中断")
                                    .chatId(chatId)
                                    .state(this.state)
                                    .step(this.currentStep)
                                    .build());
                            break;
                        }
                    }
                }

                if(currentStep >= maxStep){
                    this.state = AgentState.FINISHED;
                    sendSseEvent(sseEmitter, StreamResponse.builder()
                            .type("max_step_reached")
                            .content("已终止：已达到最大步骤数(" + maxStep + ")")
                            .chatId(chatId)
                            .state(this.state)
                            .step(this.currentStep)
                            .build());
                }

                sendSseEvent(sseEmitter, StreamResponse.builder()
                        .type("finished")
                        .content("任务完成")
                        .chatId(chatId)
                        .state(this.state)
                        .step(this.currentStep)
                        .build());
                sseEmitter.complete();
            }catch (Exception e){
                state = AgentState.ERROR;
                log.error("Error running agent",e);
                try {
                    sendSseEvent(sseEmitter, StreamResponse.builder()
                            .type("error")
                            .content("执行错误:" + e.getMessage())
                            .chatId(chatId)
                            .state(this.state)
                            .step(this.currentStep)
                            .build());
                    sseEmitter.complete();
                } catch (IOException ex) {
                    sseEmitter.completeWithError(ex);
                }
            }finally {
                this.clean();
                AskHumanTool.clearCurrentAgent();
            }
        });

        sseEmitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.clean();
            log.info("SSE connection Timeout");
        });

        sseEmitter.onCompletion(() -> {
            if(state == AgentState.RUNNING){
                this.state = AgentState.FINISHED;
            }
            this.clean();
            log.info("SSE connection Completed");
        });

        return sseEmitter;
    }

    private void sendSseEvent(SseEmitter sseEmitter, StreamResponse response) throws IOException {
        sseEmitter.send(SseEmitter.event()
                .name(response.getType())
                .data(response));
    }

// ... existing code ...


}
