package com.simibubi.create.content.trains.track;

import java.util.function.UnaryOperator;

import com.simibubi.create.compat.Mods;
import com.simibubi.create.content.contraptions.glue.SuperGlueEntity;
import com.simibubi.create.foundation.utility.AttachedRegistry;

import net.createmod.catnip.math.BlockFace;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;

public class AllPortalTracks {

	// Portals must be entered from the side and must lead to a different dimension
	// than the one entered from

	@FunctionalInterface public interface PortalTrackProvider extends UnaryOperator<Pair<ServerLevel, BlockFace>> {}

	private static final AttachedRegistry<Block, PortalTrackProvider> PORTAL_BEHAVIOURS =
		new AttachedRegistry<>(BuiltInRegistries.BLOCK);

	public static void registerIntegration(ResourceLocation block, PortalTrackProvider provider) {
		PORTAL_BEHAVIOURS.register(block, provider);
	}

	public static void registerIntegration(Block block, PortalTrackProvider provider) {
		PORTAL_BEHAVIOURS.register(block, provider);
	}

	private static void registerIntegration(Mods mod, String blockId, PortalTrackProvider provider) {
		if (mod.isLoaded())
			PORTAL_BEHAVIOURS.register(mod.rl(blockId), provider);
	}

	public static boolean isSupportedPortal(BlockState state) {
		return PORTAL_BEHAVIOURS.get(state.getBlock()) != null;
	}

	public static Portal asPortal(ResourceLocation block) {
		return (Portal) BuiltInRegistries.BLOCK.get(block);
	}

	public static Pair<ServerLevel, BlockFace> getOtherSide(ServerLevel level, BlockFace inboundTrack) {
		BlockPos portalPos = inboundTrack.getConnectedPos();
		BlockState portalState = level.getBlockState(portalPos);
		PortalTrackProvider provider = PORTAL_BEHAVIOURS.get(portalState.getBlock());
		return provider == null ? null : provider.apply(Pair.of(level, inboundTrack));
	}

	// Builtin handlers

	public static void registerDefaults() {
		registerIntegration(Blocks.NETHER_PORTAL, AllPortalTracks::nether);
		registerIntegration(Mods.AETHER, "aether_portal", AllPortalTracks::aether);
		registerIntegration(Mods.AETHER_II, "aether_portal", AllPortalTracks::aetherII);
	}

	private static Pair<ServerLevel, BlockFace> nether(Pair<ServerLevel, BlockFace> inbound) {
		return standardPortalProvider(inbound, Level.OVERWORLD, Level.NETHER, (NetherPortalBlock) Blocks.NETHER_PORTAL);
	}

	private static Pair<ServerLevel, BlockFace> aether(Pair<ServerLevel, BlockFace> inbound) {
		ResourceKey<Level> aetherLevelKey = ResourceKey.create(Registries.DIMENSION, Mods.AETHER.rl("the_aether"));
		return standardPortalProvider(inbound, Level.OVERWORLD, aetherLevelKey, asPortal(Mods.AETHER.rl("aether_portal")));
	}

	private static Pair<ServerLevel, BlockFace> aetherII(Pair<ServerLevel, BlockFace> inbound) {
		ResourceKey<Level> aetherLevelKey = ResourceKey.create(Registries.DIMENSION, Mods.AETHER_II.rl("aether_highlands"));
		return standardPortalProvider(inbound, Level.OVERWORLD, aetherLevelKey, asPortal(Mods.AETHER_II.rl("aether_portal")));
	}

	public static Pair<ServerLevel, BlockFace> standardPortalProvider(Pair<ServerLevel, BlockFace> inbound,
		ResourceKey<Level> firstDimension, ResourceKey<Level> secondDimension,
		Portal portal) {
		ServerLevel level = inbound.getFirst();
		ResourceKey<Level> resourcekey = level.dimension() == secondDimension ? firstDimension : secondDimension;
		MinecraftServer minecraftserver = level.getServer();
		ServerLevel otherLevel = minecraftserver.getLevel(resourcekey);

		if (otherLevel == null || !minecraftserver.isLevelEnabled(otherLevel))
			return null;

		BlockFace inboundTrack = inbound.getSecond();
		BlockPos portalPos = inboundTrack.getConnectedPos();
		BlockState portalState = level.getBlockState(portalPos);

		SuperGlueEntity probe = new SuperGlueEntity(level, new AABB(portalPos));
		probe.setYRot(inboundTrack.getFace()
			.toYRot());

		DimensionTransition dimensiontransition = portal.getPortalDestination(level, probe, probe.blockPosition());
		if (dimensiontransition == null)
			return null;

		BlockPos otherPortalPos = BlockPos.containing(dimensiontransition.pos());
		BlockState otherPortalState = otherLevel.getBlockState(otherPortalPos);
		if (otherPortalState.getBlock() != portalState.getBlock())
			return null;

		Direction targetDirection = inboundTrack.getFace();
		if (targetDirection.getAxis() == otherPortalState.getValue(BlockStateProperties.HORIZONTAL_AXIS))
			targetDirection = targetDirection.getClockWise();
		BlockPos otherPos = otherPortalPos.relative(targetDirection);
		return Pair.of(otherLevel, new BlockFace(otherPos, targetDirection.getOpposite()));
	}

}
