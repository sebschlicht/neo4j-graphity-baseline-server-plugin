package de.uniko.sebschlicht.neo4j.socialnet.model;

import org.neo4j.graphdb.Node;

import de.metalcon.domain.Muid;
import de.metalcon.domain.UidType;
import de.metalcon.exceptions.ServiceOverloadedException;

/**
 * status update displayed in news feeds
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

    protected long identifier;

    protected Node nStatusUpdate;

    protected UserProxy pAuthor;

    public StatusUpdateProxy(
            Node nStatusUpdate) {
        this.nStatusUpdate = nStatusUpdate;
    }

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
