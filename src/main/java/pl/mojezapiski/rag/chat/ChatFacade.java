package pl.mojezapiski.rag.chat;

import java.util.List;

interface ChatFacade {
    List<MessageDto> getMessages();

    void sendMessage(MessageForm form);
}
