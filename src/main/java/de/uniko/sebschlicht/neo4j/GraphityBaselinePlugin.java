package de.uniko.sebschlicht.neo4j;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.server.plugins.Parameter;
import org.neo4j.server.plugins.PluginTarget;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.plugins.Source;

import de.uniko.sebschlicht.graphity.exception.IllegalUserIdException;
import de.uniko.sebschlicht.graphity.exception.UnknownFollowedIdException;
import de.uniko.sebschlicht.graphity.exception.UnknownFollowingIdException;
import de.uniko.sebschlicht.graphity.exception.UnknownReaderIdException;
import de.uniko.sebschlicht.neo4j.graphity.WriteOptimizedGraphity;
import de.uniko.sebschlicht.socialnet.StatusUpdateList;
import de.uniko.sebschlicht.socialnet.requests.RequestType;

// TODO documentation
public class GraphityBaselinePlugin extends ServerPlugin {

    private static boolean DEBUG = false;

    private static boolean INITIALIZED = false;

    private static final int BOOTSTRAP_BLOCK_SIZE = 10000;

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
            name = "entries") String[] entries) throws NumberFormatException,
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
            String sIdFeed = String.valueOf(RequestType.FEED.getId());
            String sIdFollow = String.valueOf(RequestType.FOLLOW.getId());
            String sIdPost = String.valueOf(RequestType.POST.getId());
            String sIdUnfollow = String.valueOf(RequestType.UNFOLLOW.getId());
            String sId;
            for (int i = 0; i < entries.length;) {
                sId = entries[i++];
                if (sIdFeed.equals(sId)) {
                    SOCIAL_GRAPH.readStatusUpdates(entries[i++], 15, tx);
                } else if (sIdFollow.equals(sId)) {
                    SOCIAL_GRAPH.addFollowship(entries[i++], entries[i++], tx);
                } else if (sIdPost.equals(sId)) {
                    SOCIAL_GRAPH
                            .addStatusUpdate(entries[i++], entries[i++], tx);
                } else if (sIdUnfollow.equals(sId)) {
                    SOCIAL_GRAPH.removeFollowship(entries[i++], entries[i++],
                            tx);
                } else {
                    throw new IllegalStateException();
                }
                numPendingRequests += 1;
                if (numPendingRequests > BOOTSTRAP_BLOCK_SIZE) {
                    tx.success();
                    tx.close();
                    numPendingRequests = 0;
                    tx = SOCIAL_GRAPH.beginTx();
                }
            }
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
}
