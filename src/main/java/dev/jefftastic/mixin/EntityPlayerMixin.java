package dev.jefftastic.mixin;

import dev.jefftastic.JeffyTimeWeaverAddon;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin extends EntityLivingBase {
    @Shadow
    public abstract void addChatMessage(String par1Str);

    public EntityPlayerMixin(World par1World) {
        super(par1World);
    }

    @Inject(method = "sleepInBedAt", at = @At("HEAD"), cancellable = true)
    private void cancelBedSleepingIfRT(int x, int y, int z, CallbackInfoReturnable<EnumStatus> cir) {
        WorldInfo info = this.worldObj.getWorldInfo();
        boolean realTime = info.getData(JeffyTimeWeaverAddon.REAL_TIME);

        if (realTime) {
            cir.setReturnValue(EnumStatus.NOT_POSSIBLE_HERE);

            if (!this.worldObj.isRemote) {
                this.addChatMessage("tile.bed.sleepImpossible");
            }
        }
    }
}
