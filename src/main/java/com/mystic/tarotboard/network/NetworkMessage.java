package com.mystic.tarotboard.network;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * A network message containing a type identifier and associated data.
 */
public record NetworkMessage(String type, Msg data) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Sealed interface for all message data types exchanged between server and client.
     */
    public sealed
    interface Msg extends Serializable {

        /**
         * A player joining the game with a chosen name and color.
         */
        record PlayerJoin(String name, double r, double g, double b) implements Msg {
        }

        /**
         * Notification that a player has left the game.
         */
        record PlayerLeave(int playerId) implements Msg {
        }

        /**
         * The current list of connected players.
         */
        record PlayerList(ArrayList<PlayerInfo> players) implements Msg {
        }

        /**
         * A cursor movement from a player.
         */
        record CursorMove(int playerId, double x, double y) implements Msg {
        }

        /**
         * A cursor image update from a player.
         */
        record CursorImage(int playerId, byte[] imageData) implements Msg {
        }

        /**
         * A piece being moved to a new position.
         */
        record PieceMove(int playerId, String pieceId, double x, double y) implements Msg {
        }

        /**
         * A piece being rotated to a new angle.
         */
        record PieceRotate(int playerId, String pieceId, double rotation) implements Msg {
        }

        /**
         * A piece having its face visibility toggled.
         */
        record PieceFlip(int playerId, String pieceId, boolean frontVisible, boolean backVisible,
                         boolean textVisible) implements Msg {
        }

        /**
         * A piece being brought to the front of the view.
         */
        record PieceToFront(int playerId, String pieceId) implements Msg {
        }

        /**
         * A new chip being spawned on the board.
         */
        record SpawnChip(int playerId, String pieceId, double x, double y, double red, double green, double blue,
                         double opacity) implements Msg {
        }

        /**
         * A new die being spawned on the board.
         */
        record SpawnDie(int playerId, String pieceId, double x, double y, int sides, double value, double red,
                        double green, double blue, double opacity) implements Msg {
        }

        /**
         * A piece being deleted from the board.
         */
        record DeletePiece(int playerId, String pieceId) implements Msg {
        }

        /**
         * A die roll result.
         */
        record DieRoll(int playerId, String pieceId, int value) implements Msg {
        }

        /**
         * Request to start a new game.
         */
        record NewGame(int playerId) implements Msg {
        }

        /**
         * Request to reshuffle the deck.
         */
        record ReshuffleCards(int playerId) implements Msg {
        }

        /**
         * Request to reset all dice.
         */
        record ResetDice(int playerId) implements Msg {
        }

        /**
         * Request to reset all chips.
         */
        record ResetChips(int playerId) implements Msg {
        }

        /**
         * Assigns a player ID to a newly connected client.
         */
        record YourId(int playerId) implements Msg {
        }

        /**
         * Request to receive the full board state.
         */
        record SendState(int playerId) implements Msg {
        }

        /**
         * Synchronizes the list of card names with clients.
         */
        record CardNamesSync(ArrayList<String> cardNames) implements Msg {
        }

        /**
         * Request to become an operator, optionally providing a password.
         */
        record RequestOperator(int playerId, String password) implements Msg {
        }

        /**
         * Response indicating whether the client was granted operator status.
         */
        record OperatorStatus(int playerId, boolean isOperator) implements Msg {
        }

        /**
         * Full state synchronization containing all cards, chips, and dice data.
         */
        record StateSync(
                int[] cardIds, double[] cardX, double[] cardY, double[] cardRot,
                boolean[] cardBackVis, boolean[] cardFrontVis, boolean[] cardTextVis,
                String[] chipIds, double[] chipX, double[] chipY, double[] chipRot,
                boolean[] chipFrontVis, boolean[] chipBackVis,
                double[] chipR, double[] chipG, double[] chipB, double[] chipO,
                String[] dieIds, double[] dieX, double[] dieY, double[] dieRot,
                int[] dieSides, int[] dieVals,
                double[] dieR, double[] dieG, double[] dieB, double[] dieO
        ) implements Msg {
        }
    }

    /**
     * Creates a {@code NetworkMessage} wrapping the given data, using the class simple name as the type.
     *
     * @param data the message data
     * @return a new {@code NetworkMessage}
     */
    public static NetworkMessage of(Msg data) {
        return new NetworkMessage(data.getClass().getSimpleName(), data);
    }

    /**
     * Information about a connected player.
     */
    public record PlayerInfo(int id, String name, double r, double g, double b) implements Serializable {
    }
}
