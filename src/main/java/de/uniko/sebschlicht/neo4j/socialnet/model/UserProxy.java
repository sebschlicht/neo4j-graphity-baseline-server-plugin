package de.uniko.sebschlicht.neo4j.socialnet.model;

import org.neo4j.graphdb.Node;

import de.uniko.sebschlicht.neo4j.Walker;
import de.uniko.sebschlicht.neo4j.socialnet.EdgeType;

/**
 * node proxy for an user that can act in the social network
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

    /**
     * user node
     */
    protected Node nUser;

    /**
     * last recent status update posted by this user
     */
    protected StatusUpdateProxy statusUpdate;

    /**
     * Create a user node proxy to provide data access and manipulation.
     * 
     * @param nUser
     *            user node to get and set data
     */
    public UserProxy(
            Node nUser) {
        this.nUser = nUser;
    }

    /**
     * Check if this user posted any status updates.
     * 
     * @return true - this user posted one status update at least<br>
     *         false - this user did not post any status updates yet
     */
    public boolean hasStatusUpdate() {
        Node nStatusUpdate = Walker.nextNode(nUser, EdgeType.PUBLISHED);
        if (nStatusUpdate != null) {
            statusUpdate = new StatusUpdateProxy(nStatusUpdate);
            return true;
        } else {
            statusUpdate = null;
            return false;
        }
    }

    /**
     * @return last recent status update posted by this user<br>
     *         or <b>null</b> if no status update posted or missed to call
     *         <i>hasStatusUpdate</i> previously
     */
    public StatusUpdateProxy getStatusUpdate() {
        return statusUpdate;
    }
}
