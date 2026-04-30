package org.example.fitaiagent.app;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.fitaiagent.advisor.MyLogAdvisor;
import org.example.fitaiagent.chatMemory.RedisChatMemory;
import org.example.fitaiagent.rag.QueryRewriter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@Slf4j
public class FitApp {

    //本地知识库
    @Resource
    private VectorStore fitAppVectorStore;

//    //云知识库
//    @Resource
//    private Advisor fitAppRagCloudAdvisor;

    @Resource
    private QueryRewriter queryRewriter;

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "你是专注运动锻炼领域的专家，擅长帮用户解决各类运动难题，无论是想科学减脂、增肌塑形，还是改善运动习惯、规避运动损伤等。"
            +"围绕运动新手、进阶训练、日常维持三种状态提问："
            +"运动新手：询问不知道选什么运动、坚持不了、动作不标准等困扰；"
            +"进阶训练：询问突破平台期、针对性提升某项目能力、高效训练计划制定的问题；"
            +"日常维持：询问没时间运动、碎片化锻炼、运动与工作生活平衡的难题。"
            +"引导用户详述：运动场景、遇到的具体问题、尝试过的方法及自身运动目标，针对性给出用户专属方案。"
            +"每次回复都会尽量精简，不做多余阐述。";


    public FitApp(ChatModel DashScopeChatModel, RedisTemplate<String, Object> redisTemplate){

//        //基于内存记忆的会话
//        InMemoryChatMemoryRepository chatMemoryRepository = new InMemoryChatMemoryRepository();
//        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
//                .chatMemoryRepository(chatMemoryRepository)
//                .maxMessages(20) // 默认窗口大小为 20 条消息
//                .build();
        ChatMemory chatRedisMemory = new RedisChatMemory(redisTemplate);
        chatClient = ChatClient.builder(DashScopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatRedisMemory).build(),
                        new MyLogAdvisor()
                )
                .build();
    }

    public String doChat(String message,String chatId){
        //查询重写
        String remessage = queryRewriter.doQueryRewrite(message);

        ChatResponse response = chatClient
                .prompt()
                // Set advisor parameters at runtime
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatId))
                //.advisors(FitAppRagCustomAdvisorFactory.createFitAppRagCustomAdvisor(fitAppVectorStore,"新手"))
                .advisors(QuestionAnswerAdvisor.builder(fitAppVectorStore).build())//RAG知识库
                //.advisors(fitAppRagCloudAdvisor)//云知识库
                .toolCallbacks(toolCallbackProvider)
                .user(remessage)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    public Flux<String> doChatByStream(String message, String chatId){
        //查询重写
        String remessage = queryRewriter.doQueryRewrite(message);

        return chatClient
                .prompt()
                // Set advisor parameters at runtime
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, chatId))
                //.advisors(FitAppRagCustomAdvisorFactory.createFitAppRagCustomAdvisor(fitAppVectorStore,"新手"))
                .advisors(QuestionAnswerAdvisor.builder(fitAppVectorStore).build())//RAG知识库
                //.advisors(fitAppRagCloudAdvisor)//云知识库
                .toolCallbacks(toolCallbackProvider)
                .user(remessage)
                .stream()
                .content();
    }

}


