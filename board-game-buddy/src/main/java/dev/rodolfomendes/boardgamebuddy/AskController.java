package dev.rodolfomendes.boardgamebuddy;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AskController {
    private final BoardGameService boardGameService;

    public AskController(@Qualifier("selfEvaluatingBoardGameService") BoardGameService boardGameService) {
        this.boardGameService = boardGameService;
    }

    @PostMapping(path = "/ask", produces = "application/json")
    public Answer ask(@RequestBody Question question) {
        return boardGameService.askQuestion(question);
    }
}
