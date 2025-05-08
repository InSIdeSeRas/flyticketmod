package net.insideseras.flyticketmod.item;

import net.insideseras.flyticketmod.data.FlyTicketSaveState;
import net.insideseras.flyticketmod.particle.ModParticles;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.UUID;

public class RainbowPaperItem extends Item {

    // Temporäre Laufzeittabelle (UUID → EndTick)
    public static final HashMap<UUID, Long> flyTimers = new HashMap<>();

    public RainbowPaperItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!world.isClient) {
            ServerWorld serverWorld = (ServerWorld) world;
            UUID uuid = player.getUuid();

            // Prüfen ob bereits ein aktives Ticket existiert
            if (flyTimers.containsKey(uuid)) {
                player.sendMessage(Text.literal("⚠ Du hast bereits ein aktives FlyTicket.")
                        .formatted(Formatting.YELLOW), false);
                return TypedActionResult.fail(stack);
            }

            // Partikel (immer sichtbar beim Server)
            serverWorld.spawnParticles(
                    ModParticles.FLY_PARTICLE,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    13, 0.9, 0.9, 0.9, 0.01
            );

            if (!player.isCreative()) {
                // 30 Minuten = 36000 Ticks
                long endTick = serverWorld.getServer().getOverworld().getTime() + 36000;

                flyTimers.put(uuid, endTick);
                FlyTicketSaveState.get(serverWorld).setTimer(uuid, endTick);

                // Flug aktivieren
                player.getAbilities().allowFlying = true;
                player.sendAbilitiesUpdate();

                // Item verbrauchen
                stack.decrement(1);
            }
        }

        return TypedActionResult.success(stack, world.isClient());
    }
}
