package com.ascend.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Local client data only. No shared server storage is available in a client-only mod. */
public final class AscendData {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Pattern MONEY = Pattern.compile("(?i)(?:([A-Za-z0-9_]{1,16})\\s+(?:hat dir|paid you|has paid you|gab dir)\\s+)([0-9][0-9,.]*)");
    public final List<StreamEntry> streams = new ArrayList<>();
    public final List<BidEntry> bids = new ArrayList<>();
    public String clanMembers = "Noch nicht erkannt";
    public String coOwners = "Noch nicht erkannt";
    public String moderators = "Noch nicht erkannt";
    public String lastPayment = "Noch keine passende Zahlung im Chat erkannt";
    public String auctionItem = "";
    public String minimumBid = "1000";
    public int durationSeconds = 60;
    public long auctionEndsAt = 0;
    public boolean auctionActive = false;
    public String auctionOwner = "";
    public long highestBid = 0;

    private Path file() { return MinecraftClient.getInstance().runDirectory.toPath().resolve("config/ascend-client.json"); }
    public void load() {
        try {
            Path p = file(); if (!Files.exists(p)) return;
            Saved s = GSON.fromJson(Files.readString(p), Saved.class);
            if (s != null && s.streams != null) streams.addAll(s.streams);
        } catch (Exception ignored) { }
    }
    public void save() {
        try {
            Files.createDirectories(file().getParent());
            Saved s = new Saved(); s.streams = streams;
            Files.writeString(file(), GSON.toJson(s));
        } catch (IOException ignored) { }
    }
    public void onChat(String line) {
        String lower = line.toLowerCase();
        // Best-effort parsing: HugoSMP's exact message format may differ.
        if (lower.contains("clan") && (lower.contains("mitglied") || lower.contains("member"))) {
            capture(line, "(?i)(?:mitglieder|members?)\\D{0,8}(\\d+)", 0, v -> clanMembers = v);
            capture(line, "(?i)co[- ]?owner\\D{0,8}(\\d+)", 0, v -> coOwners = v);
            capture(line, "(?i)moderatoren|moderators?\\D{0,8}(\\d+)", 0, v -> moderators = v);
        }
        Matcher m = MONEY.matcher(line);
        if (m.find()) {
            lastPayment = m.group(1) + " hat dir " + m.group(2) + " gegeben";
            try {
                long amount = Long.parseLong(m.group(2).replaceAll("[,.]", ""));
                if (auctionActive && amount >= highestBid) {
                    highestBid = amount;
                    bids.add(0, new BidEntry(m.group(1), amount));
                }
            } catch (NumberFormatException ignored) { }
        }
    }
    private interface Setter { void set(String s); }
    private void capture(String text, String regex, int ignored, Setter setter) {
        Matcher m = Pattern.compile(regex).matcher(text); if (m.find()) setter.set(m.group(1));
    }
    public void startAuction(String item, long min, int seconds) {
        auctionItem = item; minimumBid = Long.toString(min); durationSeconds = seconds;
        auctionOwner = MinecraftClient.getInstance().player == null ? "Unbekannt" : MinecraftClient.getInstance().player.getName().getString();
        highestBid = 0; bids.clear(); auctionEndsAt = System.currentTimeMillis() + seconds * 1000L; auctionActive = true;
    }
    public void tick() { if (auctionActive && System.currentTimeMillis() >= auctionEndsAt) auctionActive = false; }
    public long remainingSeconds() { return Math.max(0, (auctionEndsAt - System.currentTimeMillis() + 999) / 1000); }
    public static final class StreamEntry { public String player, url; public StreamEntry(String p, String u) { player=p; url=u; } }
    public static final class BidEntry { public String player; public long amount; public BidEntry(String p, long a) { player=p; amount=a; } }
    private static final class Saved { List<StreamEntry> streams; }
}
