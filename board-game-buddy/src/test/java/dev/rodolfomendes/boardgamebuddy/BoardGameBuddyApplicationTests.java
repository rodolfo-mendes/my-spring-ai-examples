package dev.rodolfomendes.boardgamebuddy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.io.IOException;
import java.nio.charset.Charset;

@Disabled
@EnableWireMock(
	@ConfigureWireMock(baseUrlProperties = "openai.base.url"))
@SpringBootTest(
	properties = "spring.ai.openai.base-url=${openai.base.url}")
class BoardGameBuddyApplicationTests {
	@Value("classpath:/test-openai-response.json")
    Resource responseResource;

	@Autowired
	ChatClient.Builder chatClientBuilder;

	@Autowired
	GameRulesService gameRulesService;

	@BeforeEach
	public void setup() throws IOException {
		var cannedResponse = responseResource.getContentAsString(Charset.defaultCharset());
		var mapper = new ObjectMapper();
		var responseNode = mapper.readTree(cannedResponse);
		WireMock.stubFor(WireMock
			.post("/v1/chat/completions")
			.willReturn(ResponseDefinitionBuilder.okForJson(responseNode)));
	}

	@Test
	public void testAskQuestions() {
		var boardGameService = new SpringAiBoardGameService(chatClientBuilder, gameRulesService);
		var answer = boardGameService.askQuestion(new Question("Chess", "Which piece move in L-shape?"));
		Assertions.assertThat(answer).isNotNull();
		Assertions.assertThat(answer.answer()).isEqualTo("Knight");
	}
}
