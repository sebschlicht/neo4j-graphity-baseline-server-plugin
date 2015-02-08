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
     * (optional) last recent status update posted by this user
     */
    protected StatusUpdateProxy lastPost;

    protected String identifier;

    /**
     * (optional) timestamp of the last recent status update posted by this user
     */
    protected long _lastPostTimestamp;

    /**
     * Create a user node proxy to provide data access and manipulation.
     * 
     * @param nUser
     *            user node to get and set data
     */
    public UserProxy(
            Node nUser) {
        super(nUser);
        _lastPostTimestamp = -1;
    }

    public String getIdentifier() {
        if (identifier == null) {
            identifier = (String) node.getProperty(PROP_IDENTIFIER);
        }
        return identifier;
    }

    public void setLastPostTimestamp(long lastPostTimestamp) {
        node.setProperty(PROP_LAST_STREAM_UDPATE, lastPostTimestamp);
        _lastPostTimestamp = lastPostTimestamp;
    }

    public long getLastPostTimestamp() {
        if (_lastPostTimestamp == -1) {
            _lastPostTimestamp =
                    (long) node.getProperty(PROP_LAST_STREAM_UDPATE, 0L);
        }
        return _lastPostTimestamp;
    }
}
