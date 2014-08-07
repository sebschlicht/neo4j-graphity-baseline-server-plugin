package de.uniko.sebschlicht.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.plugins.Parameter;
import org.neo4j.server.plugins.PluginTarget;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.plugins.Source;

import de.uniko.sebschlicht.neo4j.graphity.WriteOptimizedGraphity;
import de.uniko.sebschlicht.neo4j.graphity.exception.UnknownFollowedIdException;
import de.uniko.sebschlicht.neo4j.graphity.exception.UnknownFollowingIdException;

// TODO documentation
public class GraphityBaselinePlugin extends ServerPlugin {

    private static boolean INITIALIZED = false;

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
            name = "followed") String idFollowed) {
        if (SOCIAL_GRAPH == null) {
            init(graphDb);
        }

        return SOCIAL_GRAPH.addFollowship(idFollowing, idFollowed);
    }

    @PluginTarget(GraphDatabaseService.class)
    public boolean unfollow(@Source GraphDatabaseService graphDb, @Parameter(
            name = "following") String idFollowing, @Parameter(
            name = "followed") String idFollowed) {
        if (SOCIAL_GRAPH == null) {
            init(graphDb);
        }

        try {
            return SOCIAL_GRAPH.removeFollowship(idFollowing, idFollowed);
        } catch (UnknownFollowingIdException e) {
            // ignore
        } catch (UnknownFollowedIdException e) {
            // ignore
        }
        return false;
    }

    @PluginTarget(GraphDatabaseService.class)
    public String post(@Source GraphDatabaseService graphDb, @Parameter(
            name = "author") String idAuthor, @Parameter(
            name = "message") String message) {
        if (SOCIAL_GRAPH == null) {
            init(graphDb);
        }

        return SOCIAL_GRAPH.addStatusUpdate(idAuthor, message);
    }

    public int feeds(@Source GraphDatabaseService graphDb, @Parameter(
            name = "reader") String idReader) {
        if (SOCIAL_GRAPH == null) {
            init(graphDb);
        }

        return SOCIAL_GRAPH.readStatusUpdates(idReader, 15).size();
    }
}
