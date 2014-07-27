package de.uniko.sebschlicht.neo4j.socialnet;

import org.neo4j.graphdb.RelationshipType;

/**
 * relations the social graph edge can represent
 * 
 * @author sebschlicht
 * 
 */
public enum EdgeType implements RelationshipType {

    /**
     * one user follows another one
     */
    FOLLOWS;
}
