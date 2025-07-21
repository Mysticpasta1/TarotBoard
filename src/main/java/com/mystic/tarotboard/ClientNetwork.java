package com.mystic.tarotboard;

import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClientNetwork {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private Thread listenerThread;
    private final String playerName;
    private final GameUI gameUI;
    private volatile boolean running = true;

    private List<String> playerOrder = new ArrayList<>();
    private Set<String> foldedPlayers = ConcurrentHashMap.newKeySet();

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
            case "PLAYERS" -> gameUI.updatePlayers(Arrays.asList(payload.split(",")));
            case "HAND" -> {
                String[] hp = payload.split(" ", 2);
                if (hp.length == 2) {
                    String player = hp[0].trim();
                    String[] cardStrings = hp[1].split(",");
                    List<TarotBoardPoker.Card> cards = new ArrayList<>();
                    for (String cardStr : cardStrings) {
                        TarotBoardPoker.Card card = parseCard(cardStr.trim());
                        if (card != null) cards.add(card);
                    }
                    gameUI.updatePlayerHand(player, cards);
                    if (cards.size() == 5) {
                        TarotBoardPoker.Hand hand = TarotBoardPoker.HandEvaluator.evaluate(cards);
                        gameUI.showMessage(player + " has a " + hand.getHandRank() +
                                " (Score: " + hand.getScore() + ")");
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
                    gameUI.updatePlayerBet(bp[0], Integer.parseInt(bp[1]));
                    gameUI.updatePlayerAction(bp[0], "bets");
                }
            }
            case "RAISE" -> {
                String[] rp = payload.split(" ");
                if (rp.length == 2) {
                    gameUI.updatePlayerBet(rp[0], Integer.parseInt(rp[1]));
                    gameUI.updatePlayerAction(rp[0], "raises");
                }
            }
            case "CHECK" -> {
                String player = payload.trim();
                gameUI.showMessage(player + " checks.");
                gameUI.updatePlayerAction(player, "checks");
            }
            case "COMMUNITY" -> {
                String payloadTrim = payload.trim();
                List<TarotBoardPoker.Card> communityCards = new ArrayList<>();
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
                        gameUI.showMessage(player + " final hand: " + hand.getHandRank() + " (Score: " + hand.getScore() + ")");
                    }
                }
                if (!winners.isEmpty()) {
                    gameUI.showMessage("Winner(s): " + String.join(", ", winners));
                }
            }
            case "NEWROUND" -> {
                foldedPlayers.clear();
                playerOrder = Arrays.asList(payload.split(","));
                gameUI.startNewRound(playerOrder);
                gameUI.showMessage("New round started!");
            }
            case "TURN" -> {
                gameUI.setCurrentTurn(payload);
                if (payload.equals(playerName)) {
                    gameUI.enableActions(true);
                } else {
                    gameUI.enableActions(false);
                }
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

        if (TarotBoardPoker.ValueCategory.WILD.name().equals(cardString) || isWildCard(cardString)) {
            TarotBoardPoker.Value val = TarotBoardPoker.Value.valueOf(cardString.toUpperCase());
            return new TarotBoardPoker.Card(null, val);
        } else if (cardString.contains(" of ")) {
            String[] parts = cardString.split(" of ");
            if (parts.length == 2) {
                try {
                    TarotBoardPoker.Value val = TarotBoardPoker.Value.valueOf(parts[0].toUpperCase());
                    TarotBoardPoker.Suit suit = TarotBoardPoker.Suit.valueOf(parts[1].toUpperCase());
                    return new TarotBoardPoker.Card(suit, val);
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
