package de.uniko.sebschlicht.neo4j.socialnet;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.schema.IndexDefinition;

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

    /**
     * Loads the index definition for a label on a certain property key.
     * 
     * @param label
     *            label the index was created for
     * @param propertyKey
     *            property key the index was created on
     * @return index definition - for the label on the property specified<br>
     *         <b>null</b> - if there is no index for the label on this property
     */
    protected IndexDefinition loadIndexDefinition(
            Label label,
            String propertyKey) {
        try (Transaction tx = this.graphDb.beginTx()) {
            for (IndexDefinition indexDefinition : this.graphDb.schema()
                    .getIndexes(label)) {
                for (String indexPropertyKey : indexDefinition
                        .getPropertyKeys()) {
                    if (indexPropertyKey.equals(propertyKey)) {
                        return indexDefinition;
                    }
                }
            }
        }
        return null;
    }
}
