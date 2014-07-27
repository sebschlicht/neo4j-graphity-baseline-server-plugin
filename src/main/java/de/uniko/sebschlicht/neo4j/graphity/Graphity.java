package de.uniko.sebschlicht.neo4j.graphity;

import java.util.concurrent.TimeUnit;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.IndexDefinition;

import de.uniko.sebschlicht.neo4j.socialnet.NodeType;
import de.uniko.sebschlicht.neo4j.socialnet.SocialGraph;
import de.uniko.sebschlicht.neo4j.socialnet.model.User;

/**
 * social graph for Graphity implementations
 * 
 * @author sebschlicht
 * 
 */
public abstract class Graphity extends SocialGraph {

    /**
     * Creates a new Graphity instance using the database provided.
     * 
     * @param graphDb
     *            graph database holding any Graphity social network graph to
     *            operate on
     */
    public Graphity(
            GraphDatabaseService graphDb) {
        super(graphDb);
    }

    @Override
    public void init() {
        // create user identifier index if not existing
        IndexDefinition indexUserId =
                this.loadIndexDefinition(NodeType.USER, User.PROP_IDENTIFIER);
        if (indexUserId == null) {
            try (Transaction tx = this.graphDb.beginTx()) {
                this.graphDb.schema().indexFor(NodeType.USER)
                        .on(User.PROP_IDENTIFIER).create();
                tx.success();
            }
        }

        try (Transaction tx = this.graphDb.beginTx()) {
            this.graphDb.schema().awaitIndexesOnline(10, TimeUnit.SECONDS);
        }
    }

    /**
     * Searches the social network graph for an user.
     * 
     * @param userIdentifier
     *            identifier of the user searched
     * @return user node - if the user is existing in social network graph<br>
     *         <b>null</b> - if there is no node representing the user specified
     */
    protected Node findUser(String userIdentifier) {
        try (ResourceIterator<Node> users =
                this.graphDb.findNodesByLabelAndProperty(NodeType.USER,
                        User.PROP_IDENTIFIER, userIdentifier).iterator()) {
            if (users.hasNext()) {
                return users.next();
            }
        }
        return null;
    }

    /**
     * Loads a user from social network or lazily creates a new one.
     * 
     * @param userIdentifier
     *            identifier of the user to interact with
     * @return user node - existing or created node representing the user
     */
    protected Node loadUser(String userIdentifier) {
        Node nUser = this.findUser(userIdentifier);
        if (nUser != null) {
            // user is already existing
            return nUser;
        }
        return this.createUser(userIdentifier);
    }

    @Override
    public boolean addUser(String userIdentifier) {
        Node nUser = this.findUser(userIdentifier);
        if (nUser == null) {
            // user identifier not in use yet
            this.createUser(userIdentifier);
            return true;
        }
        return false;
    }

    @Override
    public boolean addFollowship(String idFollowing, String idFollowed) {
        Node nFollowing = this.loadUser(idFollowing);
        Node nFollowed = this.loadUser(idFollowed);
        return this.addFollowship(nFollowing, nFollowed);
    }

    //TODO documentation
    abstract protected boolean addFollowship(Node nFollowing, Node nFollowed);

    //DEBUG
    public static void main(String[] args) {
        GraphDatabaseService graphDb =
                new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(
                        "/tmp/test").newGraphDatabase();
        Graphity g = new WriteOptimizedGraphity(graphDb);
        g.init();
        graphDb.shutdown();
    }
}
