package dev.jefftastic.mixin;

import dev.jefftastic.JeffyTimeWeaverAddon;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldServer.class)
abstract public class WorldServerMixin {
    @Unique private double serverTimeBuffer = 0D;

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/src/WorldInfo;setWorldTime(J)V", ordinal = 0))
    private void setTimeAdjusted(WorldInfo instance, long par1) {
        if (!(instance instanceof DerivedWorldInfo)) {
            this.serverTimeBuffer = JeffyTimeWeaverAddon.updateTime(instance, par1, this.serverTimeBuffer);
        }

    }
}
