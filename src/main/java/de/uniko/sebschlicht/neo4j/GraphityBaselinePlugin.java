package de.uniko.sebschlicht.neo4j;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.plugins.Parameter;
import org.neo4j.server.plugins.PluginTarget;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.plugins.Source;

import de.uniko.sebschlicht.graphity.exception.IllegalUserIdException;
import de.uniko.sebschlicht.graphity.exception.UnknownFollowedIdException;
import de.uniko.sebschlicht.graphity.exception.UnknownFollowingIdException;
import de.uniko.sebschlicht.graphity.neo4j.Neo4jGraphity;
import de.uniko.sebschlicht.graphity.neo4j.impl.WriteOptimizedGraphity;
import de.uniko.sebschlicht.socialnet.StatusUpdateList;

// TODO documentation
public class GraphityBaselinePlugin extends ServerPlugin {

    private static boolean DEBUG = false;

    private static boolean INITIALIZED = false;

    private static Neo4jGraphity SOCIAL_GRAPH = null;

    private static synchronized void init(GraphDatabaseService graphDb) {
        if (!INITIALIZED) {
            INITIALIZED = true;
            SOCIAL_GRAPH = new WriteOptimizedGraphity(graphDb);
            SOCIAL_GRAPH.init();
        }
    }

    @PluginTarget(GraphDatabaseService.class)
    public boolean user(@Source GraphDatabaseService graphDb, @Parameter(
            name = "id") long id) throws IllegalUserIdException {
        if (DEBUG) {
            return true;
        }
        if (SOCIAL_GRAPH == null) {
            init(graphDb);
        }
        return SOCIAL_GRAPH.addUser(String.valueOf(id));
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
                    name = "reader") String idReader) {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
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
        } catch (Exception e) {
            result.clear();
            result.put("error", e.getMessage());
            e.printStackTrace();
            return new JsonRepresentation(result);
        }
    }
}
