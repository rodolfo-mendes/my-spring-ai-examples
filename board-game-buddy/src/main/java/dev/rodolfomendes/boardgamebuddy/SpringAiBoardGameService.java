package dev.rodolfomendes.boardgamebuddy;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class SpringAiBoardGameService implements BoardGameService{
    @Value("classpath:/promptTemplates/systemPromptTemplate.st")
    private Resource questionPromptTemplate;

    private final ChatClient chatClient;
    private final GameRulesService gameRulesService;

    public SpringAiBoardGameService(
            ChatClient.Builder chatClientBuilder,
            GameRulesService gameRulesService) {
        this.chatClient = chatClientBuilder.build();
        this.gameRulesService = gameRulesService;
    }

    @Override
    public Answer askQuestion(Question question) {
        var gameRules = gameRulesService.getRulesFor(question.gameTitle());

        var answerText = chatClient.prompt()
            .user(userSpec -> userSpec
                .text(questionPromptTemplate)
                .param("gameTitle", question.gameTitle())
                .param("question", question.question())
                .param("rules", gameRules))
            .call()
            .content();

        return new Answer(question.gameTitle(), answerText);
    }
}
