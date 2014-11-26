package org.jake.depmanagement;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jake.utils.JakeUtilsIterable;

public final class JakeScopeMapping {

	// -------- Factory methods ----------------------------

	/**
	 * Returns a basic scope mapping between two specified scopes.
	 */
	public static JakeScopeMapping of(JakeScope from, JakeScope to) {
		final Map<JakeScope, Set<JakeScope>> map = new HashMap<JakeScope, Set<JakeScope>>();
		map.put(from, Collections.unmodifiableSet(JakeUtilsIterable.setOf(to)));
		return new JakeScopeMapping(Collections.unmodifiableMap(map));
	}

	/**
	 * Returns a partially constructed mapping specifying only scope entries and
	 * willing for the mapping values.
	 */
	@SuppressWarnings("unchecked")
	public static JakeScopeMapping.Partial from(JakeScope ...scopes) {
		return new Partial(Arrays.asList(scopes), new JakeScopeMapping(Collections.EMPTY_MAP));
	}

	/**
	 * Returns a basic scope mapping between two specified scopes.
	 */
	public static JakeScopeMapping of(JakeScope from, String ...toScopes) {
		final Set<JakeScope> set = new HashSet<JakeScope>();
		for (final String to : toScopes) {
			set.add(JakeScope.of(to));
		}
		final Map<JakeScope, Set<JakeScope>> map = new HashMap<JakeScope, Set<JakeScope>>();
		map.put(from, Collections.unmodifiableSet(set));
		return new JakeScopeMapping(Collections.unmodifiableMap(map));
	}

	public static JakeScopeMapping of(Iterable<JakeScope> from, Iterable<JakeScope> to) {
		final Map<JakeScope, Set<JakeScope>> map = new HashMap<JakeScope, Set<JakeScope>>();
		for (final JakeScope scope : from) {
			map.put(scope, JakeUtilsIterable.setOf(to));
		}
		return new JakeScopeMapping(map);
	}

	// ---------------- Instance members ---------------------------


	private final Map<JakeScope, Set<JakeScope>> map;

	private JakeScopeMapping(Map<JakeScope, Set<JakeScope>> map) {
		super();
		this.map = map;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + map.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final JakeScopeMapping other = (JakeScopeMapping) obj;
		return (!map.equals(other.map));
	}

	public JakeScopeMapping and(JakeScope from, JakeScope to) {
		return and(from, JakeUtilsIterable.listOf(to));
	}

	public JakeScopeMapping and(JakeScope from, String ...toScopes) {
		final List<JakeScope> list = new LinkedList<JakeScope>();
		for (final String to : toScopes) {
			list.add(JakeScope.of(to));
		}
		return and(from, list);
	}

	public Partial andFrom(JakeScope ...from) {
		return new Partial(Arrays.asList(from), this);
	}

	private JakeScopeMapping and(JakeScope from, Iterable<JakeScope> to) {
		final Map<JakeScope, Set<JakeScope>> result = new HashMap<JakeScope, Set<JakeScope>>(map);
		if (result.containsKey(from)) {
			final Set<JakeScope> list = result.get(from);
			final Set<JakeScope> newList = new HashSet<JakeScope>(list);
			newList.addAll(JakeUtilsIterable.toList(to));
			result.put(from, Collections.unmodifiableSet(newList));
		} else {
			final Set<JakeScope> newList = new HashSet<JakeScope>();
			newList.addAll(JakeUtilsIterable.toList(to));
			result.put(from, Collections.unmodifiableSet(newList));
		}
		return new JakeScopeMapping(result);
	}

	public JakeScopeMapping and(Iterable<JakeScope> from, Iterable<JakeScope> to) {
		JakeScopeMapping result = this;
		for (final JakeScope scope : from) {
			result = result.and(scope, to);
		}
		return result;
	}



	public Set<JakeScope> mappedScopes(JakeScope sourceScope) {
		final Set<JakeScope> result = this.map.get(sourceScope);
		if (result != null && !result.isEmpty()) {
			return result;
		}
		throw new IllegalArgumentException("No mapped scope declared for " + sourceScope + ". Declared scopes are " + this.entries());
	}

	public Set<JakeScope> entries() {
		return Collections.unmodifiableSet(this.map.keySet());
	}

	public Set<JakeScope> involvedScopes() {
		final Set<JakeScope> result = new HashSet<JakeScope>();
		result.addAll(entries());
		for (final JakeScope scope : entries()) {
			result.addAll(this.map.get(scope));
		}
		return Collections.unmodifiableSet(result);
	}

	@Override
	public String toString() {
		return map.toString();
	}



	public static class Partial {

		private final List<JakeScope> from;

		private final JakeScopeMapping mapping;

		private Partial(List<JakeScope> from, JakeScopeMapping mapping) {
			super();
			this.from = from;
			this.mapping = mapping;
		}

		public JakeScopeMapping to(JakeScope... targets) {
			return to(Arrays.asList(targets));
		}

		public JakeScopeMapping to(Iterable<JakeScope> targets) {
			JakeScopeMapping result = mapping;
			for (final JakeScope fromScope : from) {
				for (final JakeScope toScope : targets) {
					result = result.and(fromScope, toScope);
				}
			}
			return result;
		}

	}

}