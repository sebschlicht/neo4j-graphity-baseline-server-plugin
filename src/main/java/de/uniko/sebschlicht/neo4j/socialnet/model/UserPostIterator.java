package de.uniko.sebschlicht.neo4j.socialnet.model;

import org.neo4j.graphdb.Node;

import de.uniko.sebschlicht.neo4j.Walker;
import de.uniko.sebschlicht.neo4j.socialnet.EdgeType;

public class UserPostIterator implements PostIterator {

    protected UserProxy pUser;

    protected StatusUpdateProxy pCrrStatusUpdate;

    public UserPostIterator(
            UserProxy pUser) {
        this.pUser = pUser;
        pCrrStatusUpdate = getLastUserPost(pUser);
    }

    protected static StatusUpdateProxy getLastUserPost(UserProxy pUser) {
        Node nLastPost = Walker.nextNode(pUser.getNode(), EdgeType.PUBLISHED);
        if (nLastPost != null) {
            StatusUpdateProxy pStatusUpdate = new StatusUpdateProxy(nLastPost);
            pStatusUpdate.setAuthor(pUser);
            return pStatusUpdate;
        } else {
            return null;
        }
    }

    @Override
    public boolean hasNext() {
        return (pCrrStatusUpdate != null);
    }

    @Override
    public StatusUpdateProxy next() {
        StatusUpdateProxy pOldStatusUpdate = pCrrStatusUpdate;
        if (pOldStatusUpdate != null) {
            Node nNextStatusUpdate =
                    Walker.nextNode(pOldStatusUpdate.getNode(),
                            EdgeType.PUBLISHED);
            if (nNextStatusUpdate != null) {
                pCrrStatusUpdate = new StatusUpdateProxy(nNextStatusUpdate);
                pCrrStatusUpdate.setAuthor(pUser);
            } else {
                pCrrStatusUpdate = null;
            }
        }
        return pOldStatusUpdate;
    }

    @Override
    public void remove() {
        if (hasNext()) {
            next();
        }
    }

    @Override
    public long getCrrPublished() {
        if (hasNext()) {
            return pCrrStatusUpdate.getPublished();
        }
        return 0;
    }
}
