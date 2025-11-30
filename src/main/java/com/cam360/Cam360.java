package com.cam360;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Cam360 implements ClientModInitializer {
    private static KeyBinding captureKey;

    // State for capture loop
    private boolean capturing = false;
    private Iterator<Float> yawIterator;
    private float originalYaw;
    private File folder;

    @Override
    public void onInitializeClient() {
        // Register keybind
        captureKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.cam360.capture",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F12, // default key, can be changed in controls
            "category.cam360"
        ));

        // Tick handler
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Start capture when key pressed
            while (captureKey.wasPressed()) {
                startCapture(client);
            }

            // Process capture loop
            if (capturing && yawIterator != null && client.player != null) {
                if (yawIterator.hasNext()) {
                    float newYaw = yawIterator.next();
                    client.player.setYaw(newYaw);

                    String filename = "360_" + System.currentTimeMillis() + "_" + ((int)newYaw % 360) + ".png";
                    ScreenshotRecorder.saveScreenshot(folder, filename, client.getFramebuffer(), text -> {});
                } else {
                    // Done
                    client.player.setYaw(originalYaw);
                    capturing = false;
                    client.player.sendMessage(Text.literal("Captured 360° screenshots!"), false);
                }
            }
        });

        System.out.println("[Cam360] Client-side mod initialized!");
    }

    private void startCapture(MinecraftClient client) {
        if (client.player == null) return;

        originalYaw = client.player.getYaw();
        folder = new File(client.runDirectory, "screenshots360");
        if (!folder.exists()) folder.mkdirs();

        List<Float> yawSteps = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            yawSteps.add(originalYaw + (i * 45));
        }
        yawIterator = yawSteps.iterator();
        capturing = true;
    }
}
