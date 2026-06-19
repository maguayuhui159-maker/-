package com.jiangmai.platform.controller;

import com.jiangmai.platform.utils.Result;
import com.jiangmai.platform.dto.AiChatRequest;
import com.jiangmai.platform.service.DeepSeekService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI Service Interface")
public class AiController {

    private final DeepSeekService deepSeekService;

    @PostMapping("/chat")
    @Operation(summary = "Ask the AI a question")
    public Result<String> chatWithAi(@RequestBody AiChatRequest request) {
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return Result.error("消息不能为空");
        }
        String reply = deepSeekService.chat(request.getMessage());
        return Result.success(reply);
    }
}
