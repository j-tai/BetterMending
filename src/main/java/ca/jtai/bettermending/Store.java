package ca.jtai.bettermending;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import org.bukkit.entity.Player;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;

public class Store {
    private final HashMap<UUID, MendingMode> playerModes = new HashMap<>();
    private transient boolean dirty = false;

    public MendingMode getMode(Player player) {
        MendingMode mode = playerModes.get(player.getUniqueId());
        return (mode == null) ? MendingMode.REPAIR : mode;
    }

    public void setMode(Player player, MendingMode mode) {
        playerModes.put(player.getUniqueId(), mode);
        dirty = true;
    }

    private static final Gson GSON = new GsonBuilder()
        .setLenient()
        .setPrettyPrinting()
        .create();

    public static Store read(Path path) {
        try (FileReader reader = new FileReader(path.toFile())) {
            return GSON.fromJson(reader, Store.class);
        } catch (FileNotFoundException e) {
            return new Store();
        } catch (IOException | JsonParseException e) {
            e.printStackTrace();
            return new Store();
        }
    }

    public void write(Path path) {
        if (!dirty)
            return;
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (FileWriter writer = new FileWriter(path.toFile())) {
            GSON.toJson(this, writer);
        } catch (IOException | JsonParseException e) {
            e.printStackTrace();
        }
        dirty = false;
    }
}
