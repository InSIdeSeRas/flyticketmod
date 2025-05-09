
package net.insideseras.flyticketmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.insideseras.flyticketmod.data.FlyTicketSaveState;
import net.insideseras.flyticketmod.item.RainbowPaperItem;
import net.insideseras.flyticketmod.item.modItems;
import net.insideseras.flyticketmod.particle.ModParticles;
import net.minecraft.command.argument.EntityArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import static net.minecraft.server.command.CommandManager.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import me.lucko.fabric.api.permissions.v0.Permissions;

public class FlyTicketMod implements ModInitializer {
	public static final String MOD_ID = "flyticket-mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		modItems.registerModItems();
		ModParticles.registerParticles();

		ServerTickEvents.START_SERVER_TICK.register(server -> {
			long currentTick = server.getOverworld().getTime();
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				Long endTick = RainbowPaperItem.flyTimers.get(player.getUuid());
				if (endTick != null) {
					if (player.isCreative()) {
						RainbowPaperItem.flyTimers.remove(player.getUuid());
						FlyTicketSaveState.get(player.getServerWorld()).removeTimer(player.getUuid());
						continue;
					}
					long remainingTicks = endTick - currentTick;
					if (remainingTicks <= 0) {
						player.getAbilities().allowFlying = false;
						player.getAbilities().flying = false;
						player.sendAbilitiesUpdate();
						player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 250, 0));
						RainbowPaperItem.flyTimers.remove(player.getUuid());
						FlyTicketSaveState.get(player.getServerWorld()).removeTimer(player.getUuid());
						player.sendMessage(Text.literal("❌ Deine Flugzeit ist abgelaufen.").formatted(Formatting.RED), false);
					} else if (currentTick % 20 == 0) {
						int totalSeconds = MathHelper.floor(remainingTicks / 20.0);
						int minutes = totalSeconds / 60;
						int seconds = totalSeconds % 60;
						if (totalSeconds <= 10) {
							player.getServerWorld().playSoundFromEntity(null, player, SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 0.7f, 1.2f);
						}
						String timeStr = String.format("%02d:%02d", minutes, seconds);
						player.sendMessage(Text.literal("⌛ Flugzeit: ").append(Text.literal(timeStr).formatted(Formatting.AQUA)), true);
					}
				}
			}
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.player;
			ServerWorld world = player.getServerWorld();
			Long endTick = FlyTicketSaveState.get(world).getFlyTimers().get(player.getUuid());
			if (endTick != null && endTick > world.getTime()) {
				RainbowPaperItem.flyTimers.put(player.getUuid(), endTick);
				player.getAbilities().allowFlying = true;
				player.sendAbilitiesUpdate();
			}
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(literal("flyticket")
					.requires(source -> Permissions.check(source, "flyticket.use") || source.hasPermissionLevel(2))
					.executes(context -> {
						ServerPlayerEntity player = context.getSource().getPlayer();
						ItemStack ticket = new ItemStack(modItems.RAINBOW_PAPER);
						player.getInventory().insertStack(ticket);
						player.sendMessage(Text.literal("🎟 Du hast ein FlyTicket erhalten."), false);
						return 1;
					})
					.then(literal("give")
							.requires(source -> Permissions.check(source, "flyticket.give") || source.hasPermissionLevel(2))
							.then(argument("target", EntityArgumentType.player())
									.executes(ctx -> {
										ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
										target.getInventory().insertStack(new ItemStack(modItems.RAINBOW_PAPER));
										ctx.getSource().sendFeedback(() -> Text.literal("🎟 FlyTicket an " + target.getName().getString() + " gegeben."), false);
										return 1;
									})
							)
					)
					.then(literal("cancel")
							.requires(source -> Permissions.check(source, "flyticket.cancel") || source.hasPermissionLevel(2))
							.executes(ctx -> {
								ServerPlayerEntity player = ctx.getSource().getPlayer();
								if (RainbowPaperItem.flyTimers.containsKey(player.getUuid())) {
									RainbowPaperItem.flyTimers.remove(player.getUuid());
									FlyTicketSaveState.get(player.getServerWorld()).removeTimer(player.getUuid());
									player.getAbilities().allowFlying = false;
									player.getAbilities().flying = false;
									player.sendAbilitiesUpdate();
									player.sendMessage(Text.literal("❌ Dein aktives FlyTicket wurde abgebrochen.").formatted(Formatting.RED), false);
									return 1;
								} else {
									player.sendMessage(Text.literal("❌ Du hast kein aktives FlyTicket."), false);
									return 0;
								}
							})
					)
					.then(literal("addtime")
							.requires(source -> Permissions.check(source, "flyticket.time.add") || source.hasPermissionLevel(2))
							.then(argument("target", EntityArgumentType.player())
									.then(argument("seconds", IntegerArgumentType.integer(1))
											.executes(ctx -> {
												ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
												ServerPlayerEntity source = ctx.getSource().getPlayer();
												int sec = IntegerArgumentType.getInteger(ctx, "seconds");
												if (!RainbowPaperItem.flyTimers.containsKey(target.getUuid())) {
													source.sendMessage(Text.literal("❌ " + target.getName().getString() + " hat kein aktives FlyTicket.").formatted(Formatting.RED), false);
													return 0;
												}
												return modifyTime(target, sec, source);
											})
									)
							)
					)
					.then(literal("removetime")
							.requires(source -> Permissions.check(source, "flyticket.time.remove") || source.hasPermissionLevel(2))
							.then(argument("target", EntityArgumentType.player())
									.then(argument("seconds", IntegerArgumentType.integer(1))
											.executes(ctx -> {
												ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
												ServerPlayerEntity source = ctx.getSource().getPlayer();
												int sec = IntegerArgumentType.getInteger(ctx, "seconds");
												if (!RainbowPaperItem.flyTimers.containsKey(target.getUuid())) {
													source.sendMessage(Text.literal("❌ " + target.getName().getString() + " hat kein aktives FlyTicket.").formatted(Formatting.RED), false);
													return 0;
												}
												return modifyTime(target, -sec, source);
											})
									)
							)
					)
					.then(literal("settime")
							.requires(source -> Permissions.check(source, "flyticket.time.set") || source.hasPermissionLevel(2))
							.then(argument("target", EntityArgumentType.player())
									.then(argument("seconds", IntegerArgumentType.integer(1))
											.executes(ctx -> {
												ServerPlayerEntity target = EntityArgumentType.getPlayer(ctx, "target");
												ServerPlayerEntity source = ctx.getSource().getPlayer();
												int sec = IntegerArgumentType.getInteger(ctx, "seconds");
												if (!RainbowPaperItem.flyTimers.containsKey(target.getUuid())) {
													source.sendMessage(Text.literal("❌ " + target.getName().getString() + " hat kein aktives FlyTicket.").formatted(Formatting.RED), false);
													return 0;
												}
												long newEnd = target.getServerWorld().getTime() + sec * 20L;
												RainbowPaperItem.flyTimers.put(target.getUuid(), newEnd);
												FlyTicketSaveState.get(target.getServerWorld()).setTimer(target.getUuid(), newEnd);
												target.getAbilities().allowFlying = true;
												target.sendAbilitiesUpdate();
												target.sendMessage(Text.literal("⌛ Deine Flugzeit wurde gesetzt: " + sec + " Sekunden.").formatted(Formatting.AQUA), false);
												if (!target.equals(source)) {
													source.sendMessage(Text.literal("✔ Flugzeit für " + target.getName().getString() + " auf " + sec + " Sekunden gesetzt.").formatted(Formatting.GREEN), false);
												}
												return 1;
											})
									)
							)
					)
			);
		});

		LOGGER.info("FlyTicketMod wurde geladen.");
	}

	private int modifyTime(ServerPlayerEntity player, int seconds, ServerPlayerEntity source) {
		long now = player.getServerWorld().getTime();
		Long oldEnd = RainbowPaperItem.flyTimers.get(player.getUuid());
		if (oldEnd != null) {
			long newEnd = Math.max(now, oldEnd + seconds * 20L);
			RainbowPaperItem.flyTimers.put(player.getUuid(), newEnd);
			FlyTicketSaveState.get(player.getServerWorld()).setTimer(player.getUuid(), newEnd);
			String message = (seconds > 0 ? "⏫ verlängert" : "⏬ verkürzt") + " um " + Math.abs(seconds) + " Sekunden.";
			Formatting color = seconds > 0 ? Formatting.GREEN : Formatting.RED;
			if (!player.equals(source)) {
				player.sendMessage(Text.literal("⌛ Deine Flugzeit wurde von " + source.getName().getString() + " " + message).formatted(color), false);
				source.sendMessage(Text.literal("✔ Flugzeit von " + player.getName().getString() + " " + message).formatted(color), false);
			} else {
				player.sendMessage(Text.literal("✔ Flugzeit " + message).formatted(color), false);
			}
			return 1;
		} else {
			source.sendMessage(Text.literal("❌ Kein aktives FlyTicket bei " + player.getName().getString()).formatted(Formatting.YELLOW), false);
			return 0;
		}
	}
}
