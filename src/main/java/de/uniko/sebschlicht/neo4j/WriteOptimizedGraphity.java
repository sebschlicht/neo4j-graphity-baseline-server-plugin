package de.uniko.sebschlicht.neo4j;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

/**
 * first social networking approach
 * 
 * @author Rene Pickhardt, Jonas Kunze, Sebastian Schlicht
 * 
 */
public class WriteOptimizedGraphity extends Graphity {

    public WriteOptimizedGraphity(
            final GraphDatabaseService graph) {
        super(graph);
    }

    @Override
    public boolean createFriendship(
            final String idFollowing,
            final String idFollowed) {
        Node nFollowing = this.getOrCreateUser(idFollowing);
        Node nFollowed = this.getOrCreateUser(idFollowed);

        // try to find the node of the user followed
        for (Relationship followship : nFollowing.getRelationships(
                SocialRelationType.FOLLOWS, Direction.OUTGOING)) {
            if (followship.getEndNode().equals(nFollowed)) {
                return false;
            }
        }

        // create star topology
        nFollowing.createRelationshipTo(nFollowed, SocialRelationType.FOLLOWS);
        return true;
    }

    @Override
    public boolean removeFriendship(
            final String idFollowing,
            final String idFollowed) {
        Node nFollowing = this.getOrCreateUser(idFollowing);
        Node nFollowed = this.getOrCreateUser(idFollowed);

        // delete the followship if existing
        Relationship followship = null;
        for (Relationship follows : nFollowing.getRelationships(
                Direction.OUTGOING, SocialRelationType.FOLLOWS)) {
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
