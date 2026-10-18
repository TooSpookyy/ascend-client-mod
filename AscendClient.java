package com.ascend.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class AscendClient implements ClientModInitializer {
    public static final String DISCORD = "https://discord.gg/Rg3MWpDYf7";
    public static final AscendData DATA = new AscendData();
    private static KeyBinding openKey;

    @Override public void onInitializeClient() {
        DATA.load();
        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.ascend.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_K, "category.ascend"));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.wasPressed()) {
                if (client.currentScreen == null) client.setScreen(new AscendScreen());
            }
            DATA.tick();
        });
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> DATA.onChat(message.getString()));
    }

    public static void say(String message) {
        if (net.minecraft.client.MinecraftClient.getInstance().player != null)
            net.minecraft.client.MinecraftClient.getInstance().player.sendMessage(Text.literal(message), false);
    }
}
