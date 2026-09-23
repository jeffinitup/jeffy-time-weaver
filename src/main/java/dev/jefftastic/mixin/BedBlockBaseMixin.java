package dev.jefftastic.mixin;

import btw.block.blocks.BedBlockBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(BedBlockBase.class)
public class BedBlockBaseMixin {
    @Inject(method = "onBlockActivated", at = @At("HEAD"), cancellable = true)
    private void blockSleepingInRealTime() {

    }
}
