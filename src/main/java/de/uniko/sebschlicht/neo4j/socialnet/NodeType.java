package de.uniko.sebschlicht.neo4j.socialnet;

import org.neo4j.graphdb.Label;

/**
 * items the social graph node can represent
 * 
 * @author sebschlicht
 * 
 */
public enum NodeType implements Label {

    /**
     * user that can act in the social network
     */
    USER;
}
