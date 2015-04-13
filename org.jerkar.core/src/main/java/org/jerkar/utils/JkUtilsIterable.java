package org.jerkar.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class JkUtilsIterable {

	public static <T> List<T> toList(Iterable<T> it) {
		if (it instanceof List) {
			return (List<T>) it;
		}
		final List<T> result = new LinkedList<T>();
		for (final T t : it) {
			result.add(t);

		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> toGenericList(Iterable<?> it) {
		if (it instanceof List) {
			return (List<T>) it;
		}
		final List<T> result = new LinkedList<T>();
		for (final Object t : it) {
			result.add((T) t);

		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(Iterable<T> it, Class<T> clazz) {
		final List<T> list = toList(it);
		final T[] result = (T[]) Array.newInstance(clazz, list.size());
		int i = 0;
		for (final T t : it) {
			result[i] = t;
			i++;
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] toArrayFromNonGeneric(Iterable<?> it, Class<T> clazz) {
		final List<T> list = toGenericList(it);
		final T[] result = (T[]) Array.newInstance(clazz, list.size());
		int i = 0;
		for (final Object t : it) {
			result[i] = (T) t;
			i++;
		}
		return result;
	}


	public static <T> List<T> listOf(T... items) {
		return Arrays.asList(items);
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> listOfGeneric(Object... items) {
		return ((List<T>) Arrays.asList(items));
	}

	public static <T> Set<T> setOf(T... items) {
		final HashSet<T> result = new HashSet<T>();
		result.addAll(Arrays.asList(items));
		return result;
	}

	public static <T> Set<T> setOf(Iterable<T> items) {
		final HashSet<T> result = new HashSet<T>();
		for (final T item : items) {
			result.add(item);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T, U> Map<T, U> mapOf(T key, U value, Object ...others) {
		final Map<T, U> result = new HashMap<T, U>();
		result.put(key, value);
		for (int i = 0; i<others.length; i = i + 2) {
			final T otherKey = (T) others[i];
			final U otherValue = (U) others[i+1];
			result.put(otherKey, otherValue);
		}
		return result;
	}


	public static <T> Iterable<T> chain(Iterable<T> ... iterables) {
		return chainAll(Arrays.asList(iterables));
	}

	public static <T> Iterable<T> of(T ...items) {
		return Arrays.asList(items);
	}

	public static <T> Iterable<T> chain(T item, Iterable<T> ... iterables) {
		final List<Iterable<T>> list = new LinkedList<Iterable<T>>();
		final List<T> single = new ArrayList<T>(3);
		single.add(item);
		list.add(single);
		list.addAll(Arrays.asList(iterables));
		return chainAll(list);
	}

	public static <T> Iterable<T> chainAll(Iterable<Iterable<T>> iterables) {
		final List<Iterable<T>> effectiveIterables = removeEmptyIt(iterables);
		if (effectiveIterables.isEmpty()) {
			return Collections.emptyList();
		}
		if (effectiveIterables.size() == 1) {
			return effectiveIterables.get(0);
		}
		return new ChainedIterable<T>(effectiveIterables);
	}

	private static <T> List<Iterable<T>> removeEmptyIt(Iterable<Iterable<T>> iterables) {
		final List<Iterable<T>> result = new LinkedList<Iterable<T>>();
		for (final Iterable<T> iterable : iterables) {
			if (iterable.iterator().hasNext()) {
				result.add(iterable);
			}
		}
		return result;
	}

	private static final class ChainedIterable<T> implements Iterable<T> {

		private final Iterable<Iterable<T>> iterables;

		public ChainedIterable(Iterable<Iterable<T>> iterables) {
			super();
			this.iterables = iterables;
		}


		@Override
		public Iterator<T> iterator() {
			final List<Iterator<T>> iterators = new LinkedList<Iterator<T>>();
			for (final Iterable<T> iterable : iterables) {
				iterators.add(iterable.iterator());
			}
			return new ChainedIterator<T>(iterators);
		}

	}


	private static final class ChainedIterator<T> implements Iterator<T> {

		private final Iterator<Iterator<T>> iterators;

		private Iterator<T> current;

		public ChainedIterator(Iterable<Iterator<T>> iterables) {
			super();
			this.iterators = iterables.iterator();
			current = iterators.next();
		}


		@Override
		public boolean hasNext() {
			final boolean currentNext = current.hasNext();
			if (currentNext) {
				return true;
			}
			while(iterators.hasNext()) {
				current = iterators.next();
				if (current.hasNext()) {
					return true;
				}
			}
			return false;
		}

		@Override
		public T next() {
			if (current.hasNext()) {
				return current.next();
			}
			while(iterators.hasNext()) {
				current = iterators.next();
				if (current.hasNext()) {
					return current.next();
				}
			}
			return current.next();
		}

		@Override
		public void remove() {
			current.remove();
		}

	}


	/**
	 * Convenient method to put several entry in a map having the same value at once.
	 */
	public static <K, V> void putMultiEntry(Map<K,V> map, Iterable<K> keys, V value) {
		for (final K key : keys) {
			map.put(key, value);
		}
	}

	public static <T> List<T> concatLists(Iterable<? extends T> ...lists) {
		final List<T> result = new LinkedList<T>();
		for (final Iterable<? extends T> list : lists) {
			for (final T item : list) {
				result.add(item);
			}
		}
		return result;
	}


	public static <T> List<T> concatToList(T item, Iterable<? extends T> ...lists) {
		final List<T> result = new LinkedList<T>();
		result.add(item);
		result.addAll(concatLists(lists));
		return result;
	}

	public static Map<String, String> propertiesToMap(Properties properties) {
		final Map<String, String> result = new HashMap<String, String>();
		for (final Object propKey : properties.keySet()) {
			result.put(propKey.toString(), properties.getProperty(propKey.toString()));
		}
		return result;
	}

}