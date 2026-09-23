package dev.jefftastic.mixin;

import dev.jefftastic.JeffyTimeWeaverAddon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Environment(EnvType.CLIENT)
@Mixin(WorldClient.class)
abstract public class WorldClientMixin {
    @Unique private double clientTimeBuffer = 0D;

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/WorldClient;setWorldTime(J)V", ordinal = 0))
    private void setTimeAdjusted(WorldClient instance, long par1) {
        this.clientTimeBuffer = JeffyTimeWeaverAddon.updateTime(instance, par1, this.clientTimeBuffer);
    }
}
