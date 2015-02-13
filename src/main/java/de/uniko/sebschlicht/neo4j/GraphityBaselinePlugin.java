package de.uniko.sebschlicht.neo4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.server.plugins.Parameter;
import org.neo4j.server.plugins.PluginTarget;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.plugins.Source;

import de.uniko.sebschlicht.graphity.exception.IllegalUserIdException;
import de.uniko.sebschlicht.graphity.exception.UnknownFollowedIdException;
import de.uniko.sebschlicht.graphity.exception.UnknownFollowingIdException;
import de.uniko.sebschlicht.graphity.exception.UnknownReaderIdException;
import de.uniko.sebschlicht.neo4j.bootstrap.User;
import de.uniko.sebschlicht.neo4j.bootstrap.UserManager;
import de.uniko.sebschlicht.neo4j.graphity.WriteOptimizedGraphity;
import de.uniko.sebschlicht.neo4j.socialnet.NodeType;
import de.uniko.sebschlicht.neo4j.socialnet.model.StatusUpdateProxy;
import de.uniko.sebschlicht.neo4j.socialnet.model.UserProxy;
import de.uniko.sebschlicht.socialnet.StatusUpdateList;

// TODO documentation
public class GraphityBaselinePlugin extends ServerPlugin {

    private static boolean DEBUG = false;

    private static boolean INITIALIZED = false;

    private static final int BOOTSTRAP_BLOCK_SIZE = 10000;

    protected static final Random RANDOM = new Random();

    protected static final char[] POST_SYMBOLS;
    static {
        StringBuilder postSymbols = new StringBuilder();
        // numbers
        for (char number = '0'; number <= '9'; ++number) {
            postSymbols.append(number);
        }
        // lower case letters
        for (char letter = 'a'; letter <= 'z'; ++letter) {
            postSymbols.append(letter);
        }
        // upper case letters
        for (char letter = 'a'; letter <= 'z'; ++letter) {
            postSymbols.append(Character.toUpperCase(letter));
        }
        POST_SYMBOLS = postSymbols.toString().toCharArray();
    }

    private static WriteOptimizedGraphity SOCIAL_GRAPH;

    private static synchronized void init(GraphDatabaseService graphDb) {
        if (!INITIALIZED) {
            INITIALIZED = true;
            SOCIAL_GRAPH = new WriteOptimizedGraphity(graphDb);
            SOCIAL_GRAPH.init();
        }
    }

    @PluginTarget(GraphDatabaseService.class)
    public boolean follow(@Source GraphDatabaseService graphDb, @Parameter(
            name = "following") String idFollowing, @Parameter(
            name = "followed") String idFollowed) throws IllegalUserIdException {
        if (DEBUG) {
            return true;
        }
        if (SOCIAL_GRAPH == null) {
            init(graphDb);
        }
        return SOCIAL_GRAPH.addFollowship(idFollowing, idFollowed);
    }

    @PluginTarget(GraphDatabaseService.class)
    public boolean unfollow(@Source GraphDatabaseService graphDb, @Parameter(
            name = "following") String idFollowing, @Parameter(
            name = "followed") String idFollowed)
            throws UnknownFollowingIdException, UnknownFollowedIdException {
        if (DEBUG) {
            return true;
        }
        if (SOCIAL_GRAPH == null) {
            init(graphDb);
        }
        return SOCIAL_GRAPH.removeFollowship(idFollowing, idFollowed);
    }

    @PluginTarget(GraphDatabaseService.class)
    public long post(@Source GraphDatabaseService graphDb, @Parameter(
            name = "author") String idAuthor, @Parameter(
            name = "message") String message) throws IllegalUserIdException {
        if (DEBUG) {
            return System.currentTimeMillis();
        }
        if (SOCIAL_GRAPH == null) {
            init(graphDb);
        }
        return SOCIAL_GRAPH.addStatusUpdate(idAuthor, message);
    }

    @PluginTarget(GraphDatabaseService.class)
    public JsonRepresentation feeds(
            @Source GraphDatabaseService graphDb,
            @Parameter(
                    name = "reader") String idReader)
            throws UnknownReaderIdException {
        Map<String, Object> result = new HashMap<String, Object>();
        if (DEBUG) {
            result.put("feeds", new StatusUpdateList());
            return new JsonRepresentation(result);
        }
        if (SOCIAL_GRAPH == null) {
            init(graphDb);
        }
        result.put("feeds", SOCIAL_GRAPH.readStatusUpdates(idReader, 15)
                .buildJsonArray());
        return new JsonRepresentation(result);
    }

    @PluginTarget(GraphDatabaseService.class)
    public String bootstrap(@Source GraphDatabaseService graphDb, @Parameter(
            name = "userIds") long[] aUserIds, @Parameter(
            name = "subscriptions") String[] aSubscriptions, @Parameter(
            name = "numPosts") int[] aNumPosts) throws NumberFormatException,
            UnknownReaderIdException, IllegalUserIdException,
            UnknownFollowingIdException, UnknownFollowedIdException {
        if (DEBUG) {
            return "true";
        }
        try {
            if (SOCIAL_GRAPH == null) {
                init(graphDb);
            }
            Transaction tx = SOCIAL_GRAPH.beginTx();
            int numPendingRequests = 0;

            /**
             * expected format:
             * {
             * "userIds": [1,2,3,4,5],// only users we need
             * "subscriptions": ["", "1,3,4", "1,4", "1,3", "4"],// even if none
             * "posts": [0,6,5,18,4]// even if none
             * }
             */
            int postLength = 140;
            UserManager users = new UserManager();
            users.setUserIds(aUserIds);
            User user;

            // load subscriptions
            long idFollowed;
            for (int i = 0; i < aSubscriptions.length; ++i) {
                user = users.loadUserByIndex(i);// will add user
                String[] aUserSubscriptions = aSubscriptions[i].split(",");
                if (aUserSubscriptions.length == 0) {
                    continue;
                }
                User[] subscriptions = new User[aUserSubscriptions.length];
                for (int iFollowed = 0; iFollowed < aUserSubscriptions.length; ++iFollowed) {
                    idFollowed = Long.valueOf(aUserSubscriptions[iFollowed]);
                    subscriptions[iFollowed] = users.loadUser(idFollowed);// can add user
                }
                user.setSubscriptions(subscriptions);
            }

            // add vertices to graph
            // add posts
            int numUserPosts;
            Node nPost;
            long tsLastPost = 0;
            StatusUpdateProxy pStatusUpdate;
            for (int i = 0; i < aNumPosts.length; ++i) {
                user = users.loadUserByIndex(i);// can add user
                numUserPosts = aNumPosts[i];
                if (numUserPosts == 0) {
                    continue;
                }
                Node[] userPostNodes = new Node[numUserPosts];
                for (int iPost = 0; iPost < numUserPosts; ++iPost) {
                    nPost = graphDb.createNode(NodeType.UPDATE);
                    pStatusUpdate = new StatusUpdateProxy(nPost);
                    tsLastPost = System.currentTimeMillis();
                    pStatusUpdate.initNode(tsLastPost,
                            generatePostMessage(postLength));
                    userPostNodes[iPost] = nPost;

                    if (++numPendingRequests >= BOOTSTRAP_BLOCK_SIZE) {
                        tx.success();
                        tx.close();
                        tx = SOCIAL_GRAPH.beginTx();
                    }
                }
                user.setPostNodes(userPostNodes);
                user.setLastPostTimestamp(tsLastPost);
            }
            // add users
            Node nUser;
            for (long id : aUserIds) {
                user = users.loadUser(id);// can not add user
                nUser = graphDb.createNode(NodeType.USER);
                user.setNode(nUser);

                if (++numPendingRequests >= BOOTSTRAP_BLOCK_SIZE) {
                    tx.success();
                    tx.close();
                    tx = SOCIAL_GRAPH.beginTx();
                }
            }

            // add edges to graph
            UserProxy pAuthor;
            // add post edges
            for (long id : aUserIds) {
                user = users.loadUser(id);// can not add user
                if (user.getPostNodes() == null) {
                    continue;
                }
                pAuthor = new UserProxy(user.getNode());
                for (Node postNode : user.getPostNodes()) {
                    pStatusUpdate = new StatusUpdateProxy(postNode);
                    pAuthor.linkStatusUpdate(pStatusUpdate);

                    if (++numPendingRequests >= BOOTSTRAP_BLOCK_SIZE) {
                        tx.success();
                        tx.close();
                        tx = SOCIAL_GRAPH.beginTx();
                    }
                }
                pAuthor.setLastPostTimestamp(user.getLastPostTimestamp());
                numPendingRequests += 1;
            }
            // add subscription edges
            for (long id : aUserIds) {
                user = users.loadUser(id);// can not add user
                if (user.getSubscriptions() == null) {
                    continue;
                }
                if (SOCIAL_GRAPH instanceof WriteOptimizedGraphity) {
                    for (User followed : user.getSubscriptions()) {
                        SOCIAL_GRAPH.doAddFollowship(user.getNode(),
                                followed.getNode());

                        if (++numPendingRequests >= BOOTSTRAP_BLOCK_SIZE) {
                            tx.success();
                            tx.close();
                            tx = SOCIAL_GRAPH.beginTx();
                        }
                    }
                }
            }

            // TODO build Graphity index if using ReadOptimizedGraphity

            if (numPendingRequests > 0) {
                tx.success();
                tx.close();
            }
            return "true";
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return sw.toString();
        }
    }

    protected static String generatePostMessage(int length) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; ++i) {
            builder.append(getRandomPostChar());
        }
        return builder.toString();
    }

    protected static char getRandomPostChar() {
        return POST_SYMBOLS[RANDOM.nextInt(POST_SYMBOLS.length)];
    }
}
