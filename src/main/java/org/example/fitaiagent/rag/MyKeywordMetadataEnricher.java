package org.example.fitaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Component
public class MyKeywordMetadataEnricher {

    @Resource
    private ChatModel dashscopeChatModel;

    List<Document> enrichDocuments(List<Document> documents) {
        List<Document> enrichedDocs = new java.util.ArrayList<>();
        for (Document doc : documents) {
            Document enriched = addStatusMetadata(doc);
            enrichedDocs.add(enriched);
        }

        org.springframework.ai.model.transformer.KeywordMetadataEnricher keywordEnricher = new org.springframework.ai.model.transformer.KeywordMetadataEnricher(dashscopeChatModel, 5);
        return keywordEnricher.apply(enrichedDocs);
    }

    private Document addStatusMetadata(Document document) {
        String text = document.getText();
        String status = determineStatusFromContent(text);

        Map<String, Object> metadata = new HashMap<>(document.getMetadata());
        metadata.put("status", status);

        return new Document(document.getId(), text, metadata);
    }

    private String determineStatusFromContent(String text) {
        String prompt = "根据以下运动相关内容，判断其属于哪种状态，只返回一个词：'新手'、'进阶'或'日常维持'。\n" +
                "判断标准：\n" +
                "- 新手：基础运动选择、动作标准、坚持困难等\n" +
                "- 进阶：突破平台期、提升能力、训练计划等\n" +
                "- 日常维持：时间管理、碎片化锻炼、生活平衡等\n" +
                "\n内容：" + text;

        String result = dashscopeChatModel.call(prompt);
        result = result.trim();

        if (result.contains("进阶")) return "进阶";
        if (result.contains("日常") || result.contains("维持")) return "日常维持";
        return "新手";
    }

}
