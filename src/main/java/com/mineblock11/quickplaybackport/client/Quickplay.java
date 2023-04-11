package com.mineblock11.quickplaybackport.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.SaveLevelScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.client.realms.RealmsClient;
import net.minecraft.client.realms.dto.RealmsServer;
import net.minecraft.client.realms.dto.RealmsServerList;
import net.minecraft.client.realms.exception.RealmsServiceException;
import net.minecraft.client.realms.gui.screen.RealmsLongRunningMcoTaskScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.client.realms.task.RealmsGetServerDetailsTask;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TranslatableText;

import java.util.concurrent.locks.ReentrantLock;

public class Quickplay {
    public static void joinSingleplayerWorld(MinecraftClient minecraft, String string) {
        Entrypoint.LOGGER.info("Attempting to join singleplayer world @ {}", string);

        if (!minecraft.getLevelStorage().levelExists(string)) {
            Screen screen = new SelectWorldScreen(new TitleScreen());
            Entrypoint.LOGGER.error("Singleplayer world does not exist!");
            minecraft.setScreen(screen);
        } else {
            minecraft.setScreenAndRender(new SaveLevelScreen(new TranslatableText("selectWorld.data_read")));
            minecraft.startIntegratedServer(string);
        }
    }

    public static void joinMultiplayerWorld(MinecraftClient minecraft, String string) {
        Entrypoint.LOGGER.info("Attempting to join multiplayer server @ {}", string);

        ServerList serverList = new ServerList(minecraft);
        serverList.loadFile();

        ServerInfo serverInfo = new ServerInfo(I18n.translate("selectServer.defaultName"), string, false);
        serverList.add(serverInfo);
        serverList.saveFile();

        ServerAddress serverAddress = ServerAddress.parse(string);
        ConnectScreen.connect(new MultiplayerScreen(new TitleScreen()), minecraft, serverAddress, serverInfo);
    }

    public static void joinRealmsWorld(MinecraftClient minecraft, RealmsClient realmsClient, String string) {
        Entrypoint.LOGGER.info("Attempting to join realm @ {}", string);
        long realmID;
        RealmsServerList realmsServerList;
        TitleScreen titleScreen;
        RealmsMainScreen screen;
        try {
            realmID = Long.parseLong(string);
            realmsServerList = realmsClient.listWorlds();
        } catch (NumberFormatException ignored) {
            screen = new RealmsMainScreen(new TitleScreen());
            Entrypoint.LOGGER.error("Realm does not exist. {} is an invalid realm id.", string);
            minecraft.setScreen(screen);
            return;
        } catch (RealmsServiceException exception) {
            titleScreen = new TitleScreen();
            Entrypoint.LOGGER.error("Failed to connect to realms service");
            Entrypoint.LOGGER.error(exception.toString());
            minecraft.setScreen(titleScreen);
            return;
        }

        RealmsServer realmsServer = realmsServerList.servers.stream().filter((realmsServerx) -> realmsServerx.id == realmID).findFirst().orElse(null);
        if (realmsServer == null) {
            screen = new RealmsMainScreen(new TitleScreen());
            Entrypoint.LOGGER.error("Realm attempted to connect. Server is null.");
            minecraft.setScreen(screen);
        } else {
            titleScreen = new TitleScreen();
            RealmsGetServerDetailsTask getServerDetailsTask = new RealmsGetServerDetailsTask(new RealmsMainScreen(titleScreen), titleScreen, realmsServer, new ReentrantLock());
            minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(titleScreen, getServerDetailsTask));
        }
    }
}
