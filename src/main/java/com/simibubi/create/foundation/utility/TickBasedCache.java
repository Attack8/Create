package com.simibubi.create.foundation.utility;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.mutable.MutableInt;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;
import com.google.common.collect.ImmutableMap;

public class TickBasedCache<K, V> implements Cache<K, V> {

	private static int currentTick = 0;

	public static void tick() {
		currentTick++;
	}

	//

	private Map<K, MutableInt> timestamps = new HashMap<>();
	private ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();

	private int ticksUntilTimeout;
	private boolean resetTimerOnAccess;

	public TickBasedCache(int ticksUntilTimeout, boolean resetTimerOnAccess) {
		this.ticksUntilTimeout = ticksUntilTimeout;
		this.resetTimerOnAccess = resetTimerOnAccess;
	}

	@Override
	public V getIfPresent(Object key) {
		MutableInt timestamp = timestamps.get(key);
		if (timestamp == null)
			return null;
		if (timestamp.intValue() < currentTick - ticksUntilTimeout) {
			timestamps.remove(key);
			map.remove(key);
			return null;
		}
		if (resetTimerOnAccess)
			timestamp.setValue(currentTick);
		return map.get(key);
	}

	@Override
	public V get(K key, Callable<? extends V> loader) throws ExecutionException {
		V ifPresent = getIfPresent(key);
		if (ifPresent != null)
			return ifPresent;
		try {
			V entry = loader.call();
			map.put(key, entry);
			timestamps.put(key, now());
			return entry;
		} catch (Exception e) {
			throw new ExecutionException(e);
		}
	}

	private MutableInt now() {
		return new MutableInt(currentTick);
	}

	@Override
	public ImmutableMap<K, V> getAllPresent(Iterable<? extends Object> keys) {
		cleanUp();
		return ImmutableMap.copyOf(map);
	}

	@Override
	public void put(K key, V value) {
		map.put(key, value);
		timestamps.put(key, now());
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		m.forEach(this::put);
	}

	@Override
	public void invalidate(Object key) {
		map.remove(key);
		timestamps.remove(key);
	}

	@Override
	public void invalidateAll(Iterable<? extends Object> keys) {
		keys.forEach(this::invalidate);
	}

	@Override
	public void invalidateAll() {
		map.clear();
		timestamps.clear();
	}

	@Override
	public long size() {
		cleanUp();
		return timestamps.size();
	}

	@Override
	public CacheStats stats() {
		return new CacheStats(0, 0, 0, 0, 0, 0);
	}

	@Override
	public ConcurrentMap<K, V> asMap() {
		return map;
	}

	@Override
	public void cleanUp() {
		Set<K> outdated = new HashSet<>();
		timestamps.forEach((k, v) -> {
			if (v.intValue() < currentTick - ticksUntilTimeout)
				outdated.add(k);
			if (resetTimerOnAccess)
				v.setValue(currentTick);
		});
		outdated.forEach(map::remove);
		outdated.forEach(timestamps::remove);
	}

}
