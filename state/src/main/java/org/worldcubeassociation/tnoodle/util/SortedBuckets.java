package org.worldcubeassociation.tnoodle.util;

import java.util.TreeSet;

public class SortedBuckets<H> {
    protected final TreeSet<Bucket<H>> buckets;
    public SortedBuckets() {
        buckets = new TreeSet<>();
    }

    public void add(H element, int value) {
        Bucket<H> bucket;
        Bucket<H> searchBucket = new Bucket<>(value);
        if(!buckets.contains(searchBucket)) {
            // There is no bucket yet for value, so we create one.
            bucket = searchBucket;
            buckets.add(bucket);
        } else {
            bucket = buckets.tailSet(searchBucket).first();
        }
        bucket.push(element);
    }

    public int smallestValue() {
        return buckets.first().getValue();
    }

    public boolean isEmpty() {
        return buckets.size() == 0;
    }

    public H pop() {
        Bucket<H> bucket = buckets.first();
        H h = bucket.pop();
        if(bucket.isEmpty()) {
            // We just removed the last element from this bucket,
            // so we can trash the bucket now.
            buckets.remove(bucket);
        }
        return h;
    }

    public String toString() {
        return buckets.toString();
    }

    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    public boolean equals(Object o) {
        throw new UnsupportedOperationException();
    }
}
