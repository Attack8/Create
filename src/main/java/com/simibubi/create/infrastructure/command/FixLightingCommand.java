package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.builder.ArgumentBuilder;

import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.lang.Components;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class FixLightingCommand {

	static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("fixLighting")
			.requires(cs -> cs.hasPermission(0))
			.executes(ctx -> {
				CatnipServices.NETWORK.simpleActionToClient(
						(ServerPlayer) ctx.getSource().getEntity(),
						"experimentalLighting",
						String.valueOf(true)
				);

				ctx.getSource()
					.sendSuccess(() ->
						Components.literal("NeoForge's experimental block rendering pipeline is now enabled."), true);

				return 1;
			});
	}
}
