package de.uniko.sebschlicht.neo4j.socialnet.model;

import java.util.Iterator;

public interface PostIterator extends Iterator<StatusUpdateProxy> {

    long getCrrPublished();
}
