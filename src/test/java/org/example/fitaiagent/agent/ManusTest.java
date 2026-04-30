package org.example.fitaiagent.agent;

import jakarta.annotation.Resource;
import org.example.fitaiagent.agent.Manus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ManusTest {

    @Resource
    private Manus Manus;
    @Test
    void run() {
        String userPrompt = """  
                你知道我叫什么名字吗，不知道的话你可以问我""";
        String answer = Manus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }
}

