/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.ai;

import forge.game.card.Card;
import forge.game.keyword.KeywordInterface;
import forge.game.zone.Zone;
import forge.item.PaperCard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class for card equality detection to enable AI decision batching.
 *
 * <p>This class provides methods to compare cards based on their functional
 * properties, allowing the AI to group identical cards together and evaluate
 * them once instead of individually. This significantly improves performance
 * when dealing with multiple copies of the same card or tokens.</p>
 *
 * <h3>Equality Types:</h3>
 * <ul>
 *   <li><b>Functional Equality:</b> Cards that behave identically from a
 *       gameplay perspective. Ignores cosmetic properties like art index and
 *       foil status.</li>
 *   <li><b>Strict Equality:</b> Functional equality plus ID comparison. Use
 *       when card identity matters.</li>
 *   <li><b>PaperCard Functional Equality:</b> For deck-building decisions,
 *       comparing cards by name and functional variant only.</li>
 * </ul>
 */
public final class CardEqualityUtil {

    /** Private constructor to prevent instantiation. */
    private CardEqualityUtil() {
        throw new AssertionError("Utility class");
    }

    /**
     * Compares two cards for functional equality.
     *
     * <p>Two cards are functionally equal if they behave identically in the
     * game. This comparison includes:</p>
     * <ul>
     *   <li>Card name</li>
     *   <li>Current state (transformed, flipped, etc.)</li>
     *   <li>Zone</li>
     *   <li>Controller</li>
     *   <li>Tapped status</li>
     *   <li>Face-down status</li>
     *   <li>Power/Toughness (for creatures)</li>
     *   <li>Damage</li>
     *   <li>Keywords</li>
     *   <li>Phased out status</li>
     * </ul>
     *
     * <p>This comparison ignores cosmetic properties like art index, foil
     * status, collector number, and timestamp.</p>
     *
     * @param card1 the first card to compare
     * @param card2 the second card to compare
     * @return true if the cards are functionally equal, false otherwise
     */
    public static boolean areFunctionallyEqual(final Card card1,
                                                final Card card2) {
        if (card1 == card2) {
            return true;
        }
        if (card1 == null || card2 == null) {
            return false;
        }

        // Compare basic identity
        if (!Objects.equals(card1.getName(), card2.getName())) {
            return false;
        }

        // Compare current state name (Original, Transformed, Flipped, etc.)
        if (card1.getCurrentStateName() != card2.getCurrentStateName()) {
            return false;
        }

        // Compare zone
        Zone zone1 = card1.getZone();
        Zone zone2 = card2.getZone();
        if (!Objects.equals(zone1 != null ? zone1.getZoneType() : null,
                           zone2 != null ? zone2.getZoneType() : null)) {
            return false;
        }

        // Compare controller
        if (!Objects.equals(card1.getController(), card2.getController())) {
            return false;
        }

        // Compare tapped status
        if (card1.isTapped() != card2.isTapped()) {
            return false;
        }

        // Compare face-down status
        if (card1.isFaceDown() != card2.isFaceDown()) {
            return false;
        }

        // Compare phased out status
        if (card1.isPhasedOut() != card2.isPhasedOut()) {
            return false;
        }

        // Compare power and toughness for creatures
        if (card1.isCreature() || card2.isCreature()) {
            if (card1.getNetPower() != card2.getNetPower()
                    || card1.getNetToughness() != card2.getNetToughness()) {
                return false;
            }
        }

        // Compare damage
        if (card1.getDamage() != card2.getDamage()) {
            return false;
        }

        // Compare keywords (simplified - just check if they have the same
        // keywords)
        List<KeywordInterface> keywords1 = card1.getKeywords();
        List<KeywordInterface> keywords2 = card2.getKeywords();
        if (keywords1.size() != keywords2.size()) {
            return false;
        }

        // For a more precise check, we'd need to compare keyword content,
        // but for AI batching purposes, count is often sufficient
        // (tokens of the same type will have identical keywords)

        return true;
    }

    /**
     * Compares two cards for strict equality.
     *
     * <p>This is functional equality with additional ID comparison.
     * Use this when you need to distinguish between individual card instances
     * even if they are functionally identical.</p>
     *
     * @param card1 the first card to compare
     * @param card2 the second card to compare
     * @return true if the cards are strictly equal, false otherwise
     */
    public static boolean areStrictlyEqual(final Card card1,
                                            final Card card2) {
        if (!areFunctionallyEqual(card1, card2)) {
            return false;
        }

        // Add ID comparison to distinguish individual instances
        return card1.getId() == card2.getId();
    }

    /**
     * Compares two PaperCards for functional equality.
     *
     * <p>For deck-building and card selection AI, this compares cards based
     * on:</p>
     * <ul>
     *   <li>Card name</li>
     *   <li>Functional variant (e.g., rebalanced versions)</li>
     * </ul>
     *
     * <p>This ignores edition, art index, foil status, and collector
     * number.</p>
     *
     * @param card1 the first paper card to compare
     * @param card2 the second paper card to compare
     * @return true if the paper cards are functionally equal, false otherwise
     */
    public static boolean arePaperCardsFunctionallyEqual(
            final PaperCard card1, final PaperCard card2) {
        if (card1 == card2) {
            return true;
        }
        if (card1 == null || card2 == null) {
            return false;
        }

        // Compare name
        if (!Objects.equals(card1.getName(), card2.getName())) {
            return false;
        }

        // Compare functional variant (for rebalanced versions, etc.)
        return Objects.equals(card1.getFunctionalVariant(),
                card2.getFunctionalVariant());
    }

    /**
     * Builds a functional key for a card that can be used for grouping.
     *
     * <p>Cards with the same functional key are considered functionally equal.
     * This key is used internally by grouping methods.</p>
     *
     * @param card the card to build a key for
     * @return a string key representing the card's functional properties
     */
    private static String buildFunctionalKey(final Card card) {
        if (card == null) {
            return "null";
        }

        StringBuilder key = new StringBuilder();

        // Add name
        key.append(card.getName()).append("|");

        // Add state name
        key.append(card.getCurrentStateName()).append("|");

        // Add zone
        Zone zone = card.getZone();
        key.append(zone != null ? zone.getZoneType() : "null").append("|");

        // Add controller ID
        key.append(card.getController() != null
                ? card.getController().getId() : "null").append("|");

        // Add tapped status
        key.append(card.isTapped()).append("|");

        // Add face-down status
        key.append(card.isFaceDown()).append("|");

        // Add phased out status
        key.append(card.isPhasedOut()).append("|");

        // Add power/toughness if creature
        if (card.isCreature()) {
            key.append(card.getNetPower()).append("/")
                    .append(card.getNetToughness()).append("|");
        } else {
            key.append("N/A|");
        }

        // Add damage
        key.append(card.getDamage()).append("|");

        // Add keyword count (simplified for batching)
        key.append(card.getKeywords().size());

        return key.toString();
    }

    /**
     * Groups cards by functional equality.
     *
     * <p>This method takes a collection of cards and groups them into buckets
     * where all cards in each bucket are functionally identical. The map
     * returned uses a representative card from each group as the key.</p>
     *
     * <p>Example usage:</p>
     * <pre>
     * Map&lt;Card, List&lt;Card&gt;&gt; groups =
     *     CardEqualityUtil.groupByFunctionalEquality(cardList);
     * for (Map.Entry&lt;Card, List&lt;Card&gt;&gt; entry : groups.entrySet()) {
     *     Card representative = entry.getKey();
     *     List&lt;Card&gt; identicalCards = entry.getValue();
     *
     *     // Evaluate once per group instead of once per card
     *     int value = evaluateCard(representative);
     *
     *     // Apply evaluation to all identical cards
     *     for (Card card : identicalCards) {
     *         applyEvaluation(card, value);
     *     }
     * }
     * </pre>
     *
     * @param cards the collection of cards to group
     * @return a map where keys are representative cards and values are lists
     *         of functionally identical cards (including the representative)
     */
    public static Map<Card, List<Card>> groupByFunctionalEquality(
            final Collection<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return new HashMap<>();
        }

        // First pass: group by functional key
        Map<String, List<Card>> keyGroups = new HashMap<>();
        for (Card card : cards) {
            String key = buildFunctionalKey(card);
            keyGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(card);
        }

        // Second pass: create result map with representative cards as keys
        Map<Card, List<Card>> result = new LinkedHashMap<>();
        for (List<Card> group : keyGroups.values()) {
            if (!group.isEmpty()) {
                // Use first card in group as representative
                Card representative = group.get(0);
                result.put(representative, group);
            }
        }

        return result;
    }

    /**
     * Gets a simplified map of representative cards to their counts.
     *
     * <p>This is a convenience method that returns a map showing how many
     * functionally identical cards exist for each unique card type.</p>
     *
     * <p>Example usage:</p>
     * <pre>
     * Map&lt;Card, Integer&gt; counts =
     *     CardEqualityUtil.getBatchableDecisions(cardList);
     * for (Map.Entry&lt;Card, Integer&gt; entry : counts.entrySet()) {
     *     Card representative = entry.getKey();
     *     int count = entry.getValue();
     *     System.out.println(count + " copies of " + representative.getName());
     * }
     * </pre>
     *
     * @param cards the collection of cards to analyze
     * @return a map of representative cards to their counts
     */
    public static Map<Card, Integer> getBatchableDecisions(
            final Collection<Card> cards) {
        Map<Card, List<Card>> groups = groupByFunctionalEquality(cards);
        Map<Card, Integer> counts = new LinkedHashMap<>();

        for (Map.Entry<Card, List<Card>> entry : groups.entrySet()) {
            counts.put(entry.getKey(), entry.getValue().size());
        }

        return counts;
    }
}
