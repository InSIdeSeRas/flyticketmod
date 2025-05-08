package net.insideseras.flyticketmod.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.insideseras.flyticketmod.particle.ModParticles; // dein Partikel-Import

public class RainbowPaperItem extends Item {

    public RainbowPaperItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        if (!world.isClient) {
            ServerWorld serverWorld = (ServerWorld) world;

            serverWorld.spawnParticles(
                    ModParticles.FLY_PARTICLE,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    13,
                    0.9, 0.9, 0.9,
                    0.01
            );
        }

        return TypedActionResult.success(player.getStackInHand(hand), world.isClient());
    }
}
