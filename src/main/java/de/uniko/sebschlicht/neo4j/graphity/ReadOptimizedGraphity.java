package de.uniko.sebschlicht.neo4j.graphity;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import de.uniko.sebschlicht.neo4j.Walker;
import de.uniko.sebschlicht.neo4j.socialnet.EdgeType;
import de.uniko.sebschlicht.neo4j.socialnet.model.UserProxy;
import de.uniko.sebschlicht.socialnet.StatusUpdate;
import de.uniko.sebschlicht.socialnet.StatusUpdateList;

/**
 * Graphity implementation optimized for read requests
 * 
 * @author Rene Pickhardt, Jonas Kunze, sebschlicht
 * 
 */
public class ReadOptimizedGraphity extends Neo4jGraphity {

    public ReadOptimizedGraphity(
            GraphDatabaseService graphDb) {
        super(graphDb);
    }

    @Override
    protected boolean addFollowship(Node nFollowing, Node nFollowed) {
        // try to find the replica node of the user followed
        Node followedReplica = null;
        for (Relationship followship : nFollowing.getRelationships(
                EdgeType.FOLLOWS, Direction.OUTGOING)) {
            followedReplica = followship.getEndNode();
            if (Walker.nextNode(followedReplica, EdgeType.REPLICA).equals(
                    nFollowed)) {
                break;
            }
            followedReplica = null;
        }
        // user is following already
        if (followedReplica != null) {
            return false;
        }
        // create replica
        final Node newReplica = graphDb.createNode();
        nFollowing.createRelationshipTo(newReplica, EdgeType.FOLLOWS);
        newReplica.createRelationshipTo(nFollowed, EdgeType.REPLICA);
        // check if followed user is the first in following's ego network
        if (Walker.nextNode(nFollowing, EdgeType.GRAPHITY) == null) {
            nFollowing.createRelationshipTo(newReplica, EdgeType.GRAPHITY);
        } else {
            // search for insertion index within following replica layer
            final long followedTimestamp = getLastUpdateByReplica(newReplica);
            long crrTimestamp;
            Node prevReplica = nFollowing;
            Node nextReplica = null;
            while (true) {
                // get next user
                nextReplica = Walker.nextNode(prevReplica, EdgeType.GRAPHITY);
                if (nextReplica != null) {
                    crrTimestamp = getLastUpdateByReplica(nextReplica);
                    // step on if current user has newer status updates
                    if (crrTimestamp > followedTimestamp) {
                        prevReplica = nextReplica;
                        continue;
                    }
                }
                // insertion position has been found
                break;
            }
            // insert followed user's replica into following's ego network
            if (nextReplica != null) {
                prevReplica.getSingleRelationship(EdgeType.GRAPHITY,
                        Direction.OUTGOING).delete();
                newReplica.createRelationshipTo(nextReplica, EdgeType.GRAPHITY);
            }
            prevReplica.createRelationshipTo(newReplica, EdgeType.GRAPHITY);
        }
        return true;
    }

    /**
     * remove a followed user from the replica layer
     * 
     * @param followedReplica
     *            replica of the user that will be removed
     */
    private void removeFromReplicaLayer(final Node followedReplica) {
        final Node prev =
                Walker.previousNode(followedReplica, EdgeType.GRAPHITY);
        final Node next = Walker.nextNode(followedReplica, EdgeType.GRAPHITY);
        // bridge the user replica in the replica layer
        prev.getSingleRelationship(EdgeType.GRAPHITY, Direction.OUTGOING)
                .delete();
        if (next != null) {
            next.getSingleRelationship(EdgeType.GRAPHITY, Direction.INCOMING)
                    .delete();
            prev.createRelationshipTo(next, EdgeType.GRAPHITY);
        }
        // remove the followship
        followedReplica.getSingleRelationship(EdgeType.FOLLOWS,
                Direction.INCOMING).delete();
        // remove the replica node itself
        followedReplica.getSingleRelationship(EdgeType.REPLICA,
                Direction.OUTGOING).delete();
        followedReplica.delete();
    }

    @Override
    protected boolean removeFollowship(Node nFollowing, Node nFollowed) {
        // find the replica node of the user followed
        Node followedReplica = null;
        for (Relationship followship : nFollowing.getRelationships(
                EdgeType.FOLLOWS, Direction.OUTGOING)) {
            followedReplica = followship.getEndNode();
            if (Walker.nextNode(followedReplica, EdgeType.REPLICA).equals(
                    nFollowed)) {
                break;
            }
            followedReplica = null;
        }
        // there is no such followship existing
        if (followedReplica == null) {
            return false;
        }
        removeFromReplicaLayer(followedReplica);
        return true;
    }

    @Override
    protected long addStatusUpdate(Node nAuthor, StatusUpdate statusUpdate) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected StatusUpdateList readStatusUpdates(
            Node nReader,
            int numStatusUpdates) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Retrieves the timestamp of the last recent status update of the user
     * specified.
     * 
     * @param userReplica
     *            replica of the user
     * @return timestamp of the user's last recent status update
     */
    private static long getLastUpdateByReplica(final Node userReplica) {
        final Node user = Walker.nextNode(userReplica, EdgeType.REPLICA);
        UserProxy pUser = new UserProxy(user);
        return pUser.getLastPostTimestamp();
    }
}
