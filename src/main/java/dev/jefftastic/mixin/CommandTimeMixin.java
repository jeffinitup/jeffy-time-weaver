package dev.jefftastic.mixin;

import dev.jefftastic.JeffyTimeWeaverAddon;
import net.minecraft.src.CommandTime;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.WorldInfo;
import net.minecraft.src.WrongUsageException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandTime.class)
public class CommandTimeMixin {
    @Inject(method = "processCommand", at = @At("HEAD"), cancellable = true)
    private void blockCommandIfRealTime(ICommandSender par1ICommandSender,
                                        String[] par2ArrayOfStr,
                                        CallbackInfo ci) {
        WorldInfo info = par1ICommandSender.getEntityWorld().getWorldInfo();
        boolean isRealTime = info.getData(JeffyTimeWeaverAddon.REAL_TIME);

        // If running in real time, don't run command
        if (isRealTime) {
            ci.cancel();
            throw new WrongUsageException("commands.time.disabled");
        }
    }
}
