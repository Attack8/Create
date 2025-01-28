package com.simibubi.create.content.trains.track;

import java.util.function.UnaryOperator;

import com.simibubi.create.compat.Mods;
import com.simibubi.create.content.contraptions.glue.SuperGlueEntity;
import com.simibubi.create.foundation.utility.AttachedRegistry;

import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.BlockFace;
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
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;

/**
 * Manages portal track integrations for various dimensions and mods within the Create mod.
 * <p>
 * Portals must be entered from the side and must lead to a different dimension than the one entered from.
 * This class handles the registration and functionality of portal tracks for standard and modded portals.
 * </p>
 */
public class AllPortalTracks {
	/**
	 * Functional interface representing a provider for portal track connections.
	 * It takes a pair of {@link ServerLevel} and {@link BlockFace} representing the inbound track
	 * and returns a similar pair for the outbound track.
	 */
	@FunctionalInterface
	public interface PortalTrackProvider extends UnaryOperator<Pair<ServerLevel, BlockFace>> {}

	/**
	 * Registry mapping portal blocks to their respective {@link PortalTrackProvider}s.
	 */
	private static final AttachedRegistry<Block, PortalTrackProvider> PORTAL_BEHAVIOURS =
			new AttachedRegistry<>(BuiltInRegistries.BLOCK);

	/**
	 * Registers a portal track integration for a given block identified by its {@link ResourceLocation}.
	 *
	 * @param block    The resource location of the portal block.
	 * @param provider The portal track provider for the block.
	 */
	public static void registerIntegration(ResourceLocation block, PortalTrackProvider provider) {
		PORTAL_BEHAVIOURS.register(block, provider);
	}

	/**
	 * Registers a portal track integration for a given {@link Block}.
	 *
	 * @param block    The portal block.
	 * @param provider The portal track provider for the block.
	 */
	public static void registerIntegration(Block block, PortalTrackProvider provider) {
		PORTAL_BEHAVIOURS.register(block, provider);
	}

	private static void registerSimpleInteraction(Mods mod, String dimensionId, String portalBlockId) {
		ResourceKey<Level> levelKey = ResourceKey.create(Registries.DIMENSION, mod.rl(dimensionId));
		registerSimpleInteraction(mod, levelKey, portalBlockId);
	}

	private static void registerSimpleInteraction(Mods mod, ResourceKey<Level> levelKey, String portalBlockId) {
		if (mod.isLoaded())
			registerSimpleInteraction(levelKey, asPortal(mod.rl(portalBlockId)));
	}

	private static void registerSimpleInteraction(ResourceKey<Level> levelKey, Portal portalBlock) {
		PortalTrackProvider p = i ->
			standardPortalProvider(i, Level.OVERWORLD, levelKey, portalBlock);
		PORTAL_BEHAVIOURS.register((Block) portalBlock, p);
	}

	/**
	 * Checks if a given {@link BlockState} represents a supported portal block.
	 *
	 * @param state The block state to check.
	 * @return {@code true} if the block state represents a supported portal; {@code false} otherwise.
	 */
	public static boolean isSupportedPortal(BlockState state) {
		return PORTAL_BEHAVIOURS.get(state.getBlock()) != null;
	}

	/**
	 * Grabs a block from the registry as a {@link Portal} block
	 */
	public static Portal asPortal(ResourceLocation block) {
		return (Portal) BuiltInRegistries.BLOCK.get(block);
	}

	/**
	 * Retrieves the corresponding outbound track on the other side of a portal.
	 *
	 * @param level        The current {@link ServerLevel}.
	 * @param inboundTrack The inbound track {@link BlockFace}.
	 * @return A pair containing the target {@link ServerLevel} and outbound {@link BlockFace},
	 * or {@code null} if no corresponding portal is found.
	 */
	public static Pair<ServerLevel, BlockFace> getOtherSide(ServerLevel level, BlockFace inboundTrack) {
		BlockPos portalPos = inboundTrack.getConnectedPos();
		BlockState portalState = level.getBlockState(portalPos);
		PortalTrackProvider provider = PORTAL_BEHAVIOURS.get(portalState.getBlock());
		return provider == null ? null : provider.apply(Pair.of(level, inboundTrack));
	}

	// Built-in handlers

	/**
	 * Registers default portal track integrations for built-in dimensions and mods.
	 * This includes the Nether, the Aether (if loaded) and the end (if betterend is loaded).
	 */
	public static void registerDefaults() {
		registerSimpleInteraction(Level.NETHER, (Portal) Blocks.NETHER_PORTAL);
		registerSimpleInteraction(Mods.AETHER, "the_aether", "aether_portal");
		registerSimpleInteraction(Mods.AETHER_II, "aether_highlands", "aether_portal");
		registerSimpleInteraction(Mods.BETTEREND, Level.END, "end_portal_block");
	}

	/**
	 * Generalized portal provider method that calculates the corresponding outbound track across a portal.
	 *
	 * @param inbound            A pair containing the current {@link ServerLevel} and inbound {@link BlockFace}.
	 * @param firstDimension     The first dimension.
	 * @param secondDimension    The second dimension.
	 * @param portal             The portal block
	 * @return A pair with the target {@link ServerLevel} and outbound {@link BlockFace}, or {@code null} if not applicable.
	 */
	public static Pair<ServerLevel, BlockFace> standardPortalProvider(
		Pair<ServerLevel, BlockFace> inbound,
		ResourceKey<Level> firstDimension,
		ResourceKey<Level> secondDimension,
		Portal portal
	) {
		ServerLevel level = inbound.getFirst();
		ResourceKey<Level> resourceKey = level.dimension() == secondDimension ? firstDimension : secondDimension;

		MinecraftServer minecraftServer = level.getServer();
		ServerLevel otherLevel = minecraftServer.getLevel(resourceKey);

		if (otherLevel == null)
			return null;

		BlockFace inboundTrack = inbound.getSecond();
		BlockPos portalPos = inboundTrack.getConnectedPos();
		BlockState portalState = level.getBlockState(portalPos);

		SuperGlueEntity probe = new SuperGlueEntity(level, new AABB(portalPos));
		probe.setYRot(inboundTrack.getFace().toYRot());

		DimensionTransition dimensiontransition = portal.getPortalDestination(level, probe, probe.blockPosition());
		if (dimensiontransition == null)
			return null;

		if (!minecraftServer.isLevelEnabled(dimensiontransition.newLevel()))
			return null;

		BlockPos otherPortalPos = BlockPos.containing(dimensiontransition.pos());
		BlockState otherPortalState = otherLevel.getBlockState(otherPortalPos);
		if (!otherPortalState.is(portalState.getBlock()))
			return null;

		Direction targetDirection = inboundTrack.getFace();
		if (targetDirection.getAxis() == otherPortalState.getValue(BlockStateProperties.HORIZONTAL_AXIS))
			targetDirection = targetDirection.getClockWise();
		BlockPos otherPos = otherPortalPos.relative(targetDirection);
		return Pair.of(otherLevel, new BlockFace(otherPos, targetDirection.getOpposite()));
	}
}
