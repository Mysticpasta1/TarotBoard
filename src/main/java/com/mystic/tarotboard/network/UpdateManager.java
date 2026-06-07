package com.mystic.tarotboard.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.function.Consumer;

/**
 * Checks for application updates via GitHub Releases and downloads/installs them.
 */
public class UpdateManager {
    private UpdateManager() {
    }

    /**
     * Current local version identifier.
     */
    public static final String CURRENT_VERSION = "3.0.0";
    private static final String GITHUB_API = "https://api.github.com/repos/Mysticpasta1/TarotBoard/releases/latest";

    /**
     * Information about a remote release.
     *
     * @param version     the version tag string
     * @param downloadUrl URL to download the release asset
     * @param publishedAt publication timestamp
     */
    public record ReleaseInfo(String version, String downloadUrl, String publishedAt) {
    }

    /**
     * Checks the GitHub API for the latest release.
     *
     * @return ReleaseInfo if a newer release is found and an MSI asset exists, null otherwise
     * @throws IOException          if the HTTP request fails
     * @throws InterruptedException if the request is interrupted
     */
    public static ReleaseInfo checkForUpdate() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GITHUB_API))
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "TarotBoard-Updater")
                .timeout(Duration.ofSeconds(10))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) return null;

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        String tagName = json.get("tag_name").getAsString();
        String publishedAt = json.get("published_at").getAsString();
        JsonArray assets = json.getAsJsonArray("assets");

        String downloadUrl = null;
        for (var asset : assets) {
            JsonObject obj = asset.getAsJsonObject();
            String name = obj.get("name").getAsString();
            if (name.endsWith(".msi")) {
                downloadUrl = obj.get("browser_download_url").getAsString();
                break;
            }
        }
        if (downloadUrl == null) return null;

        String version = tagName.startsWith("v") ? tagName.substring(1) : tagName;
        return new ReleaseInfo(version, downloadUrl, publishedAt);
    }

    /**
     * Compares a version string against the current local version.
     *
     * @param latestVersion the remote version string to compare
     * @return true if the remote version is greater than the local version
     */
    public static boolean isNewerVersion(String latestVersion) {
        try {
            String[] local = CURRENT_VERSION.split("\\.");
            String[] remote = latestVersion.split("\\.");
            int len = Math.max(local.length, remote.length);
            for (int i = 0; i < len; i++) {
                int l = i < local.length ? Integer.parseInt(local[i]) : 0;
                int r = i < remote.length ? Integer.parseInt(remote[i]) : 0;
                if (r > l) return true;
                if (r < l) return false;
            }
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Downloads the update MSI from the given URL to a temporary file.
     *
     * @param url              the download URL for the MSI
     * @param progressCallback callback receiving download progress as a 0.0-1.0 fraction, or null
     * @return path to the downloaded temporary file
     * @throws IOException          if the download fails
     * @throws InterruptedException if the download is interrupted
     */
    public static Path downloadUpdate(String url, Consumer<Double> progressCallback) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "TarotBoard-Updater")
                .timeout(Duration.ofMinutes(5))
                .build();

        Path tempFile = Files.createTempFile("tarotboard-update-", ".msi");
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        long contentLength = response.headers().firstValueAsLong("Content-Length").orElse(-1L);

        try (InputStream in = response.body(); OutputStream out = Files.newOutputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            long totalBytesRead = 0;
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                if (contentLength > 0 && progressCallback != null) {
                    progressCallback.accept((double) totalBytesRead / contentLength);
                }
            }
        }
        return tempFile;
    }

    /**
     * Launches msiexec to silently install the downloaded MSI.
     *
     * @param msiPath path to the MSI file
     * @throws IOException if the process fails to start
     */
    public static void installUpdate(Path msiPath) throws IOException {
        String path = msiPath.toAbsolutePath().toString();
        try (Process process = new ProcessBuilder("msiexec.exe", "/i", path, "/passive", "/norestart").start()) {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("msiexec installation failed with exit code " + exitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Installation interrupted", e);
        }
    }
}
