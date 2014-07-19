package de.uniko.sebschlicht.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;


public abstract class Graphity {

    protected final GraphDatabaseService graph;

    public Graphity(
            final GraphDatabaseService graph) {
        this.graph = graph;
    }

    public Node createUser(String identifier) {
        Node nUser = this.graph.createNode(SocialItemType.USER);
        nUser.setProperty(Actor.PROP_IDENTIFIER, identifier);
        return nUser;
    }

    protected Node getOrCreateUser(String identifier) {
        try (ResourceIterator<Node> users =
                this.graph.findNodesByLabelAndProperty(SocialItemType.USER,
                        Actor.PROP_IDENTIFIER, identifier).iterator()) {
            if (users.hasNext()) {
                return users.next();
            } else {
                return this.createUser(identifier);
            }
        }
    }

    /**
     * 
     * @return true if the follow edge was created<br>
     *         false if it is already present
     */
    public abstract boolean createFriendship(
            String idFollowing,
            String idFollowed);

    /**
     * 
     * @return true - if the follow edge has been removed<br>
     *         false - if there is no follow edge between the users
     */
    public abstract boolean removeFriendship(
            String idFollowing,
            String idFollowed);

}
