package de.uniko.sebschlicht.socialnet;

public interface SocialNetwork {

    /**
     * Adds a user to the social network.
     * 
     * @param userIdentifier
     *            identifier of the new user
     * @return true - if the user was successfully created<br>
     *         false - if the identifier is already in use
     */
    boolean addUser(String userIdentifier);

    /**
     * Adds a followship to the social network.
     * 
     * @param idFollowing
     *            identifier of the user that wants to follow another user
     * @param idFollowed
     *            identifier of the user that will be followed
     * @return true - if the followship was successfully created<br>
     *         false - if this followship is already existing
     */
    boolean addFollowship(String idFollowing, String idFollowed);
}
