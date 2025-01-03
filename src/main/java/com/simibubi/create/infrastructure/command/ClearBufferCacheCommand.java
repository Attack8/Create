package com.simibubi.create.infrastructure.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.simibubi.create.CreateClient;

import net.createmod.catnip.CatnipClient;
import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.utility.lang.Components;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ClearBufferCacheCommand {

	static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("clearRenderBuffers")
			.requires(cs -> cs.hasPermission(0))
			.executes(ctx -> {
				CatnipServices.PLATFORM.executeOnClientOnly(() -> ClearBufferCacheCommand::execute);
				ctx.getSource()
					.sendSuccess(() -> Components.literal("Cleared rendering buffers."), true);
				return 1;
			});
	}

	@OnlyIn(Dist.CLIENT)
	private static void execute() {
		CatnipClient.invalidateRenderers();
		CreateClient.invalidateRenderers();
	}
}
