package forge.ai;

import forge.GuiDesktop;
import forge.StaticData;
import forge.deck.Deck;
import forge.game.Game;
import forge.game.GameRules;
import forge.game.GameStage;
import forge.game.GameType;
import forge.game.Match;
import forge.game.card.Card;
import forge.game.phase.PhaseType;
import forge.game.player.Player;
import forge.game.player.RegisteredPlayer;
import forge.game.zone.ZoneType;
import forge.gui.GuiBase;
import forge.item.IPaperCard;
import forge.item.PaperCard;
import forge.localinstance.properties.ForgePreferences.FPref;
import forge.model.FModel;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

import static org.testng.Assert.*;

import com.google.common.collect.Lists;

/**
 * Unit tests for CardEqualityUtil.
 */
public class CardEqualityUtilTest {
    private static boolean initialized = false;
    private Game game;
    private Player player;

    @BeforeClass
    public void initialize() {
        if (!initialized) {
            GuiBase.setInterface(new GuiDesktop());
            FModel.initialize(null, preferences -> {
                preferences.setPref(FPref.LOAD_CARD_SCRIPTS_LAZILY, false);
                preferences.setPref(FPref.UI_LANGUAGE, "en-US");
                return null;
            });
            initialized = true;
        }
    }

    @BeforeMethod
    public void setUp() {
        // Create a game for testing
        List<RegisteredPlayer> players = Lists.newArrayList();
        Deck d1 = new Deck();
        players.add(new RegisteredPlayer(d1).setPlayer(new LobbyPlayerAi("p1", null)));
        players.add(new RegisteredPlayer(d1).setPlayer(new LobbyPlayerAi("p2", null)));
        GameRules rules = new GameRules(GameType.Constructed);
        Match match = new Match(rules, players, "Test");
        game = new Game(players, rules, match);
        player = game.getPlayers().get(0);
        game.setAge(GameStage.Play);
        game.getPhaseHandler().devModeSet(PhaseType.MAIN1, player);
        game.getPhaseHandler().onStackResolved();
    }

    @Test
    public void testAreFunctionallyEqual_SameCard() {
        // Create a card
        IPaperCard paperCard = StaticData.instance().getCommonCards().getCard("Grizzly Bears");
        Card card = Card.fromPaperCard(paperCard, player);
        
        // A card should be equal to itself
        assertTrue(CardEqualityUtil.areFunctionallyEqual(card, card));
    }

    @Test
    public void testAreFunctionallyEqual_NullCards() {
        // Null cards should not be equal
        assertFalse(CardEqualityUtil.areFunctionallyEqual(null, null));
        
        IPaperCard paperCard = StaticData.instance().getCommonCards().getCard("Grizzly Bears");
        Card card = Card.fromPaperCard(paperCard, player);
        
        assertFalse(CardEqualityUtil.areFunctionallyEqual(null, card));
        assertFalse(CardEqualityUtil.areFunctionallyEqual(card, null));
    }

    @Test
    public void testAreFunctionallyEqual_IdenticalTokens() {
        // Create two identical tokens
        IPaperCard bearToken = StaticData.instance().getCommonCards().getCard("Grizzly Bears");
        Card token1 = Card.fromPaperCard(bearToken, player);
        Card token2 = Card.fromPaperCard(bearToken, player);
        
        // Set them up identically
        game.getAction().moveTo(ZoneType.Battlefield, token1, null, null);
        game.getAction().moveTo(ZoneType.Battlefield, token2, null, null);
        
        // They should be functionally equal
        assertTrue(CardEqualityUtil.areFunctionallyEqual(token1, token2));
    }

    @Test
    public void testAreFunctionallyEqual_DifferentNames() {
        // Create cards with different names
        IPaperCard bearCard = StaticData.instance().getCommonCards().getCard("Grizzly Bears");
        IPaperCard wolfCard = StaticData.instance().getCommonCards().getCard("Gray Ogre");
        
        Card card1 = Card.fromPaperCard(bearCard, player);
        Card card2 = Card.fromPaperCard(wolfCard, player);
        
        // Different cards should not be equal
        assertFalse(CardEqualityUtil.areFunctionallyEqual(card1, card2));
    }

    @Test
    public void testAreFunctionallyEqual_TappedVsUntapped() {
        // Create two identical cards
        IPaperCard paperCard = StaticData.instance().getCommonCards().getCard("Grizzly Bears");
        Card card1 = Card.fromPaperCard(paperCard, player);
        Card card2 = Card.fromPaperCard(paperCard, player);
        
        game.getAction().moveTo(ZoneType.Battlefield, card1, null, null);
        game.getAction().moveTo(ZoneType.Battlefield, card2, null, null);
        
        // Tap one card
        card1.setTapped(true);
        
        // They should not be equal if one is tapped
        assertFalse(CardEqualityUtil.areFunctionallyEqual(card1, card2));
    }

    @Test
    public void testAreFunctionallyEqual_DifferentZones() {
        // Create two identical cards in different zones
        IPaperCard paperCard = StaticData.instance().getCommonCards().getCard("Grizzly Bears");
        Card card1 = Card.fromPaperCard(paperCard, player);
        Card card2 = Card.fromPaperCard(paperCard, player);
        
        game.getAction().moveTo(ZoneType.Battlefield, card1, null, null);
        game.getAction().moveTo(ZoneType.Hand, card2, null, null);
        
        // Cards in different zones should not be equal
        assertFalse(CardEqualityUtil.areFunctionallyEqual(card1, card2));
    }

    @Test
    public void testAreFunctionallyEqual_DifferentDamage() {
        // Create two identical creatures
        IPaperCard paperCard = StaticData.instance().getCommonCards().getCard("Grizzly Bears");
        Card card1 = Card.fromPaperCard(paperCard, player);
        Card card2 = Card.fromPaperCard(paperCard, player);
        
        game.getAction().moveTo(ZoneType.Battlefield, card1, null, null);
        game.getAction().moveTo(ZoneType.Battlefield, card2, null, null);
        
        // Damage one card
        card1.addDamage(1, card1, null);
        
        // They should not be equal if they have different damage
        assertFalse(CardEqualityUtil.areFunctionallyEqual(card1, card2));
    }

    @Test
    public void testAreStrictlyEqual_WithID() {
        // Create two identical cards
        IPaperCard paperCard = StaticData.instance().getCommonCards().getCard("Grizzly Bears");
        Card card1 = Card.fromPaperCard(paperCard, player);
        Card card2 = Card.fromPaperCard(paperCard, player);
        
        game.getAction().moveTo(ZoneType.Battlefield, card1, null, null);
        game.getAction().moveTo(ZoneType.Battlefield, card2, null, null);
        
        // They should be functionally equal but not strictly equal (different IDs)
        assertTrue(CardEqualityUtil.areFunctionallyEqual(card1, card2));
        assertFalse(CardEqualityUtil.areStrictlyEqual(card1, card2));
    }

    @Test
    public void testArePaperCardsFunctionallyEqual_SameCard() {
        // Get the same paper card
        PaperCard card1 = StaticData.instance().getCommonCards().getCard("Grizzly Bears");
        PaperCard card2 = StaticData.instance().getCommonCards().getCard("Grizzly Bears");
        
        // Should be equal (same name)
        assertTrue(CardEqualityUtil.arePaperCardsFunctionallyEqual(card1, card2));
    }

    @Test
    public void testArePaperCardsFunctionallyEqual_DifferentCards() {
        // Get different paper cards
        PaperCard card1 = StaticData.instance().getCommonCards().getCard("Grizzly Bears");
        PaperCard card2 = StaticData.instance().getCommonCards().getCard("Gray Ogre");
        
        // Should not be equal (different names)
        assertFalse(CardEqualityUtil.arePaperCardsFunctionallyEqual(card1, card2));
    }

    @Test
    public void testArePaperCardsFunctionallyEqual_NullCards() {
        // Null checks
        assertFalse(CardEqualityUtil.arePaperCardsFunctionallyEqual(null, null));
        
        PaperCard card = StaticData.instance().getCommonCards().getCard("Grizzly Bears");
        assertFalse(CardEqualityUtil.arePaperCardsFunctionallyEqual(null, card));
        assertFalse(CardEqualityUtil.arePaperCardsFunctionallyEqual(card, null));
    }

    @Test
    public void testGroupByFunctionalEquality_EmptyList() {
        // Empty list should return empty map
        Map<Card, List<Card>> groups = CardEqualityUtil.groupByFunctionalEquality(new ArrayList<>());
        assertTrue(groups.isEmpty());
    }

    @Test
    public void testGroupByFunctionalEquality_NullList() {
        // Null list should return empty map
        Map<Card, List<Card>> groups = CardEqualityUtil.groupByFunctionalEquality(null);
        assertTrue(groups.isEmpty());
    }

    @Test
    public void testGroupByFunctionalEquality_SingleCard() {
        // Single card should create one group
        IPaperCard paperCard = StaticData.instance().getCommonCards().getCard("Grizzly Bears");
        Card card = Card.fromPaperCard(paperCard, player);
        game.getAction().moveTo(ZoneType.Battlefield, card, null, null);
        
        List<Card> cards = Arrays.asList(card);
        Map<Card, List<Card>> groups = CardEqualityUtil.groupByFunctionalEquality(cards);
        
        assertEquals(groups.size(), 1);
        assertTrue(groups.containsKey(card));
        assertEquals(groups.get(card).size(), 1);
    }

    @Test
    public void testGroupByFunctionalEquality_IdenticalCards() {
        // Create 3 identical tokens
        IPaperCard paperCard = StaticData.instance().getCommonCards().getCard("Grizzly Bears");
        Card card1 = Card.fromPaperCard(paperCard, player);
        Card card2 = Card.fromPaperCard(paperCard, player);
        Card card3 = Card.fromPaperCard(paperCard, player);
        
        game.getAction().moveTo(ZoneType.Battlefield, card1, null, null);
        game.getAction().moveTo(ZoneType.Battlefield, card2, null, null);
        game.getAction().moveTo(ZoneType.Battlefield, card3, null, null);
        
        List<Card> cards = Arrays.asList(card1, card2, card3);
        Map<Card, List<Card>> groups = CardEqualityUtil.groupByFunctionalEquality(cards);
        
        // Should have 1 group with 3 cards
        assertEquals(groups.size(), 1);
        List<Card> group = groups.values().iterator().next();
        assertEquals(group.size(), 3);
        assertTrue(group.contains(card1));
        assertTrue(group.contains(card2));
        assertTrue(group.contains(card3));
    }

    @Test
    public void testGroupByFunctionalEquality_MixedCards() {
        // Create 2 bears and 1 ogre
        IPaperCard bearCard = StaticData.instance().getCommonCards().getCard("Grizzly Bears");
        IPaperCard ogreCard = StaticData.instance().getCommonCards().getCard("Gray Ogre");
        
        Card bear1 = Card.fromPaperCard(bearCard, player);
        Card bear2 = Card.fromPaperCard(bearCard, player);
        Card ogre = Card.fromPaperCard(ogreCard, player);
        
        game.getAction().moveTo(ZoneType.Battlefield, bear1, null, null);
        game.getAction().moveTo(ZoneType.Battlefield, bear2, null, null);
        game.getAction().moveTo(ZoneType.Battlefield, ogre, null, null);
        
        List<Card> cards = Arrays.asList(bear1, bear2, ogre);
        Map<Card, List<Card>> groups = CardEqualityUtil.groupByFunctionalEquality(cards);
        
        // Should have 2 groups
        assertEquals(groups.size(), 2);
        
        // Find the bear group and ogre group
        List<Card> bearGroup = null;
        List<Card> ogreGroup = null;
        
        for (Map.Entry<Card, List<Card>> entry : groups.entrySet()) {
            if (entry.getKey().getName().equals("Grizzly Bears")) {
                bearGroup = entry.getValue();
            } else if (entry.getKey().getName().equals("Gray Ogre")) {
                ogreGroup = entry.getValue();
            }
        }
        
        assertNotNull(bearGroup);
        assertNotNull(ogreGroup);
        assertEquals(bearGroup.size(), 2);
        assertEquals(ogreGroup.size(), 1);
    }

    @Test
    public void testGroupByFunctionalEquality_TappedVsUntapped() {
        // Create 2 bears, one tapped and one untapped
        IPaperCard paperCard = StaticData.instance().getCommonCards().getCard("Grizzly Bears");
        Card card1 = Card.fromPaperCard(paperCard, player);
        Card card2 = Card.fromPaperCard(paperCard, player);
        
        game.getAction().moveTo(ZoneType.Battlefield, card1, null, null);
        game.getAction().moveTo(ZoneType.Battlefield, card2, null, null);
        
        card1.setTapped(true);
        
        List<Card> cards = Arrays.asList(card1, card2);
        Map<Card, List<Card>> groups = CardEqualityUtil.groupByFunctionalEquality(cards);
        
        // Should have 2 groups (tapped and untapped are different)
        assertEquals(groups.size(), 2);
    }

    @Test
    public void testGetBatchableDecisions_EmptyList() {
        Map<Card, Integer> counts = CardEqualityUtil.getBatchableDecisions(new ArrayList<>());
        assertTrue(counts.isEmpty());
    }

    @Test
    public void testGetBatchableDecisions_IdenticalCards() {
        // Create 5 identical tokens
        IPaperCard paperCard = StaticData.instance().getCommonCards().getCard("Grizzly Bears");
        List<Card> cards = new ArrayList<>();
        
        for (int i = 0; i < 5; i++) {
            Card card = Card.fromPaperCard(paperCard, player);
            game.getAction().moveTo(ZoneType.Battlefield, card, null, null);
            cards.add(card);
        }
        
        Map<Card, Integer> counts = CardEqualityUtil.getBatchableDecisions(cards);
        
        // Should have 1 entry with count 5
        assertEquals(counts.size(), 1);
        Integer count = counts.values().iterator().next();
        assertEquals(count, Integer.valueOf(5));
    }

    @Test
    public void testGetBatchableDecisions_MixedCards() {
        // Create 3 bears and 2 ogres
        IPaperCard bearCard = StaticData.instance().getCommonCards().getCard("Grizzly Bears");
        IPaperCard ogreCard = StaticData.instance().getCommonCards().getCard("Gray Ogre");
        
        List<Card> cards = new ArrayList<>();
        
        for (int i = 0; i < 3; i++) {
            Card card = Card.fromPaperCard(bearCard, player);
            game.getAction().moveTo(ZoneType.Battlefield, card, null, null);
            cards.add(card);
        }
        
        for (int i = 0; i < 2; i++) {
            Card card = Card.fromPaperCard(ogreCard, player);
            game.getAction().moveTo(ZoneType.Battlefield, card, null, null);
            cards.add(card);
        }
        
        Map<Card, Integer> counts = CardEqualityUtil.getBatchableDecisions(cards);
        
        // Should have 2 entries
        assertEquals(counts.size(), 2);
        
        // Check counts
        int bearCount = 0;
        int ogreCount = 0;
        
        for (Map.Entry<Card, Integer> entry : counts.entrySet()) {
            if (entry.getKey().getName().equals("Grizzly Bears")) {
                bearCount = entry.getValue();
            } else if (entry.getKey().getName().equals("Gray Ogre")) {
                ogreCount = entry.getValue();
            }
        }
        
        assertEquals(bearCount, 3);
        assertEquals(ogreCount, 2);
    }
}
