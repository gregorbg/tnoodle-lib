package org.worldcubeassociation.tnoodle.scrambles;

public interface ScrambleCacherListener<PS extends PuzzleState<PS>> {
    void scrambleCacheUpdated(ScrambleCacher<PS> src);
}
