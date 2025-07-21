package com.mystic.tarotboard;

import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClientNetwork {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private Thread listenerThread;
    private final String playerName;
    private final GameUI gameUI;
    private volatile boolean running = true;
    private List<TarotBoardPoker.Card> holeCards = new ArrayList<>();
    private List<TarotBoardPoker.Card> communityCards = new ArrayList<>();
    private List<String> playerOrder = new ArrayList<>();
    private Set<String> foldedPlayers = ConcurrentHashMap.newKeySet();
    private final Map<String, List<TarotBoardPoker.Card>> playerHands = new HashMap<>();

    public ClientNetwork(String host, int port, String playerName, GameUI gameUI) throws IOException {
        this.playerName = playerName;
        this.gameUI = gameUI;

        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        send("JOIN " + playerName);
        startListener();
    }

    private void startListener() {
        listenerThread = new Thread(() -> {
            try {
                String line;
                while (running && (line = in.readLine()) != null) {
                    String message = line.trim();
                    System.out.println("Server: " + message);
                    Platform.runLater(() -> handleServerMessage(message));
                }
            } catch (IOException e) {
                if (running) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> gameUI.showMessage("Disconnected from server"));
            } finally {
                close();
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public void send(String msg) {
        out.println(msg);
    }

    private void handleServerMessage(String msg) {
        String[] parts = msg.split(" ", 2);
        String command = parts[0];
        String payload = parts.length > 1 ? parts[1] : "";

        switch (command) {
            case "WELCOME" -> gameUI.showMessage("Welcome, " + payload);

            case "PLAYERS" -> {
                List<String> players;
                if (!payload.isEmpty()) {
                    players = Arrays.asList(payload.split(","));
                } else {
                    players = new ArrayList<>();
                }

                // Remove any hands for players who left
                playerHands.keySet().removeIf(player -> !players.contains(player));

                // Update player list UI
                gameUI.updatePlayers(players);

                // Restore hands for existing players
                for (String player : players) {
                    if (playerHands.containsKey(player)) {
                        gameUI.updatePlayerHand(player, playerHands.get(player));
                    }
                }
            }

            case "CHIPS" -> {
                String[] entries = payload.split(",");
                for (String entry : entries) {
                    String[] part = entry.split("=");
                    if (part.length == 2) {
                        String player = part[0].trim();
                        int chips = Integer.parseInt(part[1].trim());
                        gameUI.setPlayerChips(player, chips);
                    }
                }
                gameUI.showMessage("Chip counts synced.");
            }

            case "HAND" -> {
                String[] hp = payload.split(" ", 2);
                if (hp.length == 2) {
                    String player = hp[0].trim();
                    String[] cardStrings = hp[1].split(",");
                    List<TarotBoardPoker.Card> cards = new ArrayList<>();

                    for (String cardStr : cardStrings) {
                        cardStr = cardStr.trim();
                        TarotBoardPoker.Card card;
                        if ("FACEDOWN".equalsIgnoreCase(cardStr)) {
                            card = TarotBoardPoker.Card.createFaceDown();
                        } else {
                            card = parseCard(cardStr);
                        }
                        if (card != null) cards.add(card);
                    }

                    // Save hand state here
                    playerHands.put(player, cards);

                    // Update UI with new hand
                    gameUI.updatePlayerHand(player, cards);

                    // If this is YOUR hand, show evaluation message
                    if (player.equals(playerName)) {
                        boolean hasFaceUp = cards.stream().noneMatch(TarotBoardPoker.Card::isFaceDown);
                        if (hasFaceUp && cards.size() >= 7) {
                            TarotBoardPoker.Hand hand = TarotBoardPoker.HandEvaluator.evaluate(cards);
                            gameUI.showMessage("You have a " + hand.getHandRank() +
                                    " (Score: " + hand.getScore() + ")");
                        }
                    }
                }
            }

            case "CALL" -> {
                String player = payload.trim();
                gameUI.showMessage(player + " calls.");
                gameUI.updatePlayerAction(player, "calls");
            }

            case "BET" -> {
                String[] bp = payload.split(" ");
                if (bp.length == 2) {
                    String player = bp[0];
                    int betAmount = Integer.parseInt(bp[1]);
                    gameUI.showMessage(player + " bets " + betAmount);
                    gameUI.updatePlayerBet(player, betAmount);
                    gameUI.updatePlayerAction(player, "bets");
                }
            }

            case "RAISE" -> {
                String[] rp = payload.split(" ");
                if (rp.length == 3) {
                    String player = rp[0];
                    int raiseBy = Integer.parseInt(rp[1]);
                    int toCall = Integer.parseInt(rp[2]);
                    int total = raiseBy + toCall;

                    gameUI.showMessage(player + " raises by " + raiseBy);
                    gameUI.updatePlayerBet(player, total);
                    gameUI.updatePlayerAction(player, "raises");
                }
            }

            case "CHECK" -> {
                String player = payload.trim();
                gameUI.showMessage(player + " checks.");
                gameUI.updatePlayerAction(player, "checks");
            }

            case "COMMUNITY" -> {
                String payloadTrim = payload.trim();
                communityCards.clear();
                if (!payloadTrim.isEmpty()) {
                    String[] cardStrings = payloadTrim.split(",");
                    for (String cardStr : cardStrings) {
                        TarotBoardPoker.Card card = parseCard(cardStr.trim());
                        if (card != null) {
                            communityCards.add(card);
                        }
                    }
                }
                gameUI.updateCommunityCards(communityCards);

                // Evaluate your hand only if you have at least 7 cards total
                if (holeCards.size() + communityCards.size() >= 7) {
                    List<TarotBoardPoker.Card> fullHand = new ArrayList<>(holeCards);
                    fullHand.addAll(communityCards);
                    try {
                        TarotBoardPoker.Hand hand = TarotBoardPoker.HandEvaluator.evaluate(fullHand);
                        gameUI.showMessage("You have a " + hand.getHandRank() +
                                " (Score: " + hand.getScore() + ")");
                    } catch (IllegalArgumentException e) {
                        // Just in case evaluation fails, ignore for now or log
                        System.err.println("Hand evaluation failed: " + e.getMessage());
                    }
                }
            }

            case "FOLD" -> {
                String player = payload.trim();
                foldedPlayers.add(player);
                gameUI.showMessage(player + " folds.");
                gameUI.updatePlayerAction(player, "folds");
                gameUI.markPlayerFolded(player);
            }

            case "POT" -> {
                int potAmount = Integer.parseInt(payload.trim());
                gameUI.updatePot(potAmount);
            }

            case "SHOWDOWN" -> {
                gameUI.showMessage("Showdown!");
                String[] partsShowdown = payload.split(";");
                List<String> winners = new ArrayList<>();
                for (String part : partsShowdown) {
                    if (part.startsWith("Winner:")) {
                        String[] winnerNames = part.substring(7).split(",");
                        winners.addAll(Arrays.asList(winnerNames));
                    } else if (part.contains(":")) {
                        String[] split = part.split(":", 2);
                        String player = split[0].trim();
                        String[] cardStrings = split[1].split(",");
                        List<TarotBoardPoker.Card> finalHand = new ArrayList<>();
                        for (String cardStr : cardStrings) {
                            TarotBoardPoker.Card card = parseCard(cardStr.trim());
                            if (card != null) {
                                finalHand.add(card);
                            }
                        }
                        gameUI.updatePlayerHand(player, finalHand);
                        TarotBoardPoker.Hand hand = TarotBoardPoker.HandEvaluator.evaluate(finalHand);
                        gameUI.showMessage(player + " final hand: " + hand.getHandRank() +
                                " (Score: " + hand.getScore() + ")");
                    }
                }
                if (!winners.isEmpty()) {
                    gameUI.showMessage("Winner(s): " + String.join(", ", winners));
                }
            }

            case "NEWROUND" -> {
                foldedPlayers.clear();
                List<String> players = new ArrayList<>();
                if (!payload.isEmpty()) {
                    players = Arrays.asList(payload.split(","));
                }

                gameUI.startNewRound(players);
            }

            case "TURN" -> {
                gameUI.setCurrentTurn(payload);
                boolean myTurn = payload.equals(playerName);
                gameUI.enableActions(myTurn);
            }

            case "MESSAGE" -> gameUI.showMessage(payload);

            default -> System.out.println("Unknown command from server: " + command);
        }
    }

    private boolean isWildCard(String cardName) {
        try {
            TarotBoardPoker.Value val = TarotBoardPoker.Value.valueOf(cardName.toUpperCase());
            return val.isWild();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private TarotBoardPoker.Card parseCard(String cardString) {
        if (cardString == null || cardString.isEmpty()) return null;

        if (TarotBoardPoker.ValueCategory.WILD.name().equalsIgnoreCase(cardString) || isWildCard(cardString)) {
            try {
                TarotBoardPoker.Value val = TarotBoardPoker.Value.valueOf(cardString.toUpperCase());
                return new TarotBoardPoker.Card(null, val, false);
            } catch (IllegalArgumentException e) {
                return null;
            }
        } else if (cardString.contains(" of ")) {
            String[] parts = cardString.split(" of ");
            if (parts.length == 2) {
                try {
                    TarotBoardPoker.Value val = TarotBoardPoker.Value.valueOf(parts[0].toUpperCase());
                    TarotBoardPoker.Suit suit = TarotBoardPoker.Suit.valueOf(parts[1].toUpperCase());
                    return new TarotBoardPoker.Card(suit, val, false);
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }
        return null;
    }

    public void close() {
        running = false;
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ignored) {
        }
    }
}
