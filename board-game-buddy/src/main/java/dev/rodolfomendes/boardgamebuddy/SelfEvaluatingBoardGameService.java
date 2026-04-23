package dev.rodolfomendes.boardgamebuddy;

import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Service;

@Service
public class SelfEvaluatingBoardGameService implements BoardGameService {
    private final ChatClient chatClient;
    private final RelevancyEvaluator relevancyEvaluator;
    private final RetryTemplate retryTemplate;

    public SelfEvaluatingBoardGameService(ChatClient.Builder chatClientBuilder) {
        var chatOptions = ChatOptions.builder()
            .model("gpt-4o-mini")
            .build();

        this.chatClient = chatClientBuilder
            .defaultOptions(chatOptions)
            .build();

        this.relevancyEvaluator = RelevancyEvaluator.builder()
            .chatClientBuilder(chatClientBuilder)
            .build();

        final RetryPolicy retryPolicy = RetryPolicy.builder()
            .includes(AnswerNotRelevantException.class)
            .maxRetries(3)
            .build();

        this.retryTemplate = new RetryTemplate(retryPolicy);
    }

    @Override
    public Answer askQuestion(Question question) {
        try {
            return retryTemplate.invoke(() -> askAndEvaluate(question));
        } catch (AnswerNotRelevantException e) {
            return recover();
        }
    }

    private @NonNull Answer askAndEvaluate(Question question) {
        var answerText = chatClient
            .prompt()
            .user(question.question())
            .call()
            .content();

        evaluateRelevance(question, answerText);

        return new Answer(answerText);
    }

    public Answer recover() {
        return new Answer("I'm sorry, I wasn't able to answer the question");
    }

    private void evaluateRelevance(Question question, String answerText) {
        var evaluationRequest = new EvaluationRequest(question.question(), answerText);
        var evaluationResponse = relevancyEvaluator.evaluate(evaluationRequest);
        if (!evaluationResponse.isPass()) {
            throw new AnswerNotRelevantException(question.question(), answerText);
        }
    }
}
