package com.mineblock11.quickplaybackport.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.impl.game.minecraft.MinecraftGameProvider;
import net.fabricmc.loader.impl.launch.knot.Knot;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.util.Pair;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Entrypoint implements PreLaunchEntrypoint, ClientModInitializer {
    public static String[] RUN_ARGS;
    public static Logger LOGGER = LoggerFactory.getLogger("QuickplayEntrypoint");
    @Override
    public void onPreLaunch() {
        RUN_ARGS = FabricLoader.getInstance().getLaunchArguments(true);
    }

    public static Consumer<MinecraftClient> minecraftClientConsumer = (client) -> {};

    @Override
    public void onInitializeClient() {
        if(RUN_ARGS == null) {
            LOGGER.error("Run Arguments are null! Cannot process quickplay arguments.");
            return;
        }

        Pair<String, String> quickPlayArg = null;

        for (String runArg : RUN_ARGS) {
            if(runArg.contains("--quickPlaySingleplayer") || runArg.contains("--quickPlayMultiplayer") || runArg.contains("--quickPlayRealms"))
            {
                LOGGER.info("Found arg: {}", runArg);
                if(runArg.contains("=")) {
                    // Uses --arg=value format
                    String[] a = runArg.split("=");
                    String value = a[1];
                    String name = a[0];
                    quickPlayArg = new Pair<>(name, value);
                } else {
                    // Uses --arg value format
                    String name = runArg;
                    int indexOfValue = ArrayUtils.indexOf(RUN_ARGS, name) + 1;

                    if(indexOfValue > RUN_ARGS.length - 1) {
                        LOGGER.error("Argument {} doesn't have a value! Continuing as normal.", name);
                        return;
                    }

                    String value = RUN_ARGS[indexOfValue];
                    quickPlayArg = new Pair<>(name, value);
                }
                break;
            }
        }

        if(quickPlayArg == null) {
            LOGGER.info("Didn't find any quickplay arguments, continuing as normal.");
            return;
        }

        LOGGER.info("Found quickplay argument: {}={}", quickPlayArg.getLeft(), quickPlayArg.getRight());

        if(Objects.equals(quickPlayArg.getLeft(), "--quickPlaySingleplayer")) {
            final Pair<String, String> finalQuickPlayArg = quickPlayArg;
            minecraftClientConsumer = minecraftClient -> Quickplay.joinSingleplayerWorld(minecraftClient, finalQuickPlayArg.getRight());
        }

        if(Objects.equals(quickPlayArg.getLeft(), "--quickPlayMultiplayer")) {
            final Pair<String, String> finalQuickPlayArg = quickPlayArg;
            minecraftClientConsumer = minecraftClient -> Quickplay.joinMultiplayerWorld(minecraftClient, finalQuickPlayArg.getRight());
        }

        if(Objects.equals(quickPlayArg.getLeft(), "--quickPlayRealms")) {
            final Pair<String, String> finalQuickPlayArg = quickPlayArg;
            minecraftClientConsumer = minecraftClient -> Quickplay.joinRealmsWorld(minecraftClient, RealmsClient.createRealmsClient(minecraftClient), finalQuickPlayArg.getRight());
        }
    }
}
