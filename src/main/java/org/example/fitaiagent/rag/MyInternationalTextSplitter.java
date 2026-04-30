package org.example.fitaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class MyInternationalTextSplitter {

    @Resource
    private FitAppDocumentLoader fitAppDocumentLoader;

    public List<Document> splitChineseText() {
        List<Document> documents = fitAppDocumentLoader.loadMarkdowns();
        // Use Chinese punctuation marks
        TokenTextSplitter splitter = TokenTextSplitter.builder()
            .withChunkSize(800)
            .withMinChunkSizeChars(350)
            .withPunctuationMarks(List.of('。', '？', '！', '；'))
            .build();

        return splitter.apply(documents);
    }

    public List<Document> splitWithCustomMarks() {
        List<Document> documents = fitAppDocumentLoader.loadMarkdowns();
        // Mix of English and other punctuation marks
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(800)
                .withPunctuationMarks(List.of('.', '?', '!', '\n', ';', ':', '。'))
                .build();

        return splitter.apply(documents);
    }

}