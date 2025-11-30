package com.cam360;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.util.ScreenshotRecorder;
import org.lwjgl.glfw.GLFW;

import java.io.File;

public class Cam360 implements ClientModInitializer {
    private static KeyBinding captureKey;

    @Override
    public void onInitializeClient() {
        captureKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.cam360.capture",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F9,
            "category.cam360"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (captureKey.wasPressed()) {
                capture360(client);
            }
        });

        System.out.println("[Cam360] Client-side mod initialized!");
    }

    private void capture360(MinecraftClient client) {
    if (client.player == null) return;

    float originalYaw = client.player.getYaw();
    File folder = new File(client.runDirectory, "screenshots360");
    if (!folder.exists()) folder.mkdirs();

    List<Float> yawSteps = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
        yawSteps.add(originalYaw + (i * 45));
    }

    Iterator<Float> yawIterator = yawSteps.iterator();

    ClientTickEvents.END_CLIENT_TICK.register(new ClientTickEvents.EndTick() {
        @Override
        public void onEndTick(MinecraftClient client) {
            if (client.player == null) return;

            if (yawIterator.hasNext()) {
                float newYaw = yawIterator.next();
                client.player.setYaw(newYaw);

                String filename = "360_" + System.currentTimeMillis() + "_" + ((int)newYaw % 360) + ".png";
                ScreenshotRecorder.saveScreenshot(folder, filename, client.getFramebuffer(), text -> {});
            } else {
                client.player.setYaw(originalYaw);
                ClientTickEvents.END_CLIENT_TICK.unregister(this);
                client.player.sendMessage(Text.literal("Captured 360° screenshots!"), false);
            }
        }
    });
}


        client.player.setYaw(originalYaw);
    }
}
