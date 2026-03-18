package pl.mojezapiski.rag.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import pl.mojezapiski.rag.document.DocumentFacade;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService implements ChatFacade {
    private final List<MessageDto> messages = new ArrayList<>();
    private final OpenAiChatModel chatClient;
    private final DocumentFacade documentFacade;

    private List<MessageDto> getLastMessages() {
        return messages.stream()
                .skip(Math.max(0, messages.size() - 6))
                .toList();
    }

    private Message getSystemMessage(String userPrompt) {
        String systemPrompt = """
                Jesteś asystentem, który odpowiada na pytania.
                Używaj informacji zawartych w tagu <dokumentacja>, aby zapewnić dokładne odpowiedzi.
                Odpowiadaj krótko, ale precyzyjnie. Nie odpowiadaj na pytania jeśli w tagu <dokumentacja> nie ma danych.
                <dokumentacja>{documents}</dokumentacja>.
                Dołączam do kontekstu w tagu <messages> wiadomości, które wcześniej wymieniłeś z użytkownikiem: <messages>{messages}</messages>
                """;
        return new SystemPromptTemplate(systemPrompt)
                .createMessage(Map.of(
                        "documents", documentFacade.getSimilarDocuments(userPrompt),
                        "messages", getLastMessages()
                ));
    }

    @Override
    public List<MessageDto> getMessages() {
        return messages;
    }

    @Override
    public void sendMessage(MessageForm form) {
        Message userMessage = new UserMessage(form.content());
        Message systemMessage = getSystemMessage(form.content());
        Prompt prompt = new Prompt(List.of(userMessage, systemMessage));

        messages.add(new MessageDto(form.content(), MessageType.USER, LocalDateTime.now()));

        String result = chatClient.call(prompt)
                .getResult()
                .getOutput()
                .getText();

        messages.add(new MessageDto(result, MessageType.SYSTEM, LocalDateTime.now()));
    }
}
