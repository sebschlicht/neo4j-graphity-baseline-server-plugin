package de.uniko.sebschlicht.neo4j;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

public abstract class Walker {

    public static Node nextNode(Node sourceNode, RelationshipType edgeType) {
        return sourceNode.getSingleRelationship(edgeType, Direction.OUTGOING)
                .getEndNode();
    }
}
