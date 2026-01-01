package org.jwellman.foundation.provider;

import java.awt.CardLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import org.jwellman.foundation.interfaces.uiViewProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of uiViewProvider using CardLayout.
 * <p>
 * Manages multiple views (cards) in a single container, allowing applications
 * to switch between different UI states (splash → main, wizard steps, etc.).
 * <p>
 * Thread Safety: All methods should be called on the EDT.
 *
 * @author Foundation Framework
 * @since Silver Tier
 */
public class DefaultViewProvider implements uiViewProvider {

    private static final Logger log = LoggerFactory.getLogger(DefaultViewProvider.class);

    /** The CardLayout manager */
    private final CardLayout cardLayout;

    /** The container panel holding all cards */
    private final JPanel container;

    /** The currently visible card name */
    private String currentCard;

    /** Set of card names that have been added */
    private final Set<String> cardNames;

    /** Listeners for card show events, keyed by card name */
    private final Map<String, List<Runnable>> cardListeners;

    /**
     * Creates a new DefaultViewProvider with an empty CardLayout container.
     */
    public DefaultViewProvider() {
        this.cardLayout = new CardLayout();
        this.container = new JPanel(cardLayout);
        this.currentCard = null;
        this.cardNames = new HashSet<>();
        this.cardListeners = new HashMap<>();

        log.debug("DefaultViewProvider created");
    }

    @Override
    public void addCard(String cardName, Component content) {
        if (cardName == null || cardName.trim().isEmpty()) {
            throw new IllegalArgumentException("Card name cannot be null or empty");
        }
        if (content == null) {
            throw new IllegalArgumentException("Card content cannot be null");
        }

        // Remove existing card with same name (if any)
        if (hasCard(cardName)) {
            log.debug("Replacing existing card: {}", cardName);
            // Note: CardLayout doesn't have a remove by name method,
            // but adding with same name replaces it
        }

        container.add(content, cardName);
        cardNames.add(cardName);
        log.debug("Added card: {}", cardName);

        // If this is the first card, show it automatically
        if (currentCard == null) {
            showCard(cardName);
        }
    }

    @Override
    public void showCard(String cardName) {
        if (cardName == null || cardName.trim().isEmpty()) {
            throw new IllegalArgumentException("Card name cannot be null or empty");
        }

        if (!hasCard(cardName)) {
            log.warn("Cannot show card '{}' - card does not exist", cardName);
            return;
        }

        cardLayout.show(container, cardName);
        currentCard = cardName;
        log.debug("Showing card: {}", cardName);

        // Notify listeners for this card
        List<Runnable> listeners = cardListeners.get(cardName);
        if (listeners != null && !listeners.isEmpty()) {
            log.debug("Notifying {} listener(s) for card: {}", listeners.size(), cardName);
            for (Runnable listener : listeners) {
                try {
                    listener.run();
                } catch (Exception e) {
                    log.error("Error in card listener for card '{}': {}", cardName, e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public Component getContainer() {
        return container;
    }

    @Override
    public String getCurrentCard() {
        return currentCard;
    }

    @Override
    public boolean hasCard(String cardName) {
        if (cardName == null) {
            return false;
        }

        return cardNames.contains(cardName);
    }

    @Override
    public void addCardListener(String cardName, Runnable listener) {
        if (cardName == null || cardName.trim().isEmpty()) {
            throw new IllegalArgumentException("Card name cannot be null or empty");
        }
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }

        cardListeners.computeIfAbsent(cardName, k -> new ArrayList<>()).add(listener);
        log.debug("Added listener for card: {}", cardName);
    }
}
