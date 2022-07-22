package org.worldcubeassociation.tnoodle.util;

import java.util.LinkedList;

public class Bucket<H> implements Comparable<Bucket<H>> {
    private LinkedList<H> contents;
    private int value;
    public Bucket(int value) {
        this.value = value;
        this.contents = new LinkedList<>();
    }

    public int getValue() {
        return this.value;
    }

    public H pop() {
        return contents.removeLast();
    }

    public void push(H element) {
        contents.addLast(element);
    }

    public boolean isEmpty() {
        return contents.isEmpty();
    }

    public String toString() {
        return "#: " + value + ": " + contents.toString();
    }

    @Override
    public int compareTo(Bucket<H> other) {
        return this.value - other.value;
    }

    public int hashCode() {
        return this.value;
    }

    public boolean equals(Object o) {
        Bucket<?> other = (Bucket<?>) o;
        return this.value == other.value;
    }
}
