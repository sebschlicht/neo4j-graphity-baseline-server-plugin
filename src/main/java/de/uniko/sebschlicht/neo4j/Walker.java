package de.uniko.sebschlicht.neo4j;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

/**
 * graph walker for Neo4j
 * 
 * @author sebschlicht
 * 
 */
public abstract class Walker {

    /**
     * Walks along an edge type to the next node.
     * 
     * @param sourceNode
     *            node to start from
     * @param edgeType
     *            edge type to walk along
     * @return next node the edge specified directs to<br>
     *         <b>null</b> - if the start node has no such edge directing out
     * @see org.neo4j.graphdb.Node.getSingleRelationship
     */
    public static Node nextNode(Node sourceNode, RelationshipType edgeType) {
        for (Relationship edge : sourceNode.getRelationships(edgeType,
                Direction.OUTGOING)) {
            return edge.getEndNode();
        }
        return null;
    }
}
