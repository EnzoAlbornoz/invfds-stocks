package br.ufsc.investfunds.companies.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
// import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

public class StreamUtils {
    // public static <T> Stream<Stream<T>> lazyChunkedStream(Stream<T>
    // streamToChunk, int chunkSize) {
    // var chunks = new AtomicInteger();
    // Stream.iterate(streamToChunk.limit(chunkSize), hasNext, next)
    // }

    // Reference: https://stackoverflow.com/questions/32434592/partition-a-java-8-stream
    public static <T> Stream<List<T>> partition(Stream<T> stream, int batchSize) {
        List<List<T>> currentBatch = new ArrayList<List<T>>(); // just to make it mutable
        currentBatch.add(new ArrayList<T>(batchSize));
        return Stream.concat(stream.sequential().map(new Function<T, List<T>>() {
            public List<T> apply(T t) {
                currentBatch.get(0).add(t);
                return currentBatch.get(0).size() == batchSize ? currentBatch.set(0, new ArrayList<>(batchSize)) : null;
            }
        }), Stream.generate(() -> currentBatch.get(0).isEmpty() ? null : currentBatch.get(0)).limit(1))
                .filter(Objects::nonNull);
    }
}
