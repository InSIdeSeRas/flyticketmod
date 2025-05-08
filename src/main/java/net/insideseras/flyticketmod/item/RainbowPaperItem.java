package net.insideseras.flyticketmod.item;

import net.insideseras.flyticketmod.particle.ModParticles;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.UUID;

public class RainbowPaperItem extends Item {

    // Speichert das Endzeit-Tick für den Fly-Effekt pro Spieler
    public static final HashMap<UUID, Long> flyTimers = new HashMap<>();

    public RainbowPaperItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        // Nur auf Serverseite
        if (!world.isClient) {
            ServerWorld serverWorld = (ServerWorld) world;

            // ✨ Partikel für alle Spieler (auch Creative)
            serverWorld.spawnParticles(
                    ModParticles.FLY_PARTICLE,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    13, 0.9, 0.9, 0.9, 0.01
            );

            // ✅ Nur im Survival-Modus:
            if (!player.isCreative()) {
                // Flugfähigkeit aktivieren
                player.getAbilities().allowFlying = true;
                player.sendAbilitiesUpdate();

                // Timer setzen (30 Minuten = 36000 Ticks)
                long endTick = serverWorld.getServer().getOverworld().getTime() + 36000;
                flyTimers.put(player.getUuid(), endTick);

                // Item verbrauchen
                stack.decrement(1);
            }
        }

        return TypedActionResult.success(stack, world.isClient());
    }
}
