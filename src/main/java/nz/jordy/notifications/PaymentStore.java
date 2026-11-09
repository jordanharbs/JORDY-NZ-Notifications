package nz.jordy.notifications;

import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

final class PaymentStore {
    private final Map<String, Double> totals = new HashMap<>();
    private Path file() {
        return Minecraft.getInstance().gameDirectory.toPath()
                .resolve("config").resolve("jordy-nz-notifications.properties");
    }

    synchronized void load() {
        Path path = file();
        if (!Files.exists(path)) return;
        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(path)) {
            props.load(reader);
            for (String key : props.stringPropertyNames()) {
                try { totals.put(key, Double.parseDouble(props.getProperty(key))); }
                catch (NumberFormatException ignored) {}
            }
        } catch (IOException ignored) {}
    }

    synchronized void addPayment(String player, double amount) {
        if (amount <= 0) return;
        totals.merge(player, amount, Double::sum);
        save();
    }

    synchronized List<Entry> topEntries() {
        return totals.entrySet().stream()
                .map(e -> new Entry(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingDouble(Entry::total).reversed())
                .collect(Collectors.toList());
    }

    private synchronized void save() {
        Path path = file();
        try {
            Files.createDirectories(path.getParent());
            Properties props = new Properties();
            totals.forEach((name, total) -> props.setProperty(name, Double.toString(total)));
            try (Writer writer = Files.newBufferedWriter(path)) {
                props.store(writer, "JORDY NZ Notifications payment totals");
            }
        } catch (IOException ignored) {}
    }

    static String format(double value) {
        if (value >= 1_000_000_000) return String.format(Locale.ROOT, "%.2fb", value / 1_000_000_000);
        if (value >= 1_000_000) return String.format(Locale.ROOT, "%.2fm", value / 1_000_000);
        if (value >= 1_000) return String.format(Locale.ROOT, "%.2fk", value / 1_000);
        return String.format(Locale.ROOT, "%.0f", value);
    }

    record Entry(String name, double total) {}
}
