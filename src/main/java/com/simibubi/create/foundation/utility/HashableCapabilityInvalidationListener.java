package com.simibubi.create.foundation.utility;

import java.util.Objects;

import net.neoforged.neoforge.capabilities.ICapabilityInvalidationListener;

public class HashableCapabilityInvalidationListener<H> implements ICapabilityInvalidationListener {
	private final ICapabilityInvalidationListener listener;
	private final H hashKey;

	public HashableCapabilityInvalidationListener(ICapabilityInvalidationListener listener, H hashKey) {
		this.listener = listener;
		this.hashKey = hashKey;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		HashableCapabilityInvalidationListener<?> that = (HashableCapabilityInvalidationListener<?>) o;
		return Objects.equals(hashKey, that.hashKey);
	}

	@Override
	public int hashCode() {
		return Objects.hash(hashKey);
	}

	@Override
	public boolean onInvalidate() {
		return listener.onInvalidate();
	}
}
