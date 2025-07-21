package com.mystic.tarotboard;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class PokerServer {
    private ServerSocket serverSocket;
    Set<String> activePlayers = new HashSet<>();
    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final List<String> playerOrder = new ArrayList<>();
    private final Set<String> foldedPlayers = ConcurrentHashMap.newKeySet();
    private final Map<String, Integer> playerChips = new ConcurrentHashMap<>();
    private final Map<String, Integer> playerBets = new ConcurrentHashMap<>();
    Set<String> playerHasActedThisRound = new HashSet<>();
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
        activePlayers.clear();
        activePlayers.addAll(playerOrder);
        playerHasActedThisRound.clear();
        foldedPlayers.clear();
        playerBets.clear();
        pot = 0;
        currentBet = 0;
        communityCards.clear();

        // Rotate dealer and turn
        if (!playerOrder.isEmpty()) {
            dealerIndex = (dealerIndex + 1) % playerOrder.size();
            currentTurnIndex = (dealerIndex + 1) % playerOrder.size();
        }

        // Reset chips if new
        for (String player : playerOrder) {
            playerChips.putIfAbsent(player, 10000);
            playerBets.put(player, 0);
        }

        deck = generateDeck();
        Collections.shuffle(deck);

        // Send NEWROUND with player list
        String playerList = String.join(",", playerOrder);
        broadcast("NEWROUND " + playerList);

        // Deal 2 hole cards and send HAND messages
        for (String player : playerOrder) {
            List<TarotBoardPoker.Card> hand = new ArrayList<>(deck.subList(0, 2));
            deck.subList(0, 2).clear();
            clients.get(player).hand = hand;

            // Send real cards to owner
            List<String> cardStrings = hand.stream()
                    .map(TarotBoardPoker.Card::toString)
                    .toList();
            clients.get(player).sendMessage("HAND " + player + " " + String.join(",", cardStrings));

            // Send facedown cards to everyone else
            for (String recipient : playerOrder) {
                if (!recipient.equals(player)) {
                    clients.get(recipient).sendMessage("HAND " + player + " FACEDOWN,FACEDOWN");
                }
            }
        }

        phase = GamePhase.PRE_FLOP;
        broadcast("MESSAGE New round started! Dealer is " + playerOrder.get(dealerIndex));
        broadcast("COMMUNITY");
        broadcast("POT " + pot);
        broadcast("TURN " + playerOrder.get(currentTurnIndex));
    }


    private synchronized void proceedToNextPhase() {
        playerHasActedThisRound.clear();
        switch (phase) {
            case PRE_FLOP: {
                phase = GamePhase.FLOP;
                // Deal 3 community cards
                communityCards.addAll(deck.subList(0, 3));
                deck.subList(0, 3).clear();
                break;
            }
            case FLOP: {
                phase = GamePhase.TURN;
                // Deal 1 community card
                communityCards.add(deck.remove(0));
                break;
            }
            case TURN: {
                phase = GamePhase.RIVER;
                // Deal 1 community card
                communityCards.add(deck.remove(0));
                break;
            }
            case RIVER: {
                phase = GamePhase.SHOWDOWN;
                break;
            }
            default:
                break;
        }

        broadcast("COMMUNITY " + communityCards.stream()
                .map(TarotBoardPoker.Card::toString)
                .reduce((a, b) -> a + "," + b).orElse(""));

        if (phase != GamePhase.SHOWDOWN) {
            resetBetsForNewRound();
            currentTurnIndex = (dealerIndex + 1) % playerOrder.size();
            broadcast("TURN " + playerOrder.get(currentTurnIndex));
        } else {
            doShowdown();
        }
    }

    private synchronized void broadcastChips() {
        String chipsLine = playerOrder.stream()
                .map(p -> p + "=" + playerChips.getOrDefault(p, 0))
                .collect(Collectors.joining(","));
        broadcast("CHIPS " + chipsLine);
    }

    private synchronized void resetBetsForNewRound() {
        currentBet = 0;
        for (String player : playerOrder) {
            playerBets.put(player, 0);
        }
    }

    private synchronized void doShowdown() {
        // 1️⃣ Evaluate active hands
        Map<String, TarotBoardPoker.Hand> playerHands = new HashMap<>();
        for (String player : playerOrder) {
            if (!foldedPlayers.contains(player)) {
                List<TarotBoardPoker.Card> fullHand = new ArrayList<>(communityCards);
                fullHand.addAll(clients.get(player).hand);
                TarotBoardPoker.Hand hand = TarotBoardPoker.HandEvaluator.evaluate(fullHand);
                playerHands.put(player, hand);
            }
        }

        if (playerHands.isEmpty()) {
            // Only one player left (all others folded earlier)
            String lastStanding = playerOrder.stream()
                    .filter(p -> !foldedPlayers.contains(p))
                    .findFirst().orElse(null);
            if (lastStanding != null) {
                playerChips.put(lastStanding, playerChips.getOrDefault(lastStanding, 0) + pot);
                broadcast("MESSAGE " + lastStanding + " wins the pot of " + pot + " chips by default!");
            } else {
                broadcast("MESSAGE Nobody left to win the pot!");
            }
            pot = 0;
            broadcast("POT " + pot);
            broadcastChips();
            phase = GamePhase.WAITING;
            startNewRound();
            return;
        }

        // 2️⃣ Reveal hole cards for all
        for (String player : playerHands.keySet()) {
            List<TarotBoardPoker.Card> hand = clients.get(player).hand;
            List<String> cardStrings = hand.stream()
                    .map(TarotBoardPoker.Card::toString)
                    .toList();
            broadcast("HAND " + player + " " + String.join(",", cardStrings));
        }

        // 3️⃣ Determine best score
        int bestScore = playerHands.values().stream()
                .mapToInt(TarotBoardPoker.Hand::getScore)
                .max().orElse(-1);

        // 4️⃣ Collect all winners with best score
        List<String> winners = playerHands.entrySet().stream()
                .filter(e -> e.getValue().getScore() == bestScore)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 5️⃣ Split pot safely
        int splitPot = winners.isEmpty() ? 0 : pot / winners.size();
        int leftover = pot - (splitPot * winners.size());

        for (String winner : winners) {
            playerChips.put(winner, playerChips.getOrDefault(winner, 0) + splitPot);
        }

        // 6️⃣ Give leftover to first winner (rounding)
        if (leftover > 0 && !winners.isEmpty()) {
            String firstWinner = winners.get(0);
            playerChips.put(firstWinner, playerChips.get(firstWinner) + leftover);
        }

        // 7️⃣ Announce winner(s) with hand details
        if (!winners.isEmpty()) {
            String bestHandName = playerHands.get(winners.get(0)).getHandRank().name();
            String bestHandScore = String.valueOf(bestScore);
            broadcast("MESSAGE Winner(s): " + String.join(", ", winners)
                    + " — " + bestHandName + " (Score: " + bestHandScore + ") "
                    + " — Pot split: " + splitPot
                    + (leftover > 0 ? " + leftover: " + leftover : ""));
        } else {
            broadcast("MESSAGE No valid winner found. Pot lost!");
        }

        // 8️⃣ Reset pot and update chips
        pot = 0;
        broadcast("POT " + pot);
        broadcastChips();

        // 9️⃣ New round
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
                    deck.add(new TarotBoardPoker.Card(suit, value, false));
                }
            }
        }
        for (TarotBoardPoker.Value wildValue : TarotBoardPoker.Value.values()) {
            if (wildValue.getCategory() == TarotBoardPoker.ValueCategory.WILD) {
                deck.add(new TarotBoardPoker.Card(null, wildValue, false));
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
                                    playerChips.putIfAbsent(playerName, 10000);
                                    sendMessage("WELCOME " + playerName);
                                    broadcast("PLAYERS " + String.join(",", playerOrder));

                                    if (phase == GamePhase.WAITING && playerOrder.size() >= 2) {
                                        startNewRound();
                                    } else if (phase != GamePhase.WAITING) {
                                        // Mid-round join: deal hole cards to new player
                                        hand = new ArrayList<>(deck.subList(0, 2));
                                        deck.subList(0, 2).clear();

                                        // Send hole cards to the new player
                                        List<String> cardStrings = hand.stream()
                                                .map(TarotBoardPoker.Card::toString)
                                                .toList();
                                        sendMessage("HAND " + playerName + " " + String.join(",", cardStrings));

                                        // Send community cards to the new player
                                        if (!communityCards.isEmpty()) {
                                            sendMessage("COMMUNITY " + String.join(",", communityCards.stream()
                                                    .map(TarotBoardPoker.Card::toString).toList()));
                                        } else {
                                            sendMessage("COMMUNITY");
                                        }

                                        // Send pot and turn info
                                        sendMessage("POT " + pot);
                                        sendMessage("TURN " + playerOrder.get(currentTurnIndex));

                                        // Send facedown info about other players' hole cards (if needed)
                                        for (String otherPlayer : playerOrder) {
                                            if (!otherPlayer.equals(playerName)) {
                                                sendMessage("HAND " + otherPlayer + " FACEDOWN,FACEDOWN");
                                            }
                                        }

                                        // Send current chips too
                                        broadcastChips();
                                    }
                                }
                            }

                            case "BET" -> {
                                String[] betParts = payload.split(" ", 2);
                                if (betParts.length != 2) {
                                    sendMessage("MESSAGE Invalid BET command format");
                                    break;
                                }
                                String name = betParts[0];
                                int betAmount = parseAmount(betParts[1], "bet");
                                if (betAmount <= 0) break;

                                if (checkTurn(name)) break;
                                if (currentBet > 0) {
                                    sendMessage("MESSAGE Cannot place a new bet, must CALL or RAISE");
                                    break;
                                }

                                if (deductChips(name, betAmount)) break;
                                broadcastChips();

                                playerBets.put(name, betAmount);
                                pot += betAmount;
                                currentBet = betAmount;

                                playerHasActedThisRound.add(name);

                                broadcast("BET " + name + " " + betAmount);
                                broadcast("POT " + pot);
                                broadcastChips();

                                finishActionOrAdvance();
                            }

                            case "RAISE" -> {
                                String[] raiseParts = payload.split(" ", 2);
                                if (raiseParts.length != 2) {
                                    sendMessage("MESSAGE Invalid RAISE command format");
                                    break;
                                }
                                String name = raiseParts[0];
                                int raiseBy = parseAmount(raiseParts[1], "raise");
                                if (raiseBy <= 0) break;

                                if (checkTurn(name)) break;

                                int betSoFar = playerBets.getOrDefault(name, 0);
                                int toCall = currentBet - betSoFar;
                                int totalNeeded = toCall + raiseBy;

                                if (deductChips(name, totalNeeded)) break;
                                broadcastChips();

                                currentBet += raiseBy;
                                playerBets.put(name, currentBet);
                                pot += totalNeeded;

                                playerHasActedThisRound.add(name);

                                broadcast("RAISE " + name + " " + raiseBy + " " + toCall);
                                broadcast("POT " + pot);
                                broadcastChips();

                                finishActionOrAdvance();
                            }

                            case "CALL" -> {
                                if (checkTurn(playerName)) break;

                                int betSoFar = playerBets.getOrDefault(playerName, 0);
                                int toCall = currentBet - betSoFar;
                                if (toCall <= 0) {
                                    sendMessage("MESSAGE Nothing to call, use CHECK instead");
                                    break;
                                }
                                if (deductChips(playerName, toCall)) break;
                                broadcastChips();

                                playerBets.put(playerName, currentBet);
                                pot += toCall;

                                playerHasActedThisRound.add(playerName);

                                broadcast("CALL " + playerName);
                                broadcast("POT " + pot);
                                broadcastChips();

                                finishActionOrAdvance();
                            }

                            case "CHECK" -> {
                                if (checkTurn(playerName)) break;

                                int betSoFar = playerBets.getOrDefault(playerName, 0);
                                if (betSoFar < currentBet) {
                                    sendMessage("MESSAGE Cannot check, must CALL or RAISE");
                                    break;
                                }

                                playerHasActedThisRound.add(playerName);

                                broadcast("CHECK " + playerName);
                                finishActionOrAdvance();
                            }

                            case "FOLD" -> {
                                if (!checkTurn(playerName)) {
                                    foldedPlayers.add(playerName);
                                    playerHasActedThisRound.add(playerName);

                                    // Remove from active players if you have a list/set
                                    activePlayers.remove(playerName);  // Make sure activePlayers is a modifiable list or set

                                    broadcast("FOLD " + playerName);

                                    if (activePlayers.size() == 1) {
                                        String winner = activePlayers.iterator().next();  // or get(0) if list
                                        playerChips.put(winner, playerChips.getOrDefault(winner, 0) + pot);
                                        broadcast("MESSAGE " + winner + " wins the pot of " + pot + " chips by default!");
                                        broadcast("POT " + pot);  // Broadcast pot before resetting
                                        broadcastChips();
                                        startNewRound();
                                    } else {
                                        advanceTurn();
                                    }
                                }
                            }


                            default -> sendMessage("MESSAGE Unknown command: " + command);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Connection lost to " + playerName);
            } finally {
                handleDisconnect();
            }
        }

        private int parseAmount(String str, String type) {
            try {
                int amt = Integer.parseInt(str);
                if (amt <= 0) {
                    sendMessage("MESSAGE " + type + " amount must be positive");
                    return -1;
                }
                return amt;
            } catch (NumberFormatException e) {
                sendMessage("MESSAGE Invalid " + type + " amount");
                return -1;
            }
        }

        private boolean deductChips(String player, int amount) {
            int current = playerChips.getOrDefault(player, 0);

            if (amount > current) {
                // Not enough chips — deny and notify this player
                ClientHandler handler = clients.get(player);
                if (handler != null) {
                    handler.sendMessage("MESSAGE Not enough chips. You have: " + current);
                }
                return true;
            }

            // Deduct and save new value
            playerChips.put(player, current - amount);
            return false;
        }

        private boolean checkTurn(String name) {
            if (!name.equals(playerOrder.get(currentTurnIndex))) {
                sendMessage("MESSAGE Not your turn");
                return true;
            }
            return false;
        }

        private void finishActionOrAdvance() {
            if (isBettingComplete()) {
                proceedToNextPhase();
            } else {
                advanceTurn();
            }
        }

        private void handleDisconnect() {
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

        private boolean isBettingComplete() {
            for (String player : playerOrder) {
                if (foldedPlayers.contains(player)) continue;

                int bet = playerBets.getOrDefault(player, 0);

                // 1. If their bet is lower → they must act
                if (bet < currentBet) return false;

                // 2. If their bet is equal but they never acted, they could still raise.
                if (!playerHasActedThisRound.contains(player)) return false;
            }
            return true;
        }

    }
}
