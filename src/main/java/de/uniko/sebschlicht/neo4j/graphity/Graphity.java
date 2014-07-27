package de.uniko.sebschlicht.neo4j.graphity;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import de.uniko.sebschlicht.neo4j.socialnet.SocialGraph;

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

    protected Node loadUser(String userIdentifier) {
        //TODO try to load user from index or create a new one lazily
        return null;
    }

    @Override
    public boolean addUser(String userIdentifier) {
        Node nUser = this.createUser(userIdentifier);
        return (nUser != null);
    }
}
