package org.example.fitaiagent.agent;

import com.itextpdf.styledxmlparser.jsoup.internal.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.fitaiagent.agent.model.AgentState;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

    /**
     * 执行单个步骤
     * @return
     */
    public abstract String step();

    /**
     * 清理数据
     */
    protected void clean(){
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

        try{
            while(this.currentStep < this.maxStep && this.state != AgentState.FINISHED){
                this.currentStep++;
                log.info("Running step:"+this.currentStep);
                //单步执行
                String stepResult = step();
                String result ="step"+currentStep+": "+ stepResult;
                resultList.add(result);
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
        }

    }



    public SseEmitter runStream(String userPrompt){

        SseEmitter sseEmitter = new SseEmitter(300000L);
        //使方法异步执行
        CompletableFuture.runAsync(()->{
            try {
                if(state != AgentState.IDLE){
                    sseEmitter.send("错误，无法从状态运行代理"+this.state);
                    sseEmitter.complete();
                    return;
                }
                if(StringUtil.isBlank(userPrompt)){
                    sseEmitter.send("错误，无法从空用户提示运行代理");
                    sseEmitter.complete();
                    return;
                }
            }catch (Exception e){
                sseEmitter.completeWithError(e);
            }

            //更改运行状态
            this.state = AgentState.RUNNING;
            //保存记忆
            messageList.add(new UserMessage(userPrompt));

            try{
                while(this.currentStep < this.maxStep && this.state != AgentState.FINISHED){
                    this.currentStep++;
                    log.info("Running step:"+this.currentStep);
                    //单步执行
                    String stepResult = step();
                    String result ="step"+currentStep+": "+ stepResult;
                    sseEmitter.send(result);
                }

                if(currentStep>=maxStep){
                    this.state = AgentState.FINISHED;
                    sseEmitter.send("已终止：已达到最大步骤数("+maxStep+")");
                }
                sseEmitter.complete();
            }catch (Exception e){
                state = AgentState.ERROR;
                log.error("Error running agent",e);
                try {
                    sseEmitter.send("执行错误:"+e.getMessage());
                    sseEmitter.complete();
                } catch (IOException ex) {
                    sseEmitter.completeWithError(ex);
                }
            }finally {
                this.clean();
            }
        });

        //设置超时回溯
        sseEmitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.clean();
            log.info("SSE connection Timeout");
        });

        //设置完成回溯
        sseEmitter.onCompletion(() -> {
            if(state == AgentState.RUNNING){
                this.state = AgentState.FINISHED;
            }
            this.clean();
            log.info("SSE connection Completed");
        });

        //告诉 Spring 这是个 SSE 长连接，别关掉
        return sseEmitter;
    }


}

