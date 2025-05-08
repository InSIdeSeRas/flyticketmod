
package net.insideseras.flyticketmod.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.datafixer.DataFixTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlyTicketSaveState extends PersistentState {

    private static final String NAME = "flyticket_data";

    private final Map<UUID, Long> flyTimers = new HashMap<>();

    public static final Type<FlyTicketSaveState> TYPE = new Type<>(
            FlyTicketSaveState::new,
            FlyTicketSaveState::readNbt,
            DataFixTypes.LEVEL
    );

    public FlyTicketSaveState() {}

    public Map<UUID, Long> getFlyTimers() {
        return flyTimers;
    }

    public void setTimer(UUID uuid, long endTick) {
        flyTimers.put(uuid, endTick);
        markDirty();
    }

    public void removeTimer(UUID uuid) {
        if (flyTimers.remove(uuid) != null) {
            markDirty();
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound allData = new NbtCompound();
        for (Map.Entry<UUID, Long> entry : flyTimers.entrySet()) {
            allData.putLong(entry.getKey().toString(), entry.getValue());
        }
        nbt.put("FlyTimers", allData);
        return nbt;
    }

    public static FlyTicketSaveState readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        FlyTicketSaveState state = new FlyTicketSaveState();
        NbtCompound allData = nbt.getCompound("FlyTimers");
        for (String key : allData.getKeys()) {
            try {
                UUID uuid = UUID.fromString(key);
                long end = allData.getLong(key);
                state.flyTimers.put(uuid, end);
            } catch (IllegalArgumentException ignored) {}
        }
        return state;
    }

    public static FlyTicketSaveState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE, NAME);
    }
}
