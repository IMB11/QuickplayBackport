package com.mineblock11.quickplaybackport.client.mixin;

import com.mineblock11.quickplaybackport.client.Entrypoint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    @Unique
    private boolean hasAlreadyQuickplayed = false;

    @Inject(method = "init", cancellable = false, at = @At("TAIL"))
    public void $Quickplay_TitleScreenInitInject(CallbackInfo ci) {
        if (!hasAlreadyQuickplayed) {
            hasAlreadyQuickplayed = true;
            Entrypoint.minecraftClientConsumer.accept(MinecraftClient.getInstance());
        }
    }
}
