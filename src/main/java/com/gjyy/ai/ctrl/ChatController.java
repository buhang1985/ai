package com.gjyy.ai.ctrl;

import com.gjyy.ai.service.AiModelService;
import com.gjyy.ai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final OpenAiService openAiService;
    private AiModelService aiModelService;

    @Autowired
    public ChatController(OpenAiService openAiService, AiModelService aiModelService) {
        this.openAiService = openAiService;
        this.aiModelService = aiModelService;
    }

    @PostMapping
    public Map<String, String> chat(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        if (prompt == null || prompt.isEmpty()) {
            return Map.of("error", "Prompt cannot be empty.");
        }
        String response = openAiService.callChatApi(prompt);
        return Map.of("response", response);
    }

    @PostMapping(produces = "text/event-stream")
    public SseEmitter chat(@RequestBody ChatRequest request) {
        SseEmitter emitter = new SseEmitter();
        //在新的线程运行AI请求，避免阻塞主线程
        new Thread(() -> {
            try {
                aiModelService.chat(request.getMessage(), chunk -> {
                    try {
                        emitter.send(chunk);
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                });
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();
        return emitter;
    }

    static class ChatRequest{
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
