package dev.jefftastic;

import api.BTWAddon;
import api.world.data.DataEntry;
import api.world.data.DataProvider;
import net.minecraft.src.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class JeffyTimeWeaverAddon extends BTWAddon {
    private static JeffyTimeWeaverAddon INSTANCE;
    private static Logger LOGGER;

    private static final String DAY_DILATION_NAME = "DayDilation";
    private static final String NIGHT_DILATION_NAME = "NightDilation";
    private static final String REAL_TIME_NAME = "IsRealTime";
    private static final String EPOCH_OFFSET_NAME = "RealTimeEpochOffset";
    /**
     * Scalar for how long daylight lasts (default: 1)
     */
    public static final DataEntry.WorldDataEntry<Double> DAY_DILATION;
    /**
     * Scalar for how long night lasts (default: 1)
     */
    public static final DataEntry.WorldDataEntry<Double> NIGHT_DILATION;
    /**
     * Whether or not "real time" mode is enabled, syncing world time to local OS time (default: false)
     */
    public static final DataEntry.WorldDataEntry<Boolean> REAL_TIME;
    /**
     * Offset between the calendar day and the in-game day (default: Long.MIN_VALUE as an uninitialized flag)
     */
    public static final DataEntry.WorldDataEntry<Long> EPOCH_OFFSET;

    public JeffyTimeWeaverAddon() {
        // Well, there's an addon here.
        super();
    }

    @Override
    public void initialize() {
        LOGGER = LogManager.getLogger(this.addonName);
        LOGGER.info("Initializing {} addon ver {}", this.addonName, this.versionString);

        // Register world data
        LOGGER.info("Registering world data");
        DAY_DILATION.register();
        NIGHT_DILATION.register();
        REAL_TIME.register();

        // Register commands
        LOGGER.info("Registering world commands");
        registerAddonCommand(new CommandDilate());
        registerAddonCommand(new CommandRealTime());
    }

    /**
     * Handles time updates on client and server
     *
     * @param info World info
     */
    public static double updateTime(WorldInfo info, long time, double buffer) {
        // Get values from world
        boolean isRealTime = info.getData(JeffyTimeWeaverAddon.REAL_TIME);
        double dayFactor = info.getData(JeffyTimeWeaverAddon.DAY_DILATION);
        double nightFactor = info.getData(JeffyTimeWeaverAddon.NIGHT_DILATION);

        // If real time, run that instead
        if (isRealTime) {
            return updateRealTime(info);
        }

        // Dilate based on time of day
        long currentTime = time - 1;
        boolean isDay = info.getWorldTime() % 24000 < 12000;
        return dilateTime(info, currentTime, isDay ? dayFactor : nightFactor, buffer);
    }

    private static double dilateTime(WorldInfo info, long time, double factor, double buffer) {
        if (factor < 1F) {
            // Get the "raw" and rounded values
            double rawIncrement = (1D / factor) + buffer;
            int increment = (int) Math.floor(rawIncrement);

            // Set value and set remainder
            info.setWorldTime(time + increment);
            buffer = rawIncrement - increment;
        } else {
            // Add to buffer
            buffer += 1D;

            // If it exceeds the factor, subtract from buffer and increment time
            if (buffer >= factor) {
                buffer = Math.max(0, buffer - factor);
                info.setWorldTime(time + 1L);
            }
        }
        return buffer;
    }

    /**
     * Handles real time dilation
     * @param world World instance
     */
    public static double updateRealTime(WorldInfo world) {
        // Convert OS time to a fractional hour
        LocalDateTime now = LocalDateTime.now();
        double realHours = now.getHour() + (now.getMinute() / 60.0) +
                (now.getSecond() / 3600.0) + (now.getNano() / 3.6e12);

        // Convert to time
        double targetTime = (realHours * 1000.0) - 6000.0;
        if (targetTime < 0) {
            targetTime += 24000.0;
        }

        // Get calendar date compared to one in world file
        LocalDate mcDate = now.minusHours(6).toLocalDate();
        long currentEpochDay = mcDate.toEpochDay();
        long offset = world.getData(JeffyTimeWeaverAddon.EPOCH_OFFSET);

        // If first time running realtime, set epoch
        if (offset == Long.MIN_VALUE) {
            long currentMcDay = world.getWorldTime() / 24000L;
            offset = currentMcDay - currentEpochDay;
            world.setData(JeffyTimeWeaverAddon.EPOCH_OFFSET, offset);
        }

        // Total time
        long targetMcDay = Math.max(0, currentEpochDay + offset);
        long targetTotalTime = (targetMcDay * 24000L) + (long) targetTime;
        world.setWorldTime(targetTotalTime);
        return 0D;
    }

    static {
        DAY_DILATION = DataProvider.getBuilder(Double.class)
                .name(DAY_DILATION_NAME)
                .defaultSupplier(() -> 1D)
                .readNBT(NBTTagCompound::getDouble)
                .writeNBT(NBTTagCompound::setDouble)
                .global()
                .sync()
                .build();
        NIGHT_DILATION = DataProvider.getBuilder(Double.class)
                .name(NIGHT_DILATION_NAME)
                .defaultSupplier(() -> 1D)
                .readNBT(NBTTagCompound::getDouble)
                .writeNBT(NBTTagCompound::setDouble)
                .global()
                .sync()
                .build();
        REAL_TIME = DataProvider.getBuilder(Boolean.class)
                .name(REAL_TIME_NAME)
                .defaultSupplier(() -> false)
                .readNBT(NBTTagCompound::getBoolean)
                .writeNBT(NBTTagCompound::setBoolean)
                .global()
                .sync()
                .build();
        EPOCH_OFFSET = DataProvider.getBuilder(Long.class)
                .name(EPOCH_OFFSET_NAME)
                .defaultSupplier(() -> Long.MIN_VALUE)
                .readNBT(NBTTagCompound::getLong)
                .writeNBT(NBTTagCompound::setLong)
                .global()
                .sync()
                .build();
    }
}