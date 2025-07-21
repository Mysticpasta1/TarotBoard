package com.mystic.tarotboard;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class PokerServer {
    private ServerSocket serverSocket;
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final List<String> playerOrder = new ArrayList<>();
    private final Set<String> foldedPlayers = ConcurrentHashMap.newKeySet();
    private final Map<String, Integer> playerChips = new ConcurrentHashMap<>();
    private final Map<String, Integer> playerBets = new ConcurrentHashMap<>();

    private List<TarotBoardPoker.Card> deck;
    private List<TarotBoardPoker.Card> communityCards = new ArrayList<>();

    private int dealerIndex = 0;
    private int currentTurnIndex = 0;
    private int currentBet = 0;
    private volatile int pot = 0;
    private volatile boolean running = true;

    private enum GamePhase {WAITING, PRE_FLOP, FLOP, TURN, RIVER, SHOWDOWN}

    private GamePhase phase = GamePhase.WAITING;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter IP to bind (leave empty for all interfaces): ");
        String host = scanner.nextLine().trim();
        if (host.isEmpty()) host = null;

        int port = 55555;
        while (true) {
            System.out.print("Enter port number to bind (1024-65535): ");
            String portStr = scanner.nextLine().trim();
            try {
                port = Integer.parseInt(portStr);
                if (port < 1024 || port > 65535) {
                    System.out.println("Port must be between 1024 and 65535.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number. Please enter a valid integer.");
            }
        }

        PokerServer server = new PokerServer();
        server.start(host, port);
    }

    private synchronized void checkIfBettingComplete() {
        for (String player : playerOrder) {
            if (foldedPlayers.contains(player)) continue;
            int bet = playerBets.getOrDefault(player, 0);
            if (bet != currentBet) return; // Someone hasn't matched bet yet
        }
        // All matched - proceed
        proceedToNextPhase();
    }


    public void start(String host, int port) throws IOException {
        if (host == null) {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);
        } else {
            InetAddress bindAddr = InetAddress.getByName(host);
            serverSocket = new ServerSocket(port, 50, bindAddr);
            System.out.println("Server started on " + host + ":" + port);
        }

        running = true;

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            } catch (IOException e) {
                if (running) e.printStackTrace();
                break;
            }
        }

        stop();
    }

    public void stop() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                System.out.println("Server stopped.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void broadcast(String message) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(message);
        }
    }

    private synchronized void startNewRound() {
        // Reset everything for new round
        foldedPlayers.clear();
        playerBets.clear();
        pot = 0;
        currentBet = 0;
        communityCards.clear();

        // Rotate dealer
        if (!playerOrder.isEmpty()) {
            dealerIndex = (dealerIndex + 1) % playerOrder.size();
            currentTurnIndex = (dealerIndex + 1) % playerOrder.size(); // first to act is left of dealer
        }

        // Reset player chips if new players joined
        for (String player : playerOrder) {
            playerChips.putIfAbsent(player, 1000);
            playerBets.put(player, 0);
        }

        // Prepare deck and shuffle
        deck = generateDeck();
        Collections.shuffle(deck);

        // Deal 2 hole cards to each player and send **privately**
        for (String player : playerOrder) {
            List<TarotBoardPoker.Card> hand = new ArrayList<>(deck.subList(0, 2));
            deck.subList(0, 2).clear();
            clients.get(player).hand = hand;

            List<String> cardStrings = hand.stream()
                    .map(TarotBoardPoker.Card::toString)
                    .toList();

            // Send to the specific client ONLY, NOT broadcast
            clients.get(player).sendMessage("HAND " + player + " " + String.join(",", cardStrings));
        }

        phase = GamePhase.PRE_FLOP;
        broadcast("MESSAGE New round started! Dealer is " + playerOrder.get(dealerIndex));
        broadcast("COMMUNITY"); // empty community cards
        broadcast("POT " + pot);
        broadcast("TURN " + playerOrder.get(currentTurnIndex));
    }

    private synchronized void proceedToNextPhase() {
        switch (phase) {
            case PRE_FLOP -> {
                phase = GamePhase.FLOP;
                // Deal 3 community cards
                communityCards.addAll(deck.subList(0, 3));
                deck.subList(0, 3).clear();
            }
            case FLOP -> {
                phase = GamePhase.TURN;
                // Deal 1 community card
                communityCards.add(deck.remove(0));
            }
            case TURN -> {
                phase = GamePhase.RIVER;
                // Deal 1 community card
                communityCards.add(deck.remove(0));
            }
            case RIVER -> {
                phase = GamePhase.SHOWDOWN;
            }
            default -> {
            }
        }
        broadcast("COMMUNITY " + communityCards.stream()
                .map(TarotBoardPoker.Card::toString)
                .reduce((a, b) -> a + "," + b).orElse(""));

        if (phase != GamePhase.SHOWDOWN) {
            // Reset bets and turn for new betting round
            resetBetsForNewRound();
            currentTurnIndex = (dealerIndex + 1) % playerOrder.size();
            broadcast("TURN " + playerOrder.get(currentTurnIndex));
        } else {
            // Showdown
            doShowdown();
        }
    }

    private synchronized void resetBetsForNewRound() {
        currentBet = 0;
        for (String player : playerOrder) {
            playerBets.put(player, 0);
        }
    }

    private synchronized void doShowdown() {
        // Evaluate hands of all players who didn't fold
        Map<String, TarotBoardPoker.Hand> playerHands = new HashMap<>();

        for (String player : playerOrder) {
            if (!foldedPlayers.contains(player)) {
                List<TarotBoardPoker.Card> fullHand = new ArrayList<>(communityCards);
                fullHand.addAll(clients.get(player).hand);
                TarotBoardPoker.Hand hand = TarotBoardPoker.HandEvaluator.evaluate(fullHand);
                playerHands.put(player, hand);
            }
        }

        // Find best hand
        String winner = null;
        TarotBoardPoker.Hand bestHand = null;
        for (var entry : playerHands.entrySet()) {
            if (bestHand == null || entry.getValue().compareTo(bestHand) > 0) {
                bestHand = entry.getValue();
                winner = entry.getKey();
            }
        }

        if (winner != null) {
            playerChips.put(winner, playerChips.get(winner) + pot);
            broadcast("MESSAGE " + winner + " wins the pot of " + pot + " chips with " + bestHand.getHandRank());
        } else {
            broadcast("MESSAGE No winner this round.");
        }

        pot = 0;
        broadcast("POT " + pot);
        phase = GamePhase.WAITING;
        startNewRound();
    }

    private synchronized int activePlayersCount() {
        int count = 0;
        for (String player : playerOrder) {
            if (!foldedPlayers.contains(player)) count++;
        }
        return count;
    }

    private synchronized void advanceTurn() {
        if (playerOrder.isEmpty()) return;

        int tries = 0;
        do {
            currentTurnIndex = (currentTurnIndex + 1) % playerOrder.size();
            tries++;
            if (tries > playerOrder.size()) break; // prevent infinite loops
        } while (foldedPlayers.contains(playerOrder.get(currentTurnIndex))
                || playerChips.getOrDefault(playerOrder.get(currentTurnIndex), 0) == 0);

        broadcast("TURN " + playerOrder.get(currentTurnIndex));
    }

    private List<TarotBoardPoker.Card> generateDeck() {
        List<TarotBoardPoker.Card> deck = new ArrayList<>();
        for (TarotBoardPoker.Suit suit : TarotBoardPoker.Suit.values()) {
            for (TarotBoardPoker.Value value : TarotBoardPoker.Value.values()) {
                if (value.getCategory() != TarotBoardPoker.ValueCategory.WILD) {
                    deck.add(new TarotBoardPoker.Card(suit, value));
                }
            }
        }
        for (TarotBoardPoker.Value wildValue : TarotBoardPoker.Value.values()) {
            if (wildValue.getCategory() == TarotBoardPoker.ValueCategory.WILD) {
                deck.add(new TarotBoardPoker.Card(null, wildValue));
            }
        }
        return deck;
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;
        private final BufferedReader in;
        private final PrintWriter out;
        private String playerName;
        private List<TarotBoardPoker.Card> hand = new ArrayList<>();

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String line = in.readLine();
                    if (line == null) break;

                    System.out.println("Received: " + line);
                    String[] parts = line.split(" ", 2);
                    String command = parts[0].toUpperCase();
                    String payload = parts.length > 1 ? parts[1] : "";

                    synchronized (PokerServer.this) {
                        switch (command) {
                            case "JOIN" -> {
                                if (clients.containsKey(payload)) {
                                    sendMessage("MESSAGE Name taken");
                                } else {
                                    playerName = payload;
                                    clients.put(playerName, this);
                                    playerOrder.add(playerName);
                                    playerChips.put(playerName, 1000);
                                    sendMessage("WELCOME " + playerName);
                                    broadcast("PLAYERS " + String.join(",", playerOrder));

                                    if (phase == GamePhase.WAITING && playerOrder.size() >= 2) {
                                        startNewRound();
                                    }
                                }
                            }
                            case "BET" -> {
                                // Split payload into playerName and amount
                                String[] betParts = payload.split(" ", 2);
                                if (betParts.length != 2) {
                                    sendMessage("MESSAGE Invalid BET command format");
                                    break;
                                }
                                String playerName = betParts[0];
                                String amountStr = betParts[1];

                                if (!(phase == GamePhase.PRE_FLOP || phase == GamePhase.FLOP || phase == GamePhase.TURN || phase == GamePhase.RIVER)) {
                                    sendMessage("MESSAGE Betting is not allowed now");
                                    break;
                                }

                                if (!playerName.equals(playerOrder.get(currentTurnIndex))) {
                                    sendMessage("MESSAGE Not your turn");
                                    break;
                                }

                                if (currentBet > 0) {
                                    sendMessage("MESSAGE Cannot place a new bet, must CALL or RAISE");
                                    break;
                                }

                                int betAmount;
                                try {
                                    betAmount = Integer.parseInt(amountStr);
                                } catch (NumberFormatException e) {
                                    sendMessage("MESSAGE Invalid bet amount");
                                    break;
                                }

                                if (betAmount <= 0) {
                                    sendMessage("MESSAGE Bet must be greater than zero");
                                    break;
                                }

                                int playerChipCount = playerChips.getOrDefault(playerName, 0);
                                if (betAmount > playerChipCount) {
                                    sendMessage("MESSAGE Not enough chips to bet that amount");
                                    break;
                                }

                                // Deduct chips and update bet
                                playerChips.put(playerName, playerChipCount - betAmount);
                                playerBets.put(playerName, betAmount);
                                pot += betAmount;
                                currentBet = betAmount;

                                System.out.println("Pot updated to: " + pot);

                                broadcast("BET " + playerName + " " + betAmount);
                                broadcast("POT " + pot);

                                checkIfBettingComplete();
                                if (phase != GamePhase.SHOWDOWN) {
                                    advanceTurn();
                                }
                            }

                            case "RAISE" -> {
                                // Split payload into playerName and amount
                                String[] raiseParts = payload.split(" ", 2);
                                if (raiseParts.length != 2) {
                                    sendMessage("MESSAGE Invalid RAISE command format");
                                    break;
                                }
                                String playerName = raiseParts[0];
                                String amountStr = raiseParts[1];

                                if (!(phase == GamePhase.PRE_FLOP || phase == GamePhase.FLOP || phase == GamePhase.TURN || phase == GamePhase.RIVER)) {
                                    sendMessage("MESSAGE Raising is not allowed now");
                                    break;
                                }

                                if (!playerName.equals(playerOrder.get(currentTurnIndex))) {
                                    sendMessage("MESSAGE Not your turn");
                                    break;
                                }

                                int raiseBy;
                                try {
                                    raiseBy = Integer.parseInt(amountStr);
                                } catch (NumberFormatException e) {
                                    sendMessage("MESSAGE Invalid raise amount");
                                    break;
                                }

                                if (raiseBy <= 0) {
                                    sendMessage("MESSAGE Raise must be greater than zero");
                                    break;
                                }

                                int playerChipCount = playerChips.getOrDefault(playerName, 0);
                                int playerBetSoFar = playerBets.getOrDefault(playerName, 0);
                                int toCall = currentBet - playerBetSoFar;

                                int totalNeeded = toCall + raiseBy;
                                if (totalNeeded > playerChipCount) {
                                    sendMessage("MESSAGE Not enough chips to call and raise");
                                    break;
                                }

                                // Deduct chips and update bets and pot
                                playerChips.put(playerName, playerChipCount - totalNeeded);
                                int newBet = currentBet + raiseBy;
                                playerBets.put(playerName, newBet);
                                pot += totalNeeded;
                                currentBet = newBet;

                                System.out.println("Pot updated to: " + pot);

                                broadcast("RAISE " + playerName + " " + newBet);
                                broadcast("POT " + pot);

                                checkIfBettingComplete();
                                if (phase != GamePhase.SHOWDOWN) {
                                    advanceTurn();
                                }
                            }
                            case "FOLD" -> {
                                if (!playerName.equals(playerOrder.get(currentTurnIndex))) {
                                    sendMessage("MESSAGE Not your turn");
                                    break;
                                }
                                foldedPlayers.add(playerName);
                                broadcast("FOLD " + playerName);

                                if (activePlayersCount() <= 1) {
                                    broadcast("MESSAGE Round ended due to folds.");
                                    phase = GamePhase.WAITING;
                                    startNewRound();
                                } else {
                                    advanceTurn();
                                }
                            }
                            case "CHECK" -> {
                                if (!playerName.equals(playerOrder.get(currentTurnIndex))) {
                                    sendMessage("MESSAGE Not your turn");
                                    break;
                                }

                                int playerBet = playerBets.getOrDefault(playerName, 0);
                                if (playerBet < currentBet) {
                                    sendMessage("MESSAGE Cannot check, must call or raise");
                                    break;
                                }
                                broadcast("CHECK " + playerName);
                                advanceTurn();
                            }
                            case "CALL" -> {
                                if (!playerName.equals(playerOrder.get(currentTurnIndex))) {
                                    sendMessage("MESSAGE Not your turn");
                                    break;
                                }

                                int playerBetSoFar = playerBets.getOrDefault(playerName, 0);
                                int toCall = currentBet - playerBetSoFar;

                                if (toCall <= 0) {
                                    sendMessage("MESSAGE Nothing to call, you should CHECK instead");
                                    break;
                                }

                                int chips = playerChips.getOrDefault(playerName, 0);
                                if (chips < toCall) {
                                    sendMessage("MESSAGE Not enough chips to call");
                                    break;
                                }

                                playerChips.put(playerName, chips - toCall);
                                playerBets.put(playerName, currentBet);
                                pot += toCall;

                                broadcast("CALL " + playerName);
                                broadcast("POT " + pot);

                                if (isBettingComplete()) {
                                    proceedToNextPhase();
                                } else {
                                    advanceTurn();
                                }
                            }
                            case "ENDTURN" -> {
                                if (!playerName.equals(playerOrder.get(currentTurnIndex))) {
                                    sendMessage("MESSAGE Not your turn");
                                    break;
                                }
                                advanceTurn();
                            }
                            default -> sendMessage("MESSAGE Unknown command: " + command);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Connection lost to " + playerName);
            } finally {
                synchronized (PokerServer.this) {
                    if (playerName != null) {
                        clients.remove(playerName);
                        playerOrder.remove(playerName);
                        foldedPlayers.remove(playerName);
                        playerChips.remove(playerName);
                        playerBets.remove(playerName);
                        broadcast("PLAYERS " + String.join(",", playerOrder));
                    }
                }
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private boolean isBettingComplete() {
        for (String player : playerOrder) {
            if (foldedPlayers.contains(player)) continue;
            int bet = playerBets.getOrDefault(player, 0);
            if (bet != currentBet) return false;
        }
        return true;
    }
}
