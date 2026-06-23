package be.ipam.memoloop.service;

import be.ipam.memoloop.dto.CardDto;
import be.ipam.memoloop.dto.CreateCardDto;
import be.ipam.memoloop.mapper.CardMapper;
import be.ipam.memoloop.model.Card;
import be.ipam.memoloop.model.Deck;
import be.ipam.memoloop.repository.CardRepository;
import be.ipam.memoloop.repository.DeckRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CardService {
    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;
    private final CardMapper cardMapper;

    public CardService(CardRepository cardRepository, DeckRepository deckRepository, CardMapper cardMapper) {
        this.cardRepository = cardRepository;
        this.deckRepository = deckRepository;
        this.cardMapper = cardMapper;
    }

    // Read Card by id
    public CardDto getCardById(Long id) {
        Card card = cardRepository.findById(id).orElseThrow(() -> new RuntimeException("Card not found"));
        return cardMapper.toDto(card);
    }

    //add card
    public CardDto addCard(CreateCardDto createCardDto) {
        Card card = cardMapper.toEntity(createCardDto);
        Card savedCard = cardRepository.save(card);
        return cardMapper.toDto(savedCard);
    }

    // add card to a specific deck
    public CardDto addCardToDeck(Long deckId, CreateCardDto createCardDto) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Deck not found"));

        Card card = cardMapper.toEntity(createCardDto);
        card.setDeck(deck);

        Card savedCard = cardRepository.save(card);
        return cardMapper.toDto(savedCard);
    }

    // delete card
    public void deleteCard(Long id) {
        if (!cardRepository.existsById(id)) {
            throw new RuntimeException("Card not found");
        }
        cardRepository.deleteById(id);
    }

    public CardDto updateCard(Long id, CreateCardDto dto) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id));

        card.setWord(dto.getWord());
        card.setTranslation(dto.getTranslation());

        Card updatedCard = cardRepository.save(card);

        return new CardDto(
                updatedCard.getId(),
                updatedCard.getWord(),
                updatedCard.getTranslation()
        );
    }
}
