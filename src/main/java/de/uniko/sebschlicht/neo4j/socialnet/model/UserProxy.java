package de.uniko.sebschlicht.neo4j.socialnet.model;

import org.neo4j.graphdb.Node;

/**
 * user that can act in the social network
 * 
 * @author sebschlicht
 * 
 */
public class UserProxy {

    /**
     * unique user identifier
     */
    public static final String PROP_IDENTIFIER = "identifier";

    /**
     * timestamp of last stream update
     */
    public static final String PROP_LAST_STREAM_UDPATE = "stream_update";

    protected Node nAuthor;

    public UserProxy(
            Node nAuthor) {
        this.nAuthor = nAuthor;
    }
}
