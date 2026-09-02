package com.sixgroup.refit.observability.item35.creator.shared.utils;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.function.Function;

public final class LazyIterators {

    private LazyIterators() {
    }

    public static <S, T> Iterator<T> filterMap(Iterator<S> source, Function<S, Optional<T>> mapper) {
        return new Iterator<>() {
            private T next;
            private boolean prepared;

            @Override
            public boolean hasNext() {
                while (!prepared && source.hasNext()) {
                    Optional<T> mapped = mapper.apply(source.next());
                    if (mapped.isPresent()) {
                        next = mapped.get();
                        prepared = true;
                    }
                }
                return prepared;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                prepared = false;
                return next;
            }
        };
    }

    @SafeVarargs
    public static <T> Iterator<T> concat(Iterator<T>... sources) {
        return new Iterator<>() {
            private int index;

            @Override
            public boolean hasNext() {
                while (index < sources.length && !sources[index].hasNext()) {
                    index++;
                }
                return index < sources.length;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return sources[index].next();
            }
        };
    }

    @SafeVarargs
    public static <T> Iterator<T> mergeSorted(Comparator<T> comparator, Iterator<T>... sources) {
        record Entry<T>(T value, Iterator<T> source, int sourceIndex) {
        }

        PriorityQueue<Entry<T>> queue = new PriorityQueue<>(
            Comparator.<Entry<T>, T>comparing(Entry::value, comparator)
                .thenComparingInt(Entry::sourceIndex));
        for (int sourceIndex = 0; sourceIndex < sources.length; sourceIndex++) {
            Iterator<T> source = sources[sourceIndex];
            if (source.hasNext()) {
                queue.add(new Entry<>(source.next(), source, sourceIndex));
            }
        }

        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return !queue.isEmpty();
            }

            @Override
            public T next() {
                if (queue.isEmpty()) {
                    throw new NoSuchElementException();
                }
                Entry<T> entry = queue.remove();
                if (entry.source().hasNext()) {
                    queue.add(new Entry<>(entry.source().next(), entry.source(), entry.sourceIndex()));
                }
                return entry.value();
            }
        };
    }
}
