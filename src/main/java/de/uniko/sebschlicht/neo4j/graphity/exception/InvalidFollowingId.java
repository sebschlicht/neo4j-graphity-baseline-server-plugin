package de.uniko.sebschlicht.neo4j.graphity.exception;

// TODO documentation
public class InvalidFollowingId extends IllegalArgumentException {

    private static final long serialVersionUID = -1228234431314197210L;

    private final String idFollowing;

    public InvalidFollowingId(
            String idFollowing) {
        this.idFollowing = idFollowing;
    }

    public String getFollowingId() {
        return idFollowing;
    }
}
