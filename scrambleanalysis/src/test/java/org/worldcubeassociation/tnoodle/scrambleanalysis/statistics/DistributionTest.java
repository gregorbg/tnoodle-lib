package org.worldcubeassociation.tnoodle.scrambleanalysis.statistics;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class DistributionTest {
    @Test
    public void minimumSampleSizeTest() {
        assertEquals(Distribution.minimumSampleSize(), 6144);
    }
}
