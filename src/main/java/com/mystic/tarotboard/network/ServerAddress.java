package com.mystic.tarotboard.network;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * A parsed server location: a host (hostname, subdomain, IPv4 or IPv6 literal) and a TCP port.
 * <p>
 * Accepts the forms players actually paste in, so a single field can carry either just a host
 * or a host and port together:
 * <pre>
 *   play.example.com          host only, uses the fallback port
 *   eu.play.example.com:7777  subdomain with an explicit port
 *   tarotboard://host:7777/   scheme and trailing path are ignored
 *   192.168.1.20:5555         IPv4
 *   [2001:db8::1]:5555        bracketed IPv6 with a port
 *   2001:db8::1               bare IPv6, uses the fallback port
 * </pre>
 */
public record ServerAddress(String host, int port) {
    /** Default port, both for joining and for a player hosting a game from their own machine. */
    public static final int DEFAULT_PORT = 5555;

    /**
     * The public dedicated server, as allocated by its Craftlands hosting panel. The dedicated
     * server defaults to this port when nothing else says otherwise, so it binds the port that
     * panel actually forwards rather than {@link #DEFAULT_PORT}.
     */
    public static final ServerAddress PUBLIC_SERVER = new ServerAddress("45.137.199.179", 25605);

    private static final Pattern SCHEME = Pattern.compile("^[a-zA-Z][a-zA-Z0-9+.-]*://");
    // A hostname label is alphanumeric with inner hyphens; a name is one or more labels, so
    // arbitrarily deep subdomains are fine. A trailing root dot is allowed.
    private static final Pattern HOSTNAME =
            Pattern.compile("^(?=.{1,253}\\.?$)[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?"
                    + "(\\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*\\.?$");
    private static final Pattern IPV6 = Pattern.compile("^[0-9a-fA-F:]+(%[a-zA-Z0-9._~-]+)?$");
    private static final Pattern DIGITS = Pattern.compile("^\\d{1,5}$");

    /**
     * Parses user-entered text into a host and port.
     *
     * @param text        the address as typed; may include a scheme, port, or trailing path
     * @param fallbackPort the port to use when {@code text} carries none
     * @return the parsed address
     * @throws IllegalArgumentException if the host or port is missing or malformed, with a
     *                                  message suitable for showing to the player
     */
    public static ServerAddress parse(String text, int fallbackPort) {
        String s = text == null ? "" : text.trim();
        if (s.isEmpty()) throw new IllegalArgumentException("Enter a server address");

        s = SCHEME.matcher(s).replaceFirst("");
        int slash = s.indexOf('/');
        if (slash >= 0) s = s.substring(0, slash);
        int at = s.lastIndexOf('@');
        if (at >= 0) s = s.substring(at + 1);
        if (s.isEmpty()) throw new IllegalArgumentException("Enter a server address");

        String host;
        String portText = null;
        if (s.startsWith("[")) {
            int close = s.indexOf(']');
            if (close < 0) throw new IllegalArgumentException("Unclosed [ in address");
            host = s.substring(1, close);
            String rest = s.substring(close + 1);
            if (rest.startsWith(":")) portText = rest.substring(1);
            else if (!rest.isEmpty()) throw new IllegalArgumentException("Invalid address: " + text.trim());
        } else {
            int colon = s.lastIndexOf(':');
            // More than one colon and no brackets means a bare IPv6 literal, which has no port.
            if (colon >= 0 && s.indexOf(':') == colon) {
                host = s.substring(0, colon);
                portText = s.substring(colon + 1);
            } else {
                host = s;
            }
        }

        if (host.isEmpty()) throw new IllegalArgumentException("Enter a server address");
        if (!isValidHost(host)) throw new IllegalArgumentException("Invalid server address: " + host);

        int port = fallbackPort;
        if (portText != null && !portText.isEmpty()) port = parsePort(portText);
        if (port < 1 || port > 65535) throw new IllegalArgumentException("Port must be 1-65535");
        return new ServerAddress(host.toLowerCase(Locale.ROOT), port);
    }

    /**
     * Resolves the port a server should listen on, in the order a hosting provider expects:
     * an explicit {@code --port=N}, {@code --port N}, {@code -p N} or bare numeric argument
     * first, then the {@code PORT} environment variable that most hosting panels use to hand a
     * server its allocated port, then the {@link #PUBLIC_SERVER} port.
     * <p>
     * A resolved value of {@code 0} is returned as-is, meaning "bind any free port".
     *
     * @param args the command-line arguments
     * @return the port to bind, or 0 to let the system allocate one
     */
    public static int resolvePort(List<String> args) {
        String value = null;
        for (int i = 0; i < args.size(); i++) {
            String arg = args.get(i);
            if (arg.startsWith("--port=")) {
                value = arg.substring("--port=".length());
            } else if ((arg.equals("--port") || arg.equals("-p")) && i + 1 < args.size()) {
                value = args.get(++i);
            } else if (arg.startsWith("-") && i + 1 < args.size() && !args.get(i + 1).startsWith("-")) {
                i++; // another flag's value, e.g. --password 1234, which is not a port
            } else if (!arg.startsWith("-") && DIGITS.matcher(arg).matches()) {
                value = arg;
            }
        }
        if (value == null) value = System.getenv("PORT");
        if (value == null || value.isBlank()) return PUBLIC_SERVER.port();
        if (value.trim().equals("0")) return 0;
        try {
            return parsePort(value);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage() + " - falling back to " + PUBLIC_SERVER.port());
            return PUBLIC_SERVER.port();
        }
    }

    /**
     * Parses a port number on its own.
     *
     * @param text the port as typed
     * @return the port number
     * @throws IllegalArgumentException if the text is not a number in range
     */
    public static int parsePort(String text) {
        String s = text == null ? "" : text.trim();
        int port;
        try {
            port = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port: " + s);
        }
        if (port < 1 || port > 65535) throw new IllegalArgumentException("Port must be 1-65535");
        return port;
    }

    private static boolean isValidHost(String host) {
        if (host.contains(":")) return IPV6.matcher(host).matches();
        return HOSTNAME.matcher(host).matches();
    }

    /**
     * Formats the address for display, bracketing IPv6 literals.
     *
     * @return the address as {@code host:port}
     */
    @Override
    public String toString() {
        return (host.contains(":") ? "[" + host + "]" : host) + ":" + port;
    }
}
