package de.uniko.sebschlicht.neo4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.server.plugins.Parameter;
import org.neo4j.server.plugins.PluginTarget;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.plugins.Source;

import de.uniko.sebschlicht.neo4j.graphity.WriteOptimizedGraphity;

// TODO documentation
public class GraphityBaselinePlugin extends ServerPlugin {

    private static WriteOptimizedGraphity socialGraph;

    @PluginTarget(GraphDatabaseService.class)
    public boolean addUser(@Source GraphDatabaseService graphDb, @Parameter(
            name = "identifier") String identifier) {
        if (socialGraph == null) {
            socialGraph = new WriteOptimizedGraphity(graphDb);
        }

        try (Transaction tx = graphDb.beginTx()) {
            if (socialGraph.addUser(identifier)) {
                tx.success();
                return true;
            }
        }
        return false;
    }

    @PluginTarget(GraphDatabaseService.class)
    public boolean follow(@Source GraphDatabaseService graphDb, @Parameter(
            name = "following") String idFollowing, @Parameter(
            name = "followed") String idFollowed) {
        if (socialGraph == null) {
            socialGraph = new WriteOptimizedGraphity(graphDb);
        }

        try (Transaction tx = graphDb.beginTx()) {
            if (socialGraph.addFollowship(idFollowing, idFollowed)) {
                tx.success();
                return true;
            }
        }
        return false;
    }

    @PluginTarget(GraphDatabaseService.class)
    public boolean post(@Source GraphDatabaseService graphDb, @Parameter(
            name = "author") String idAuthor, @Parameter(
            name = "message") String message) {
        return false;
    }
}
