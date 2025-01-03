package com.simibubi.create.infrastructure.codec;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

public class CombiningCodec<A, B, AB> extends MapCodec<AB> {
	protected final MapCodec<A> first;
	protected final MapCodec<B> second;
	protected final BiFunction<A, B, AB> combiner;
	protected final Function<AB, Pair<A, B>> extractor;

	protected CombiningCodec(MapCodec<A> first, MapCodec<B> second, BiFunction<A, B, AB> combiner, Function<AB, Pair<A, B>> extractor) {
		this.first = first;
		this.second = second;
		this.combiner = combiner;
		this.extractor = extractor;
	}

	public static <A, B, AB> Codec<AB> of(Codec<A> first, Codec<B> second, BiFunction<A, B, AB> combiner, Function<AB, Pair<A, B>> extractor) {
		if (first instanceof MapCodec.MapCodecCodec<A> firstMap && second instanceof MapCodec.MapCodecCodec<B> secondMap) {
			return new CombiningCodec<>(firstMap.codec(), secondMap.codec(), combiner, extractor).codec();
		} else {
			return Codec.pair(
					first,
					second
			).xmap(
					fsPair -> combiner.apply(fsPair.getFirst(), fsPair.getSecond()),
					extractor
			);
		}
	}

	public static <A, B, AB> MapCodec<AB> of(MapCodec<A> first, MapCodec<B> second, BiFunction<A, B, AB> combiner, Function<AB, Pair<A, B>> extractor) {
		return new CombiningCodec<>(first, second, combiner, extractor);
	}

	public static <A, B> MapCodec<Pair<A, B>> of (MapCodec<A> first, MapCodec<B> second) {
		return new CombiningCodec<>(first, second, Pair::of, pair -> pair);
	}

	public static <A, B> Codec<Pair<A, B>> of(Codec<A> first, Codec<B> second) {
		return of(first, second, Pair::of, pair -> pair);
	}

	public static <P, C extends P, E> MapCodec<C> ofExtending(MapCodec<P> parent, MapCodec<E> child, BiFunction<P, E, C> combiner, Function<C, E> extractor) {
		return new CombiningCodec<>(parent, child, combiner, c -> Pair.of(c, extractor.apply(c)));
	}

	public static <P, C extends P, E> Codec<C> ofExtending(Codec<P> parent, Codec<E> child, BiFunction<P, E, C> combiner, Function<C, E> extractor) {
		return of(parent, child, combiner, c -> Pair.of(c, extractor.apply(c)));
	}

	@Override
	public <T> Stream<T> keys(DynamicOps<T> ops) {
		return Stream.concat(first.keys(ops), second.keys(ops));
	}

	@Override
	public <T> DataResult<AB> decode(DynamicOps<T> ops, MapLike<T> input) {
		DataResult<A> parentResult = first.decode(ops, input);
		Optional<DataResult.Error<A>> parentError = parentResult.error();
		Lifecycle parentLifecycle = parentResult.lifecycle();

		if (parentError.isPresent()) {
			return DataResult.error(() -> parentError.get().message(), parentLifecycle);
		}

		@SuppressWarnings("OptionalGetWithoutIsPresent")
		A parentValue = parentResult.result().get();

		DataResult<B> childResult = second.decode(ops, input);
		Optional<DataResult.Error<B>> childError = childResult.error();
		Lifecycle childLifecycle = childResult.lifecycle();

		if (childError.isPresent()) {
			return DataResult.error(() -> childError.get().message(), childLifecycle);
		}

		@SuppressWarnings("OptionalGetWithoutIsPresent")
		B childValue = childResult.result().get();

		Lifecycle lifecycle = parentLifecycle.add(childLifecycle);

		return DataResult.success(
				combiner.apply(parentValue, childValue),
				lifecycle
		);
	}

	@Override
	public <T> RecordBuilder<T> encode(AB input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
		Pair<A, B> pair = extractor.apply(input);

		RecordBuilder<T> parentResult = first.encode(pair.getFirst(), ops, prefix);

		return second.encode(pair.getSecond(), ops, parentResult);
	}
}
