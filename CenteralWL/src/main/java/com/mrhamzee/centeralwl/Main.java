package com.mrhamzee.centeralwl;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import okhttp3.*;
import java.io.IOException;

public final class Main extends JavaPlugin {
    private static final String API_URL = "http://your-discord-bot-url/verify"; // Replace with your bot's API URL
    private static final String API_KEY = "your-secret-api-key"; // Replace with a secure key

    @Override
    public void onEnable() {
        getLogger().info("CenteralWL has been enabled!");
        saveDefaultConfig(); // Creates config.yml if not present
    }

    @Override
    public void onDisable() {
        getLogger().info("CenteralWL has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("verify")) {
            if (args.length != 1) {
                player.sendMessage("Usage: /verify <token>");
                return true;
            }

            String token = args[0];
            String minecraftUsername = player.getName();

            verifyAccount(player, token, minecraftUsername);
            return true;
        }

        return false;
    }

    private void verifyAccount(Player player, String token, String minecraftUsername) {
        OkHttpClient client = new OkHttpClient();
        String json = String.format("{\"token\": \"%s\", \"minecraftUsername\": \"%s\"}", token, minecraftUsername);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                player.sendMessage("Verification failed: Could not connect to the server.");
                getLogger().warning("API call failed: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    player.sendMessage("Verification failed: Invalid response from server.");
                    getLogger().warning("API returned: " + response.code());
                    return;
                }

                String responseBody = response.body().string();
                if (responseBody.contains("success")) {
                    player.sendMessage("You have been verified successfully!");
                } else {
                    player.sendMessage("Verification failed: " + responseBody);
                }
            }
        });
    }
}