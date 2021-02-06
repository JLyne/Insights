package dev.frankheijden.insights.api.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public interface SetCollector<T> extends Collector<T, Set<T>, Set<T>> {

    Set<T> supplySet();

    @Override
    default Supplier<Set<T>> supplier() {
        return this::supplySet;
    }

    @Override
    default BiConsumer<Set<T>, T> accumulator() {
        return Set::add;
    }

    @Override
    default BinaryOperator<Set<T>> combiner() {
        return (s1, s2) -> {
            s1.addAll(s2);
            return s1;
        };
    }

    @Override
    default Function<Set<T>, Set<T>> finisher() {
        return Function.identity();
    }

    @Override
    default Set<Collector.Characteristics> characteristics() {
        return new HashSet<>(Collections.singleton(Collector.Characteristics.UNORDERED));
    }
}
