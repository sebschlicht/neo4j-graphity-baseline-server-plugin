package de.uniko.sebschlicht.neo4j.graphity;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import de.uniko.sebschlicht.neo4j.Walker;
import de.uniko.sebschlicht.neo4j.socialnet.EdgeType;
import de.uniko.sebschlicht.neo4j.socialnet.NodeType;
import de.uniko.sebschlicht.neo4j.socialnet.model.StatusUpdateProxy;
import de.uniko.sebschlicht.neo4j.socialnet.model.UserProxy;
import de.uniko.sebschlicht.socialnet.StatusUpdate;

// TODO documentation
/**
 * Graphity implementation optimized for write requests
 * 
 * @author Rene Pickhardt, Jonas Kunze, sebschlicht
 * 
 */
public class WriteOptimizedGraphity extends Graphity {

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

    //
    //    public static void main(String[] args) {
    //        GraphDatabaseBuilder builder =
    //                new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(
    //                        new File("/tmp/testdb").getAbsolutePath()).setConfig(
    //                        GraphDatabaseSettings.cache_type, "none");
    //        GraphDatabaseService graphDb = builder.newGraphDatabase();
    //        Graphity graphity = new WriteOptimizedGraphity(graphDb);
    //        try (Transaction tx = graphDb.beginTx()) {
    //            System.out.println(graphity.addStatusUpdate("5", "testy"));
    //            tx.success();
    //        }
    //        graphDb.shutdown();
    //    }
}
