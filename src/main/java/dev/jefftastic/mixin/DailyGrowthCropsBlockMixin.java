package dev.jefftastic.mixin;

import api.block.blocks.DailyGrowthCropsBlock;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.jefftastic.JeffyTimeWeaverAddon;
import net.minecraft.src.World;
import net.minecraft.src.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = DailyGrowthCropsBlock.class)
public class DailyGrowthCropsBlockMixin {
    @ModifyExpressionValue(method = "attemptToGrow", at = @At(value = "INVOKE", target = "Lapi/block/blocks/DailyGrowthCropsBlock;getHasGrownToday(Lnet/minecraft/src/IBlockAccess;III)Z"))
    private boolean forceGrowthRT(boolean original, @Local(argsOnly = true) World world) {
        WorldInfo info = world.getWorldInfo();
        boolean isRealTime = info.getData(JeffyTimeWeaverAddon.REAL_TIME);
        return !isRealTime && original;
    }

    @ModifyReturnValue(method = "getBaseGrowthChance", at = @At("RETURN"))
    private float lowerGrowthRateRT(float original, @Local(argsOnly = true) World world) {
        WorldInfo info = world.getWorldInfo();
        boolean isRealTime = info.getData(JeffyTimeWeaverAddon.REAL_TIME);
        return isRealTime ? original / 8F : original;
    }
}
