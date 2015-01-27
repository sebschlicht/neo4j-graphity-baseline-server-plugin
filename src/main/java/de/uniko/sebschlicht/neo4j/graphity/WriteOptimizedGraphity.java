package de.uniko.sebschlicht.neo4j.graphity;

import java.io.File;
import java.util.TreeSet;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import de.uniko.sebschlicht.graphity.exception.IllegalUserIdException;
import de.uniko.sebschlicht.graphity.exception.UnknownReaderIdException;
import de.uniko.sebschlicht.neo4j.Walker;
import de.uniko.sebschlicht.neo4j.socialnet.EdgeType;
import de.uniko.sebschlicht.neo4j.socialnet.NodeType;
import de.uniko.sebschlicht.neo4j.socialnet.model.PostIteratorComparator;
import de.uniko.sebschlicht.neo4j.socialnet.model.StatusUpdateProxy;
import de.uniko.sebschlicht.neo4j.socialnet.model.UserPostIterator;
import de.uniko.sebschlicht.neo4j.socialnet.model.UserProxy;
import de.uniko.sebschlicht.socialnet.StatusUpdate;
import de.uniko.sebschlicht.socialnet.StatusUpdateList;

// TODO documentation
/**
 * Graphity implementation optimized for write requests
 * 
 * @author Rene Pickhardt, Jonas Kunze, sebschlicht
 * 
 */
public class WriteOptimizedGraphity extends Neo4jGraphity {

    public WriteOptimizedGraphity(
            GraphDatabaseService graphDb) {
        super(graphDb);
    }

    @Override
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

    @Override
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

    @Override
    protected long addStatusUpdate(Node nAuthor, StatusUpdate statusUpdate) {
        // get last recent status update
        Node lastUpdate = Walker.nextNode(nAuthor, EdgeType.PUBLISHED);

        // create new status update node and fill via proxy
        Node crrUpdate = graphDb.createNode(NodeType.UPDATE);
        StatusUpdateProxy pStatusUpdate = new StatusUpdateProxy(crrUpdate);
        //TODO handle service overload
        pStatusUpdate.init();
        pStatusUpdate.setAuthor(new UserProxy(nAuthor));
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
        nAuthor.setProperty(UserProxy.PROP_LAST_STREAM_UDPATE,
                statusUpdate.getPublished());

        return pStatusUpdate.getIdentifier();
    }

    @Override
    protected StatusUpdateList readStatusUpdates(
            Node nReader,
            int numStatusUpdates) {
        StatusUpdateList statusUpdates = new StatusUpdateList();
        final TreeSet<UserPostIterator> postIterators =
                new TreeSet<UserPostIterator>(new PostIteratorComparator());

        // loop through users followed
        UserProxy pCrrUser;
        UserPostIterator postIterator;
        for (Relationship relationship : nReader.getRelationships(
                EdgeType.FOLLOWS, Direction.OUTGOING)) {
            // add post iterator
            pCrrUser = new UserProxy(relationship.getEndNode());
            postIterator = new UserPostIterator(pCrrUser);

            if (postIterator.hasNext()) {
                postIterators.add(postIterator);
            }
        }

        // handle queue
        while ((statusUpdates.size() < numStatusUpdates)
                && !postIterators.isEmpty()) {
            // add last recent status update
            postIterator = postIterators.pollLast();
            statusUpdates.add(postIterator.next().getStatusUpdate());

            // re-add iterator if not empty
            if (postIterator.hasNext()) {
                postIterators.add(postIterator);
            }
        }

        //            // access single stream only
        //            final UserProxy posterNode = new UserProxy(nSource);
        //            UserPostIterator postIterator = new UserPostIterator(posterNode);
        //
        //            while ((statusUpdates.size() < numStatusUpdates)
        //                    && postIterator.hasNext()) {
        //                statusUpdates.add(postIterator.next().getStatusUpdate());
        //            }

        return statusUpdates;
    }

    public static void main(String[] args) {
        GraphDatabaseBuilder builder =
                new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(
                        new File("/tmp/testdb").getAbsolutePath()).setConfig(
                        GraphDatabaseSettings.cache_type, "none");
        GraphDatabaseService graphDb = builder.newGraphDatabase();
        Neo4jGraphity graphity = new WriteOptimizedGraphity(graphDb);
        try {
            System.out.println(graphity.addFollowship("1", "2"));
            System.out.println(graphity.addFollowship("1", "3"));
            System.out.println(graphity.addFollowship("1", "4"));

            System.out.println(graphity.addFollowship("2", "1"));
            System.out.println(graphity.addFollowship("2", "4"));

            System.out.println(graphity.addStatusUpdate("4", "mine"));
            System.out.println(graphity.addStatusUpdate("4", "of"));
            System.out.println(graphity.addStatusUpdate("3", "friend"));
            System.out.println(graphity.addStatusUpdate("2", "dear"));
            System.out.println(graphity.addStatusUpdate("2", "my"));
            System.out.println(graphity.addStatusUpdate("3", "hello"));

            System.out.println("-------");
            System.out.println(graphity.readStatusUpdates("1", 15));
            System.out.println("-------");
            System.out.println(graphity.readStatusUpdates("2", 2));
            System.out.println("-------");
            System.out.println(graphity.readStatusUpdates("2", 1));
            if (graphity.readStatusUpdates("3", 10).size() == 0) {
                System.out.println("...");
            }
        } catch (IllegalUserIdException | UnknownReaderIdException e) {
            e.printStackTrace();
        }
        graphDb.shutdown();
    }
}
