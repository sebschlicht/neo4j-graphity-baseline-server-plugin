package de.uniko.sebschlicht.neo4j.graphity;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import de.uniko.sebschlicht.neo4j.socialnet.EdgeType;

public class WriteOptimizedGraphity extends Graphity {

    public WriteOptimizedGraphity(
            GraphDatabaseService graphDb) {
        super(graphDb);
    }

    public boolean addFollowship(Node nFollowing, Node nFollowed) {
        // try to find the node of the user followed
        for (Relationship followship : nFollowing.getRelationships(
                EdgeType.FOLLOWS, Direction.OUTGOING)) {
            if (followship.getEndNode().equals(nFollowed)) {
                return false;
            }
        }

        // create star topology
        nFollowing.createRelationshipTo(nFollowed, EdgeType.FOLLOWS);
        return true;
    }

    public boolean removeFollowship(Node nFollowing, Node nFollowed) {
        // delete the followship if existing
        Relationship followship = null;
        for (Relationship follows : nFollowing.getRelationships(
                Direction.OUTGOING, EdgeType.FOLLOWS)) {
            if (follows.getEndNode().equals(nFollowed)) {
                followship = follows;
                break;
            }
        }

        // there is no such followship existing
        if (followship == null) {
            return false;
        }

        followship.delete();
        return true;
    }
}
