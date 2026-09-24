package dev.jefftastic.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.jefftastic.JeffyTimeWeaverAddon;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityChicken.class)
public abstract class EntityChickenMixin extends EntityAnimal {
    @Unique private final int TICKS_FOR_EGG = 20 * 60 * 20; // 20 * 60 * 21; // 18-20 mins on avg
    @Unique private int rtMakingEgg = 0;

    public EntityChickenMixin(World par1World) {
        super(par1World);
    }

    @Inject(method = "writeEntityToNBT", at = @At("TAIL"))
    private void appendRTData(NBTTagCompound tag, CallbackInfo ci) {
        tag.setShort("rtMakingEgg", (short) this.rtMakingEgg);
    }

    @Inject(method = "readEntityFromNBT", at = @At("TAIL"))
    private void readRTData(NBTTagCompound tag, CallbackInfo ci) {
        this.rtMakingEgg = tag.hasKey("rtMakingEgg") ? tag.getShort("rtMakingEgg") : 0;
    }

    @Inject(method = "onBecomeFamished", at = @At("TAIL"))
    private void onFamishedRT(CallbackInfo ci) {
        this.rtMakingEgg = 0;
    }

    @ModifyReturnValue(method = "isReadyToEatBreedingItem", at = @At("RETURN"))
    private boolean canEatRT(boolean original) {
        WorldInfo info = this.worldObj.getWorldInfo();
        boolean isRealTime = info.getData(JeffyTimeWeaverAddon.REAL_TIME);

        if (!isRealTime) {
            return original;
        }

        return original && this.rtMakingEgg == 0;
    }

    @Inject(method = "onEatBreedingItem", at = @At("HEAD"), cancellable = true)
    private void onEatRT(CallbackInfo ci) {
        WorldInfo info = this.worldObj.getWorldInfo();
        boolean isRealTime = info.getData(JeffyTimeWeaverAddon.REAL_TIME);

        if (!isRealTime) {
            return;
        }

        // Enable egg timer
        this.rtMakingEgg = 1 + rand.nextInt(1600);
        ci.cancel();
    }

    @Inject(method = "updateHungerState", at = @At("HEAD"), cancellable = true)
    private void onUpdateHungerRT(CallbackInfo ci) {
        WorldInfo info = this.worldObj.getWorldInfo();
        boolean isRealTime = info.getData(JeffyTimeWeaverAddon.REAL_TIME);

        if (!isRealTime) {
            return;
        }

        boolean isChild = this.isChild();
        boolean isFed = this.isFullyFed();
        boolean isReadyToLayEgg = this.rtMakingEgg >= this.TICKS_FOR_EGG;

        if (!isChild && isFed && isReadyToLayEgg) {
            this.playSound("mob.slime.attack", 1.0f, this.getSoundPitch());
            this.playSound(this.getDeathSound(), this.getSoundVolume(), (this.getSoundPitch() + 0.25f) * (this.getSoundPitch() + 0.25f));
            this.dropItem(Item.egg.itemID, 1);
            this.rtMakingEgg = 0;
        }

        if (this.rtMakingEgg > 0) {
            this.rtMakingEgg += 1;
        }

        ci.cancel();
    }
}
