package com.simibubi.create.content.equipment.potatoCannon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.createmod.catnip.codecs.stream.CatnipLargerStreamCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class PotatoCannonProjectileType {
	public static final Codec<PotatoCannonProjectileType> CODEC = RecordCodecBuilder.create(i -> i.group(
			BuiltInRegistries.ITEM.byNameCodec().listOf().fieldOf("items").forGetter(p -> p.getItems()),
			Codec.INT.optionalFieldOf("reload_ticks").forGetter(p -> Optional.of(p.getReloadTicks())),
			Codec.INT.optionalFieldOf("damage").forGetter(p -> Optional.of(p.getDamage())),
			Codec.INT.optionalFieldOf("split").forGetter(p -> Optional.of(p.getSplit())),
			Codec.FLOAT.optionalFieldOf("knockback").forGetter(p -> Optional.of(p.getKnockback())),
			Codec.FLOAT.optionalFieldOf("drag").forGetter(p -> Optional.of(p.getDrag())),
			Codec.FLOAT.optionalFieldOf("velocity_multiplier").forGetter(p -> Optional.of(p.getVelocityMultiplier())),
			Codec.FLOAT.optionalFieldOf("gravity_multiplier").forGetter(p -> Optional.of(p.getGravityMultiplier())),
			Codec.FLOAT.optionalFieldOf("sound_pitch").forGetter(p -> Optional.of(p.getSoundPitch())),
			Codec.BOOL.optionalFieldOf("sticky").forGetter(p -> Optional.of(p.isSticky()))
	).apply(i, (items, reloadTicks, damage, split, knockback, drag, velocityMultiplier, gravityMultiplier, soundPitch, sticky) -> {
		PotatoCannonProjectileType type = new PotatoCannonProjectileType();
		type.items.addAll(items);
		reloadTicks.ifPresent(r -> type.reloadTicks = r);
		damage.ifPresent(r -> type.damage = r);
		split.ifPresent(r -> type.split = r);
		knockback.ifPresent(r -> type.knockback = r);
		drag.ifPresent(r -> type.drag = r);
		velocityMultiplier.ifPresent(r -> type.velocityMultiplier = r);
		gravityMultiplier.ifPresent(r -> type.gravityMultiplier = r);
		soundPitch.ifPresent(r -> type.soundPitch = r);
		sticky.ifPresent(r -> type.sticky = r);

		return type;
	}));

	public static final StreamCodec<RegistryFriendlyByteBuf, PotatoCannonProjectileType> STREAM_CODEC = CatnipLargerStreamCodecs.composite(
			ByteBufCodecs.registry(Registries.ITEM).apply(ByteBufCodecs.list()), t -> t.items,
			ByteBufCodecs.INT, PotatoCannonProjectileType::getReloadTicks,
			ByteBufCodecs.INT, PotatoCannonProjectileType::getDamage,
			ByteBufCodecs.INT, PotatoCannonProjectileType::getSplit,
			ByteBufCodecs.FLOAT, PotatoCannonProjectileType::getKnockback,
			ByteBufCodecs.FLOAT, PotatoCannonProjectileType::getDrag,
			ByteBufCodecs.FLOAT, PotatoCannonProjectileType::getVelocityMultiplier,
			ByteBufCodecs.FLOAT, PotatoCannonProjectileType::getGravityMultiplier,
			ByteBufCodecs.FLOAT, PotatoCannonProjectileType::getSoundPitch,
			ByteBufCodecs.BOOL, PotatoCannonProjectileType::isSticky,
			PotatoCannonProjectileType::new
	);

	private List<Item> items = new ArrayList<>();

	private int reloadTicks = 10;
	private int damage = 1;
	private int split = 1;
	private float knockback = 1;
	private float drag = 0.99f;
	private float velocityMultiplier = 1;
	private float gravityMultiplier = 1;
	private float soundPitch = 1;
	private boolean sticky = false;
	private PotatoProjectileRenderMode renderMode = PotatoProjectileRenderMode.Billboard.INSTANCE;

	private Predicate<EntityHitResult> preEntityHit = e -> false; // True if hit should be canceled
	private Predicate<EntityHitResult> onEntityHit = e -> false; // True if shouldn't recover projectile
	private BiPredicate<LevelAccessor, BlockHitResult> onBlockHit = (w, ray) -> false;

	protected PotatoCannonProjectileType() {}

	public PotatoCannonProjectileType(List<Item> items, int reloadTicks, int damage, int split, float knockback,
									  float drag, float velocityMultiplier, float gravityMultiplier, float soundPitch,
									  boolean sticky) {
		this.items = items;
		this.reloadTicks = reloadTicks;
		this.damage = damage;
		this.split = split;
		this.knockback = knockback;
		this.drag = drag;
		this.velocityMultiplier = velocityMultiplier;
		this.gravityMultiplier = gravityMultiplier;
		this.soundPitch = soundPitch;
		this.sticky = sticky;
	}

	public List<Item> getItems() {
		return items;
	}

	public int getReloadTicks() {
		return reloadTicks;
	}

	public int getDamage() {
		return damage;
	}

	public int getSplit() {
		return split;
	}

	public float getKnockback() {
		return knockback;
	}

	public float getDrag() {
		return drag;
	}

	public float getVelocityMultiplier() {
		return velocityMultiplier;
	}

	public float getGravityMultiplier() {
		return gravityMultiplier;
	}

	public float getSoundPitch() {
		return soundPitch;
	}

	public boolean isSticky() {
		return sticky;
	}

	public PotatoProjectileRenderMode getRenderMode() {
		return renderMode;
	}

	public boolean preEntityHit(EntityHitResult ray) {
		return preEntityHit.test(ray);
	}

	public boolean onEntityHit(EntityHitResult ray) {
		return onEntityHit.test(ray);
	}

	public boolean onBlockHit(LevelAccessor world, BlockHitResult ray) {
		return onBlockHit.test(world, ray);
	}

	public static class Builder {

		protected ResourceLocation id;
		protected PotatoCannonProjectileType result;

		public Builder(ResourceLocation id) {
			this.id = id;
			this.result = new PotatoCannonProjectileType();
		}

		public Builder reloadTicks(int reload) {
			result.reloadTicks = reload;
			return this;
		}

		public Builder damage(int damage) {
			result.damage = damage;
			return this;
		}

		public Builder splitInto(int split) {
			result.split = split;
			return this;
		}

		public Builder knockback(float knockback) {
			result.knockback = knockback;
			return this;
		}

		public Builder drag(float drag) {
			result.drag = drag;
			return this;
		}

		public Builder velocity(float velocity) {
			result.velocityMultiplier = velocity;
			return this;
		}

		public Builder gravity(float modifier) {
			result.gravityMultiplier = modifier;
			return this;
		}

		public Builder soundPitch(float pitch) {
			result.soundPitch = pitch;
			return this;
		}

		public Builder sticky() {
			result.sticky = true;
			return this;
		}

		public Builder renderMode(PotatoProjectileRenderMode renderMode) {
			result.renderMode = renderMode;
			return this;
		}

		public Builder renderBillboard() {
			renderMode(PotatoProjectileRenderMode.Billboard.INSTANCE);
			return this;
		}

		public Builder renderTumbling() {
			renderMode(PotatoProjectileRenderMode.Tumble.INSTANCE);
			return this;
		}

		public Builder renderTowardMotion(int spriteAngle, float spin) {
			renderMode(new PotatoProjectileRenderMode.TowardMotion(spriteAngle, spin));
			return this;
		}

		public Builder preEntityHit(Predicate<EntityHitResult> callback) {
			result.preEntityHit = callback;
			return this;
		}

		public Builder onEntityHit(Predicate<EntityHitResult> callback) {
			result.onEntityHit = callback;
			return this;
		}

		public Builder onBlockHit(BiPredicate<LevelAccessor, BlockHitResult> callback) {
			result.onBlockHit = callback;
			return this;
		}

		public Builder addItems(ItemLike... items) {
			for (ItemLike provider : items)
				result.items.add(provider.asItem());
			return this;
		}

		public PotatoCannonProjectileType register() {
			PotatoProjectileTypeManager.registerBuiltinType(id, result);
			return result;
		}

		public PotatoCannonProjectileType registerAndAssign(ItemLike... items) {
			addItems(items);
			register();
			return result;
		}

	}
}
