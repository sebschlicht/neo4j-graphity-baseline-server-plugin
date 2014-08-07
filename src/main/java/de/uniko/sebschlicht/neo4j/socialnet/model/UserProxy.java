package de.uniko.sebschlicht.neo4j.socialnet.model;

import org.neo4j.graphdb.Node;

/**
 * node proxy for an user that can act in the social network
 * 
 * @author sebschlicht
 * 
 */
public class UserProxy extends SocialNodeProxy {

    /**
     * unique user identifier
     */
    public static final String PROP_IDENTIFIER = "identifier";

    /**
     * timestamp of last stream update
     */
    public static final String PROP_LAST_STREAM_UDPATE = "stream_update";

    /**
     * user node
     */
    protected Node nUser;

    /**
     * last recent status update posted by this user
     */
    protected StatusUpdateProxy lastPost;

    /**
     * Create a user node proxy to provide data access and manipulation.
     * 
     * @param nUser
     *            user node to get and set data
     */
    public UserProxy(
            Node nUser) {
        super(nUser);
    }
}
