package dev.rodolfomendes.boardgamebuddy;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class SpringAiBoardGameService implements BoardGameService{
    private final ChatClient chatClient;

    public SpringAiBoardGameService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public Answer askQuestion(Question question) {
        String prompt = "Answer this question about " + question.gameTitle() + ": " + question.question();

        var answerText = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return new Answer(question.gameTitle(), answerText);
    }
}
