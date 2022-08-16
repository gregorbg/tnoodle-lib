package org.worldcubeassociation.tnoodle.scrambles;

public interface ScrambleCacherListener {
    void scrambleCacheUpdated(ScrambleCacher src);

    void scrambleGenerated(String scramble);
    void scrambleRetrieved(String scramble);
}
