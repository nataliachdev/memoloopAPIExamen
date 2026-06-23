package be.ipam.memoloop.service;

import be.ipam.memoloop.dto.DeckDetailedDto;
import be.ipam.memoloop.dto.CreateDeckDto;
import be.ipam.memoloop.dto.DeckDto;
import be.ipam.memoloop.mapper.DeckDetailedMapper;
import be.ipam.memoloop.mapper.DeckMapper;
import be.ipam.memoloop.model.Card;
import be.ipam.memoloop.model.Deck;
import be.ipam.memoloop.repository.DeckRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class DeckService {
    private final DeckRepository deckRepository;
    private final DeckMapper deckMapper;
    private final DeckDetailedMapper deckDetailedMapper;

    public DeckService(DeckRepository deckRepository, DeckMapper deckMapper, DeckDetailedMapper deckDetailedMapper) {
        this.deckRepository = deckRepository;
        this.deckMapper = deckMapper;
        this.deckDetailedMapper = deckDetailedMapper;
    }

    //Read all decks
    public List<DeckDto> getAllDecks() {
        List<Deck> decks = deckRepository.findAll();
        return decks.stream().map(deckMapper::toDto).toList();
    }

    //Read a page of decks
    public List<DeckDto> getDecks(int page, int size) {
        List<Deck> decks = deckRepository.findAll(PageRequest.of(page, size)).getContent();
        return decks.stream().map(deckMapper::toDto).toList();
    }

    //Read a deck by id
    public Deck getDeckById(Long id) {
        return deckRepository.findById(id).orElseThrow(() -> new RuntimeException("Deck not found"));
    }

    //Read detailed deck
    public DeckDetailedDto getDeckDetailedById(Long id) {
        Deck deck = deckRepository.findById(id).orElseThrow(() -> new RuntimeException("Deck not found"));
        return deckDetailedMapper.toDto(deck);
    }

    //add deck
    public DeckDto addDeck(CreateDeckDto createDeckDto) {
        Deck deck = deckMapper.toEntity(createDeckDto);
        Deck savedDeck = deckRepository.save(deck);
        return deckMapper.toDto(savedDeck);
    }

    //Draw x cards from deck
    public List<Card> drawCards(Deck deck){
        // Refresh deck from DB
        deck = deckRepository.findById(deck.getId())
                .orElseThrow(() -> new RuntimeException("Deck not found"));

        List<Card> cards = new ArrayList<>(deck.getCards());

        if (cards.size() < 10) {
            throw new IllegalStateException("Not enough cards in deck: " + cards.size());
        }

        Collections.shuffle(cards);
        return cards.subList(0, 10);
    }

    //delete deck
    public void deleteDeck(Long id) {
        deckRepository.deleteById(id);
    }

}
