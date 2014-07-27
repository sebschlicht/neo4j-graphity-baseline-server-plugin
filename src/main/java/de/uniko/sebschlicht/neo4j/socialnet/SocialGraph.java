package de.uniko.sebschlicht.neo4j.socialnet;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import de.uniko.sebschlicht.neo4j.socialnet.model.User;
import de.uniko.sebschlicht.socialnet.SocialNetwork;

/**
 * social graph that holds a social network and provides interaction
 * 
 * @author sebschlicht
 * 
 */
public abstract class SocialGraph implements SocialNetwork {

    /**
     * graph database holding the social network graph
     */
    protected GraphDatabaseService graphDb;

    /**
     * Creates a new social network graph instance using the database provided.
     * 
     * @param graphDb
     *            graph database holding the social network graph to operate on
     */
    public SocialGraph(
            GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
    }

    /**
     * Initializes the social graph instance in order to access and manipulate
     * the social network graph.
     */
    public void init() {
        // can be overridden to create/load indices and similar startup actions
    }

    /**
     * Creates a user that can act in the social network.
     * 
     * @param userIdentifier
     *            identifier of the new user
     * @return user node - if the user was successfully created<br>
     *         <b>null</b> - if the identifier is already in use
     */
    public Node createUser(String userIdentifier) {
        Node nUser = this.graphDb.createNode(NodeType.USER);
        nUser.setProperty(User.PROP_IDENTIFIER, userIdentifier);
        return nUser;
    }

    //TODO add methods to map from SocialNetwork-calls to neo4j-based calls
    // e.g.: String userId -> Node nUser
}
