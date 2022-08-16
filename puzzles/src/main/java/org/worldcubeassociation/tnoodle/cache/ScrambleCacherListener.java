package org.worldcubeassociation.tnoodle.cache;

public interface ScrambleCacherListener {
    void scrambleCacheUpdated(ScrambleCacher src);

    void scrambleGenerated(String scramble);
    void scrambleRetrieved(String scramble);
}
