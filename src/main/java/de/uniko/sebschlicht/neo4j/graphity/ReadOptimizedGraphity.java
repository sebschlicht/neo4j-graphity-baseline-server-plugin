package de.uniko.sebschlicht.neo4j.graphity;

import java.util.TreeSet;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import de.uniko.sebschlicht.neo4j.Walker;
import de.uniko.sebschlicht.neo4j.socialnet.EdgeType;
import de.uniko.sebschlicht.neo4j.socialnet.NodeType;
import de.uniko.sebschlicht.neo4j.socialnet.model.PostIteratorComparator;
import de.uniko.sebschlicht.neo4j.socialnet.model.StatusUpdateProxy;
import de.uniko.sebschlicht.neo4j.socialnet.model.UserPostIterator;
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

    /**
     * update the ego network of a user
     * 
     * @param user
     *            user where changes have occurred
     */
    private void updateEgoNetwork(final Node user) {
        Node followedReplica, followingUser, lastPosterReplica;
        Node prevReplica, nextReplica;
        // loop through followers
        for (Relationship relationship : user.getRelationships(
                EdgeType.REPLICA, Direction.INCOMING)) {
            // load each replica and the user corresponding
            followedReplica = relationship.getStartNode();
            followingUser =
                    Walker.previousNode(followedReplica, EdgeType.FOLLOWS);
            // bridge user node
            prevReplica =
                    Walker.previousNode(followedReplica, EdgeType.GRAPHITY);
            if (!prevReplica.equals(followingUser)) {
                followedReplica.getSingleRelationship(EdgeType.GRAPHITY,
                        Direction.INCOMING).delete();
                nextReplica =
                        Walker.nextNode(followedReplica, EdgeType.GRAPHITY);
                if (nextReplica != null) {
                    followedReplica.getSingleRelationship(EdgeType.GRAPHITY,
                            Direction.OUTGOING).delete();
                    prevReplica.createRelationshipTo(nextReplica,
                            EdgeType.GRAPHITY);
                }
            }
            // insert user's replica at its new position
            lastPosterReplica =
                    Walker.nextNode(followingUser, EdgeType.GRAPHITY);
            if (!lastPosterReplica.equals(followedReplica)) {
                followingUser.getSingleRelationship(EdgeType.GRAPHITY,
                        Direction.OUTGOING).delete();
                followingUser.createRelationshipTo(followedReplica,
                        EdgeType.GRAPHITY);
                followedReplica.createRelationshipTo(lastPosterReplica,
                        EdgeType.GRAPHITY);
            }
        }
    }

    @Override
    protected long addStatusUpdate(Node nAuthor, StatusUpdate statusUpdate) {
        // get last recent status update
        final Node lastUpdate = Walker.nextNode(nAuthor, EdgeType.PUBLISHED);

        // create new status update node and fill via proxy
        Node crrUpdate = graphDb.createNode(NodeType.UPDATE);
        StatusUpdateProxy pStatusUpdate = new StatusUpdateProxy(crrUpdate);
        //TODO handle service overload
        pStatusUpdate.init();
        UserProxy pAuthor = new UserProxy(nAuthor);
        pStatusUpdate.setAuthor(pAuthor);
        pStatusUpdate.setMessage(statusUpdate.getMessage());
        pStatusUpdate.setPublished(statusUpdate.getPublished());

        // update references to previous status update (if existing)
        if (lastUpdate != null) {
            nAuthor.getSingleRelationship(EdgeType.PUBLISHED,
                    Direction.OUTGOING).delete();
            crrUpdate.createRelationshipTo(lastUpdate, EdgeType.PUBLISHED);
        }
        // add reference from user to current update node
        nAuthor.createRelationshipTo(crrUpdate, EdgeType.PUBLISHED);
        pAuthor.setLastPostTimestamp(statusUpdate.getPublished());

        // update ego networks of status update author followers
        updateEgoNetwork(nAuthor);

        return pStatusUpdate.getIdentifier();
    }

    @Override
    protected StatusUpdateList readStatusUpdates(
            Node nReader,
            int numStatusUpdates) {
        StatusUpdateList statusUpdates = new StatusUpdateList();
        final TreeSet<UserPostIterator> postIterators =
                new TreeSet<UserPostIterator>(new PostIteratorComparator());

        // load first user by replica
        UserProxy pCrrUser = null;
        UserPostIterator userPostIterator;
        Node nReplica = Walker.nextNode(nReader, EdgeType.GRAPHITY);
        if (nReplica != null) {
            pCrrUser =
                    new UserProxy(Walker.nextNode(nReplica, EdgeType.REPLICA));
            userPostIterator = new UserPostIterator(pCrrUser);
            userPostIterator.setReplicaNode(nReplica);

            if (userPostIterator.hasNext()) {
                postIterators.add(userPostIterator);
            }
        }

        // handle user queue
        UserProxy pPrevUser = pCrrUser;
        while (statusUpdates.size() < numStatusUpdates
                && !postIterators.isEmpty()) {
            // add last recent status update
            userPostIterator = postIterators.pollLast();
            statusUpdates.add(userPostIterator.next().getStatusUpdate());

            // re-add iterator if not empty
            if (userPostIterator.hasNext()) {
                postIterators.add(userPostIterator);
            }

            // load additional user if necessary
            if (userPostIterator.getUser() == pPrevUser) {
                nReplica =
                        Walker.nextNode(userPostIterator.getReplicaNode(),
                                EdgeType.GRAPHITY);
                // check if additional user existing
                if (nReplica != null) {
                    pCrrUser =
                            new UserProxy(Walker.nextNode(nReplica,
                                    EdgeType.REPLICA));
                    userPostIterator = new UserPostIterator(pCrrUser);
                    userPostIterator.setReplicaNode(nReplica);
                    // check if user has status updates
                    if (userPostIterator.hasNext()) {
                        postIterators.add(userPostIterator);
                        pPrevUser = pCrrUser;
                    } else {
                        // further users do not need to be loaded
                        pPrevUser = null;
                    }
                }
            }
        }

        //            // access single stream only
        //            final UserProxy posterNode = new UserProxy(nReader);
        //            UserPostIterator postIterator = new UserPostIterator(posterNode);
        //
        //            while ((statusUpdates.size() < numStatusUpdates)
        //                    && postIterator.hasNext()) {
        //                statusUpdates.add(postIterator.next().getStatusUpdate());
        //            }

        return statusUpdates;
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
