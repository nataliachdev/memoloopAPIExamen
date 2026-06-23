package be.ipam.memoloop.service;

import be.ipam.memoloop.dto.CardDto;
import be.ipam.memoloop.dto.DeckDto;
import be.ipam.memoloop.dto.GameAnswerDto;
import be.ipam.memoloop.dto.GameDto;
import be.ipam.memoloop.mapper.DeckMapper;
import be.ipam.memoloop.mapper.GameAnswerMapper;
import be.ipam.memoloop.mapper.GameMapper;
import be.ipam.memoloop.model.Card;
import be.ipam.memoloop.model.Deck;
import be.ipam.memoloop.model.Game;
import be.ipam.memoloop.model.GameAnswer;
import be.ipam.memoloop.repository.GameAnswerRepository;
import be.ipam.memoloop.repository.GameRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class GameService {
    private final GameAnswerRepository gameAnswerRepository;
    private final GameRepository gameRepository;
    private GameMapper gameMapper;
    private GameAnswerMapper gameAnswerMapper;
    private DeckMapper deckMapper;
    private DeckService deckService;
    private GameAnswerService gameAnswerService;


    public GameService(GameRepository gameRepository, GameMapper gameMapper, DeckMapper deckMapper, GameAnswerService gameAnswerService, GameAnswerMapper gameAnswerMapper, DeckService deckService, GameAnswerRepository gameAnswerRepository) {
        this.gameRepository = gameRepository;
        this.gameMapper = gameMapper;
        this.deckMapper = deckMapper;
        this.gameAnswerService = gameAnswerService;
        this.gameAnswerMapper = gameAnswerMapper;
        this.gameAnswerRepository = gameAnswerRepository;
        this.deckService = deckService;
    }

    //Create game
    public GameDto createGame(DeckDto deckDto) {
        Game game = new Game();
        game.setDeck(deckService.getDeckById(deckDto.getId()));
        game = gameRepository.save(game);
        List<Card> drawnCards = deckService.drawCards(game.getDeck());

        for (Card card : drawnCards) {
            gameAnswerService.createGameAnswer(game, card);
        }

        return gameMapper.toDto(game);
    }

    @Transactional
    public GameDto getGame(Long id) {
        Game game = gameRepository.findById(id).orElseThrow(() -> new RuntimeException("Game not found"));
        return gameMapper.toDto(game);
    }

    /*public GameAnswerDto getNextCard(Long id) {
        Game game = gameRepository.findById(id).orElseThrow(() -> new RuntimeException("Game not found"));
        //find first not answered boolean null
        Optional<GameAnswer> firstUnanswered = game.getAnswers().stream()
                .filter(a -> a.getAnswer() == null).min(Comparator.comparing(GameAnswer::getId));
        return firstUnanswered
                .map(gameAnswerMapper::toDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No more cards"));
    }*/
    public GameAnswerDto getNextCard(Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        Optional<GameAnswer> firstUnanswered = game.getAnswers().stream()
                .filter(a -> a.getAnswer() == null)
                .min(Comparator.comparing(GameAnswer::getId));

        if (firstUnanswered.isEmpty()) {
            return null; // FIN DU JEU
        }

        return gameAnswerMapper.toDto(firstUnanswered.get());
    }

    public GameAnswerDto answer(GameAnswerDto answerDto) {
        GameAnswer myAnswer = gameAnswerMapper.toEntity(answerDto);
        GameAnswer answer = gameAnswerRepository.findById(myAnswer.getId()).orElseThrow(() -> new RuntimeException("Game answer not found"));
        answer.setAnswer(myAnswer.getAnswer());
        answer.setCorrectAnswer(myAnswer.getAnswer().equalsIgnoreCase(answer.getCard().getTranslation()));
        gameAnswerRepository.save(answer);
        return gameAnswerMapper.toDto(answer);
    }


}
