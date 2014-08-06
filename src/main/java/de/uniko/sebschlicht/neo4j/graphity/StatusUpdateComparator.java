package de.uniko.sebschlicht.neo4j.graphity;

import java.util.Comparator;

import de.uniko.sebschlicht.neo4j.socialnet.model.StatusUpdateProxy;

// TODO documentation
public class StatusUpdateComparator implements Comparator<StatusUpdateProxy> {

    @Override
    public int
        compare(final StatusUpdateProxy su1, final StatusUpdateProxy su2) {
        if (su1.getPublished() > su2.getPublished()) {
            return 1;
        } else {
            return -1;
        }
    }
}
