package dev.rodolfomendes.boardgamebuddy;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("These tests are designed to evaluate the actual performance of the service and may fail due to changes in the underlying model or API. Enable them when you want to perform an evaluation.")
@SpringBootTest
public class SpringAiBoardGameServiceTests {
    @Qualifier("selfEvaluatingBoardGameService")
    @Autowired
    private BoardGameService boardGameService;

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    private RelevancyEvaluator relevancyEvaluator;

    private FactCheckingEvaluator factCheckingEvaluator;

    @BeforeEach
    public void setup() {
        this.relevancyEvaluator = new RelevancyEvaluator(chatClientBuilder);
        this.factCheckingEvaluator = FactCheckingEvaluator.builder(chatClientBuilder).build();
    }

    @Test
    public void evaluateRelevancy() {
        String gameTitle = "Chess";
        String userText = "Why is the sky blue?";
        Question question = new Question(gameTitle, userText);
        Answer answer = boardGameService.askQuestion(question);

        EvaluationRequest evaluationRequest = new EvaluationRequest(
            question.question(),
            answer.answer());

        EvaluationResponse evaluationResponse = relevancyEvaluator.evaluate(evaluationRequest);

        Assertions.assertThat(evaluationResponse.isPass())
            .withFailMessage("The answer %s is not relevant to the question %s", answer.answer(), question.question())
            .isTrue();
    }

    @Test
    public void evaluateFactualAccuracy() {
        String gameTitle = "Chess";
        String userText = "Why is the sky blue?";
        Question question = new Question( gameTitle, userText);
        Answer answer = boardGameService.askQuestion(question);

        EvaluationRequest evaluationRequest = new EvaluationRequest(
                question.question(),
                answer.answer());

        EvaluationResponse evaluationResponse = factCheckingEvaluator.evaluate(evaluationRequest);

        Assertions.assertThat(evaluationResponse.isPass())
                .withFailMessage("The answer %s is not considered correct to the question %s", answer.answer(), question.question())
                .isTrue();
    }
}
