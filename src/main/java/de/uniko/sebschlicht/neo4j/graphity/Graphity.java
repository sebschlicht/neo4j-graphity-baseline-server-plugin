package de.uniko.sebschlicht.neo4j.graphity;

import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.functors.ExceptionFactory;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.IndexDefinition;

import de.uniko.sebschlicht.neo4j.graphity.exception.InvalidFollowingId;
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
                loadIndexDefinition(NodeType.USER, User.PROP_IDENTIFIER);
        if (indexUserId == null) {
            try (Transaction tx = graphDb.beginTx()) {
                graphDb.schema().indexFor(NodeType.USER)
                        .on(User.PROP_IDENTIFIER).create();
                tx.success();
            }
        }

        try (Transaction tx = graphDb.beginTx()) {
            graphDb.schema().awaitIndexesOnline(10, TimeUnit.SECONDS);
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
                graphDb.findNodesByLabelAndProperty(NodeType.USER,
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
        Node nUser = findUser(userIdentifier);
        if (nUser != null) {
            // user is already existing
            return nUser;
        }
        return createUser(userIdentifier);
    }

    @Override
    public boolean addUser(String userIdentifier) {
        Node nUser = findUser(userIdentifier);
        if (nUser == null) {
            // user identifier not in use yet
            createUser(userIdentifier);
            return true;
        }
        return false;
    }

    @Override
    public boolean addFollowship(String idFollowing, String idFollowed) {
        Node nFollowing = loadUser(idFollowing);
        Node nFollowed = loadUser(idFollowed);
        return this.addFollowship(nFollowing, nFollowed);
    }

    /**
     * Adds a followship between two user nodes to the social network graph.
     * 
     * @param nFollowing
     *            node of the user that wants to follow another user
     * @param nFollowed
     *            node of the user that will be followed
     * @return true - if the followship was successfully created<br>
     *         false - if this followship is already existing
     */
    abstract protected boolean addFollowship(Node nFollowing, Node nFollowed);

    @Override
    public boolean removeFollowship(String idFollowing, String idFollowed) {
        Node nFollowing = findUser(idFollowing);
        if (nFollowing == null) {
            throw new InvalidFollowingId(idFollowing);
        }
        Node nFollowed = findUser(idFollowed);
        if (nFollowed == null) {
            throw ExceptionFactory.invalidFollowedId(idFollowed);
        }
        return this.removeFollowship(nFollowing, nFollowed);
    }

    /**
     * Removes a followship between two user nodes from the social network
     * graph.
     * 
     * @param nFollowing
     *            node of the user that wants to unfollow a user
     * @param nFollowed
     *            node of the user that will be unfollowed
     * @return true - if the followship was successfully removed<br>
     *         false - if this followship is not existing
     */
    abstract protected boolean
        removeFollowship(Node nFollowing, Node nFollowed);
}
