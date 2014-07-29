package de.uniko.sebschlicht.neo4j.socialnet.model;

import org.neo4j.graphdb.Node;

import de.metalcon.domain.Muid;
import de.metalcon.domain.UidType;
import de.metalcon.exceptions.ServiceOverloadedException;

/**
 * node proxy for a status update displayed in news feeds
 * 
 * @author sebschlicht
 * 
 */
public class StatusUpdateProxy {

    /**
     * permanent universally unique identifier
     */
    public static final String PROP_IDENTIFIER = "identifier";

    /**
     * date and time the activity was published
     */
    public static final String PROP_PUBLISHED = "published";

    /**
     * content message
     */
    public static final String PROP_MESSAGE = "message";

    /**
     * status update identifier
     */
    protected long identifier;

    /**
     * status update node
     */
    protected Node nStatusUpdate;

    /**
     * author (proxy to user node)
     */
    protected UserProxy pAuthor;

    /**
     * Create a status update node to provide data access and manipulation.
     * 
     * @param nStatusUpdate
     *            status update node to get and set data
     */
    public StatusUpdateProxy(
            Node nStatusUpdate) {
        this.nStatusUpdate = nStatusUpdate;
    }

    /**
     * initialize the status update node<br>
     * necessary if the proxy is for a new status update
     * 
     * @return true - if the status update was successfully created<br>
     *         false - if status update creation failed due to service overload
     */
    public boolean init() {
        try {
            identifier = Muid.create(UidType.DISC).getValue();
            nStatusUpdate.setProperty(PROP_IDENTIFIER, identifier);
            return true;
        } catch (ServiceOverloadedException e) {
            return false;
        }
    }

    public void setAuthor(UserProxy pAuthor) {
        this.pAuthor = pAuthor;
    }

    /**
     * @return status update identifier
     */
    public long getIdentifier() {
        return identifier;
    }

    public void setMessage(String message) {
        nStatusUpdate.setProperty(PROP_MESSAGE, message);
    }

    public void setPublished(long published) {
        nStatusUpdate.setProperty(PROP_PUBLISHED, published);
    }
}
