package com.simibubi.create;

import java.util.List;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides.ClipboardType;
import com.simibubi.create.content.equipment.symmetryWand.mirror.SymmetryMirror;
import com.simibubi.create.content.equipment.zapper.PlacementPatterns;
import com.simibubi.create.content.equipment.zapper.terrainzapper.PlacementOptions;
import com.simibubi.create.content.equipment.zapper.terrainzapper.TerrainBrushes;
import com.simibubi.create.content.equipment.zapper.terrainzapper.TerrainTools;
import com.simibubi.create.content.fluids.potion.PotionFluid.BottleType;
import com.simibubi.create.content.logistics.box.PackageItem.PackageOrderData;
import com.simibubi.create.content.logistics.filter.AttributeFilterWhitelistMode;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute.ItemAttributeEntry;
import com.simibubi.create.content.logistics.redstoneRequester.AutoRequestData;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.logistics.tableCloth.ShoppingListItem.ShoppingList;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe.SequencedAssembly;
import com.simibubi.create.content.redstone.displayLink.ClickToLinkBlockItem.ClickToLinkData;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity.SchematicannonOptions;
import com.simibubi.create.content.trains.track.BezierTrackPointLocation;
import com.simibubi.create.content.trains.track.TrackPlacement.ConnectingFrom;

import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class AllDataComponents {
	private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Create.ID);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> BACKTANK_AIR = DATA_COMPONENTS.registerComponentType(
			"banktank_air",
			builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> BELT_FIRST_SHAFT = DATA_COMPONENTS.registerComponentType(
			"belt_first_shaft",
			builder -> builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> INFERRED_FROM_RECIPE = DATA_COMPONENTS.registerComponentType(
			"inferred_from_recipe",
			builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<PlacementPatterns>> PLACEMENT_PATTERN = DATA_COMPONENTS.registerComponentType(
			"placement_pattern",
			builder -> builder.persistent(PlacementPatterns.CODEC).networkSynchronized(PlacementPatterns.STREAM_CODEC)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<TerrainBrushes>> SHAPER_BRUSH = DATA_COMPONENTS.registerComponentType(
			"shaper_brush",
			builder -> builder.persistent(TerrainBrushes.CODEC).networkSynchronized(TerrainBrushes.STREAM_CODEC)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> SHAPER_BRUSH_PARAMS = DATA_COMPONENTS.registerComponentType(
			"shaper_brush_params",
			builder -> builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<PlacementOptions>> SHAPER_PLACEMENT_OPTIONS = DATA_COMPONENTS.registerComponentType(
			"shaper_placement_options",
			builder -> builder.persistent(PlacementOptions.CODEC).networkSynchronized(PlacementOptions.STREAM_CODEC)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<TerrainTools>> SHAPER_TOOL = DATA_COMPONENTS.registerComponentType(
			"shaper_tool",
			builder -> builder.persistent(TerrainTools.CODEC).networkSynchronized(TerrainTools.STREAM_CODEC)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockState>> SHAPER_BLOCK_USED = DATA_COMPONENTS.registerComponentType(
			"shaper_block_used",
			builder -> builder.persistent(BlockState.CODEC).networkSynchronized(ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY))
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SHAPER_SWAP = DATA_COMPONENTS.registerComponentType(
			"shaper_swap",
			builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> SHAPER_BLOCK_DATA = DATA_COMPONENTS.registerComponentType(
			"shaper_block_data",
			builder -> builder.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> FILTER_ITEMS = DATA_COMPONENTS.registerComponentType(
			"filter_items",
			builder -> builder.persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC)
	);

	// These 2 are placed on items inside filters and not the filter itself
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> FILTER_ITEMS_RESPECT_NBT = DATA_COMPONENTS.registerComponentType(
			"filter_items_respect_nbt",
			builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> FILTER_ITEMS_BLACKLIST = DATA_COMPONENTS.registerComponentType(
			"filter_items_blacklist",
			builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<AttributeFilterWhitelistMode>> ATTRIBUTE_FILTER_WHITELIST_MODE = DATA_COMPONENTS.registerComponentType(
			"attribute_filter_whitelist_mode",
			builder -> builder.persistent(AttributeFilterWhitelistMode.CODEC).networkSynchronized(AttributeFilterWhitelistMode.STREAM_CODEC)
	);

	// TODO - Make a stream codec for this
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ItemAttributeEntry>>> ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES = DATA_COMPONENTS.registerComponentType(
			"attribute_filter_matched_attributes",
			builder -> builder.persistent(ItemAttributeEntry.CODEC.listOf())
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<ClipboardType>> CLIPBOARD_TYPE = DATA_COMPONENTS.registerComponentType(
			"clipboard_type",
			builder -> builder.persistent(ClipboardType.CODEC).networkSynchronized(ClipboardType.STREAM_CODEC)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<List<ClipboardEntry>>>> CLIPBOARD_PAGES = DATA_COMPONENTS.registerComponentType(
			"clipboard_pages",
			builder -> builder.persistent(ClipboardEntry.CODEC.listOf().listOf()).networkSynchronized(ClipboardEntry.STREAM_CODEC.apply(ByteBufCodecs.list()).apply(ByteBufCodecs.list()))
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> CLIPBOARD_READ_ONLY = DATA_COMPONENTS.registerComponentType(
			"clipboard_read_only",
			builder -> builder.persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> CLIPBOARD_COPIED_VALUES = DATA_COMPONENTS.registerComponentType(
			"clipboard_copied_values",
			builder -> builder.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CLIPBOARD_PREVIOUSLY_OPENED_PAGE = DATA_COMPONENTS.registerComponentType(
			"clipboard_previously_opened_page",
			builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<ConnectingFrom>> TRACK_CONNECTING_FROM = DATA_COMPONENTS.registerComponentType(
			"track_connecting_from",
			builder -> builder.persistent(ConnectingFrom.CODEC).networkSynchronized(ConnectingFrom.STREAM_CODEC)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> TRACK_EXTENDED_CURVE = DATA_COMPONENTS.registerComponentType(
			"track_extend_curve",
			builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> TRACK_TARGETING_ITEM_SELECTED_POS = DATA_COMPONENTS.registerComponentType(
			"track_targeting_item_selected_pos",
			builder -> builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> TRACK_TARGETING_ITEM_SELECTED_DIRECTION = DATA_COMPONENTS.registerComponentType(
			"track_targeting_item_selected_direction",
			builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<BezierTrackPointLocation>> TRACK_TARGETING_ITEM_BEZIER = DATA_COMPONENTS.registerComponentType(
			"track_targeting_item_bezier",
			builder -> builder.persistent(BezierTrackPointLocation.CODEC).networkSynchronized(BezierTrackPointLocation.STREAM_CODEC)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SCHEMATIC_DEPLOYED = DATA_COMPONENTS.registerComponentType(
			"schematic_deployed",
			builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> SCHEMATIC_OWNER = DATA_COMPONENTS.registerComponentType(
			"schematic_owner",
			builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> SCHEMATIC_FILE = DATA_COMPONENTS.registerComponentType(
			"schematic_file",
			builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> SCHEMATIC_ANCHOR = DATA_COMPONENTS.registerComponentType(
			"schematic_anchor",
			builder -> builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Rotation>> SCHEMATIC_ROTATION = DATA_COMPONENTS.registerComponentType(
			"schematic_rotation",
			builder -> builder.persistent(Rotation.CODEC).networkSynchronized(CatnipStreamCodecs.ROTATION)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Mirror>> SCHEMATIC_MIRROR = DATA_COMPONENTS.registerComponentType(
			"schematic_mirror",
			builder -> builder.persistent(Mirror.CODEC).networkSynchronized(CatnipStreamCodecs.MIRROR)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Vec3i>> SCHEMATIC_BOUNDS = DATA_COMPONENTS.registerComponentType(
			"schematic_bounds",
			builder -> builder.persistent(Vec3i.CODEC).networkSynchronized(CatnipStreamCodecs.VEC3I)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SCHEMATIC_HASH = DATA_COMPONENTS.registerComponentType(
			"schematic_hash",
			builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CHROMATIC_COMPOUND_COLLECTING_LIGHT = DATA_COMPONENTS.registerComponentType(
			"chromatic_compound_collecting_light",
			builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemStack>> SAND_PAPER_POLISHING = DATA_COMPONENTS.registerComponentType(
			"sand_paper_polishing",
			builder -> builder.persistent(ItemStack.CODEC).networkSynchronized(ItemStack.STREAM_CODEC)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> SAND_PAPER_JEI = DATA_COMPONENTS.registerComponentType(
			"sand_paper_jei",
			builder -> builder.persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
	);

	// Holds contraption data when a minecraft contraption is picked up
	public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> MINECRAFT_CONTRAPTION_DATA = DATA_COMPONENTS.registerComponentType(
			"minecart_contraption_data",
			builder -> builder.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> LINKED_CONTROLLER_ITEMS = DATA_COMPONENTS.registerComponentType(
			"linked_controller_items",
			builder -> builder.persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> TOOLBOX_INVENTORY = DATA_COMPONENTS.registerComponentType(
			"toolbox_inventory",
			builder -> builder.persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> TOOLBOX_UUID = DATA_COMPONENTS.registerComponentType(
			"toolbox_uuid",
			builder -> builder.persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<SequencedAssembly>> SEQUENCED_ASSEMBLY = DATA_COMPONENTS.registerComponentType(
			"sequenced_assembly",
			builder -> builder.persistent(SequencedAssembly.CODEC).networkSynchronized(SequencedAssembly.STREAM_CODEC)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<CompoundTag>> TRAIN_SCHEDULE = DATA_COMPONENTS.registerComponentType(
			"train_schedule",
			builder -> builder.persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<SymmetryMirror>> SYMMETRY_WAND = DATA_COMPONENTS.registerComponentType(
			"symmetry_wand",
			builder -> builder.persistent(SymmetryMirror.CODEC).networkSynchronized(SymmetryMirror.STREAM_CODEC)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SYMMETRY_WAND_ENABLE = DATA_COMPONENTS.registerComponentType(
			"symmetry_wand_enable",
			builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> SYMMETRY_WAND_SIMULATE = DATA_COMPONENTS.registerComponentType(
			"symmetry_wand_simulate",
			builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> DISPLAY_LINK_SELECTED_POS = DATA_COMPONENTS.registerComponentType(
			"display_link_selected_pos",
			builder -> builder.persistent(BlockPos.CODEC).networkSynchronized(BlockPos.STREAM_CODEC)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<BottleType>> POTION_FLUID_BOTTLE_TYPE = DATA_COMPONENTS.registerComponentType(
			"potion_fluid_bottle_type",
			builder -> builder.persistent(BottleType.CODEC).networkSynchronized(BottleType.STREAM_CODEC)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<SchematicannonOptions>> SCHEMATICANNON_OPTIONS = DATA_COMPONENTS.registerComponentType(
			"schematicannon_options",
			builder -> builder.persistent(SchematicannonOptions.CODEC).networkSynchronized(SchematicannonOptions.STREAM_CODEC)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<AutoRequestData>> AUTO_REQUEST_DATA = DATA_COMPONENTS.registerComponentType(
		"auto_request_data",
		builder -> builder.persistent(AutoRequestData.CODEC).networkSynchronized(AutoRequestData.STREAM_CODEC)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<ShoppingList>> SHOPPING_LIST = DATA_COMPONENTS.registerComponentType(
		"shopping_list",
		builder -> builder.persistent(ShoppingList.CODEC).networkSynchronized(ShoppingList.STREAM_CODEC)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> SHOPPING_LIST_ADDRESS = DATA_COMPONENTS.registerComponentType(
		"shopping_list_address",
		builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> PACKAGE_ADDRESS = DATA_COMPONENTS.registerComponentType(
		"package_address",
		builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> PACKAGE_CONTENTS = DATA_COMPONENTS.registerComponentType(
		"package_contents",
		builder -> builder.persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<PackageOrderData>> PACKAGE_ORDER_DATA = DATA_COMPONENTS.registerComponentType(
		"package_order_data",
		builder -> builder.persistent(PackageOrderData.CODEC).networkSynchronized(PackageOrderData.STREAM_CODEC)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<PackageOrder>> PACKAGE_ORDER_CONTEXT = DATA_COMPONENTS.registerComponentType(
		"package_order_context",
		builder -> builder.persistent(PackageOrder.CODEC).networkSynchronized(PackageOrder.STREAM_CODEC)
	);

	public static final DeferredHolder<DataComponentType<?>, DataComponentType<ClickToLinkData>> CLICK_TO_LINK_DATA = DATA_COMPONENTS.registerComponentType(
		"click_to_link_data",
		builder -> builder.persistent(ClickToLinkData.CODEC).networkSynchronized(ClickToLinkData.STREAM_CODEC)
	);

	public static void register(IEventBus modEventBus) {
		DATA_COMPONENTS.register(modEventBus);
	}
}
