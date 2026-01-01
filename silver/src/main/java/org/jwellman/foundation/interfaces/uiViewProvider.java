package org.jwellman.foundation.interfaces;

import java.awt.Component;

/**
 * Strategy interface for managing card-based view switching in Foundation applications.
 * <p>
 * Enables applications to manage multiple views (cards) within a single container,
 * supporting use cases such as:
 * <ul>
 * <li>User-dismissable splash screens</li>
 * <li>Multi-step wizards</li>
 * <li>Modal/modeless workflow transitions</li>
 * </ul>
 * <p>
 * The view provider manages a CardLayout container where each view is identified
 * by a unique string name. The framework automatically creates a "main" card for
 * the primary application content.
 *
 * @author Foundation Framework
 * @since Silver Tier
 */
public interface uiViewProvider {

    /**
     * Adds a new card (view) to the managed container.
     * <p>
     * The card name must be unique. Adding a card with the same name as an
     * existing card will replace the existing card.
     *
     * @param cardName Unique identifier for this card
     * @param content The component to display when this card is shown
     * @throws IllegalArgumentException if cardName is null or empty
     */
    void addCard(String cardName, Component content);

    /**
     * Switches the visible card to the specified name.
     * <p>
     * If the card name does not exist, this method logs a warning and
     * takes no action.
     *
     * @param cardName The name of the card to show
     * @throws IllegalArgumentException if cardName is null or empty
     */
    void showCard(String cardName);

    /**
     * Returns the container panel that manages all cards.
     * <p>
     * This is the panel that should be set as the content pane of the
     * main window or desktop frame.
     *
     * @return The CardLayout container panel
     */
    Component getContainer();

    /**
     * Returns the currently visible card name, or null if no cards exist.
     *
     * @return The name of the currently visible card, or null
     */
    String getCurrentCard();

    /**
     * Checks if a card with the given name exists.
     *
     * @param cardName The card name to check
     * @return true if the card exists, false otherwise
     */
    boolean hasCard(String cardName);
}
