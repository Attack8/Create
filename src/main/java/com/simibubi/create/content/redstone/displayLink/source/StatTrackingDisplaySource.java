package com.simibubi.create.content.redstone.displayLink.source;

import java.util.stream.Stream;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;

import net.createmod.catnip.data.IntAttached;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public abstract class StatTrackingDisplaySource extends ScoreboardDisplaySource {

	@Override
	protected Stream<IntAttached<MutableComponent>> provideEntries(DisplayLinkContext context, int maxRows) {
		Level level = context.blockEntity()
			.getLevel();
		if (!(level instanceof ServerLevel sLevel))
			return Stream.empty();

		String name = "create_auto_" + getObjectiveName();
		Scoreboard scoreboard = level.getScoreboard();
		if (scoreboard.getObjective(name) == null)
			// FIXME 1.21: check over
			scoreboard.addObjective(name, ObjectiveCriteria.DUMMY, getObjectiveDisplayName(), RenderType.INTEGER, false, null);
		Objective objective = scoreboard.getObjective(name);

		sLevel.getServer().getPlayerList().getPlayers()
			.forEach(s -> scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly(s.getScoreboardName()), objective)
				.set(updatedScoreOf(s)));

		return showScoreboard(sLevel, name, maxRows);
	}

	protected abstract String getObjectiveName();

	protected abstract Component getObjectiveDisplayName();

	protected abstract int updatedScoreOf(ServerPlayer player);

	@Override
	protected boolean valueFirst() {
		return false;
	}

	@Override
	protected boolean shortenNumbers(DisplayLinkContext context) {
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {}

}
