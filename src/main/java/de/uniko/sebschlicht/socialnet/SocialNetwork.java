package de.uniko.sebschlicht.socialnet;


/**
 * operations to access and manipulate a social network using REST API
 * 
 * @author sebschlicht
 * 
 */
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

    //TODO Uses lazy approach: Ensure that the identifiers are valid and
    // belong to an existing user if there is any mapping to external services.
    // This can not be done wo help of external service like an user database.
    // Therefore this task belongs to you.
    /**
     * Adds a followship between two users to the social network.
     * 
     * @param idFollowing
     *            identifier of the user that wants to follow another user
     * @param idFollowed
     *            identifier of the user that will be followed
     * @return true - if the followship was successfully created<br>
     *         false - if this followship is already existing
     */
    boolean addFollowship(String idFollowing, String idFollowed);

    /**
     * Removes a followship between two users from the social network.
     * 
     * @param idFollowing
     *            identifier of the user that wants to unfollow an user
     * @param idFollowed
     *            identifier of the user that will be unfollowed
     * @return true - if the followship was successfully removed<br>
     *         false - if this followship is not existing
     */
    boolean removeFollowship(String idFollowing, String idFollowed);

    //TODO Make content generic to enable the usage of more complex post types.
    //TODO Introduce wall concept so users can post to other walls than their own
    /**
     * Adds a status update to the social network in order to appear in user
     * streams.
     * 
     * @param idAuthor
     *            identifier of the status update author<br>
     *            at the moment you can only post status updates to your own
     *            wall
     * @param message
     *            message to be displayed
     * @return identifier of the status update
     */
    String addStatusUpdate(String idAuthor, String message);

    /**
     * Reads a number of status updates posted in a user's social network.
     * 
     * @param idReader
     *            identifier of the reader
     * @param numStatusUpdates
     *            number of status updates to get
     * @return list containing up to <i>numStatusUpdates</i> status updates<br>
     */
    StatusUpdateList readStatusUpdates(String idReader, int numStatusUpdates);
}
