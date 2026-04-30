package org.example.fitaiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

@SpringBootTest
@ActiveProfiles("local")

public class FitAppTest {

    @Resource
    private FitApp fitApp;

    @Test
    public void testDoChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "我想健身，帮我寻找太原理工大学（明向校区）周围的店铺";
        String answer = fitApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
//        // 第二轮
//        message = "我想锻炼身体";
//        answer = fitApp.doChat(message, chatId);
//        Assertions.assertNotNull(answer);
//        // 第三轮
//        message = "我的名字叫什么来着？刚跟你说过，帮我回忆一下";
//        answer = fitApp.doChat(message, chatId);
//        Assertions.assertNotNull(answer);
    }
}
