package pl.mojezapiski.rag.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai/chat")
class ChatController {
    private final ChatFacade chatFacade;
    @GetMapping("/messages")
    Map<String, Object> getMessages() {
        Map<String, Object> model = new HashMap<>();
        model.put("messages", chatFacade.getMessages());
        //model.put("messageForm", new MessageForm(StringUtils.EMPTY));
        return model;
    }

    @PostMapping("/messages")
    Map<String, Object> sendMessage(@RequestBody MessageForm messageForm) {
        chatFacade.sendMessage(messageForm);
        return getMessages();
    }
}
