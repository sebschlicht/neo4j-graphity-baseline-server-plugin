package de.uniko.sebschlicht.neo4j.graphity.exception;

/**
 * An unknown user identifier was passed as following identifier.
 * 
 * @author sebschlicht
 * 
 */
public class UnknownFollowingIdException extends IllegalArgumentException {

    private static final long serialVersionUID = -1228234431314197210L;

    /**
     * following user identifier passed
     */
    private final String idFollowing;

    /**
     * Creates an exception to express that the identifier passed for the
     * following user is unknown.
     * 
     * @param idFollowing
     *            following user identifier passed
     */
    public UnknownFollowingIdException(
            String idFollowing) {
        this.idFollowing = idFollowing;
    }

    /**
     * @return following user identifier passed
     */
    public String getFollowingId() {
        return idFollowing;
    }
}
