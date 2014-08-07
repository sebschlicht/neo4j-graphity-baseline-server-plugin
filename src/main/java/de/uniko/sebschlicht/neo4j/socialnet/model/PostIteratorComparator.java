package de.uniko.sebschlicht.neo4j.socialnet.model;

import java.util.Comparator;

// TODO documentation
public class PostIteratorComparator implements Comparator<PostIterator> {

    @Override
    public int compare(final PostIterator iter1, final PostIterator iter2) {
        if (iter1.getCrrPublished() > iter2.getCrrPublished()) {
            return 1;
        } else {
            return -1;
        }
    }
}
