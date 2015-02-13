package de.uniko.sebschlicht.neo4j.bootstrap;

import org.neo4j.graphdb.Node;

public class User {

    private Node _node;

    private User[] _subscriptions;

    private long _tsLastPost;

    private Node[] _postNodes;

    public User() {
        _subscriptions = null;
        _tsLastPost = 0;
        _postNodes = null;
    }

    public void setNode(Node node) {
        _node = node;
    }

    public Node getNode() {
        return _node;
    }

    public void setSubscriptions(User[] subscriptions) {
        _subscriptions = subscriptions;
    }

    public User[] getSubscriptions() {
        return _subscriptions;
    }

    public void setLastPostTimestamp(long tsLastPost) {
        _tsLastPost = tsLastPost;
    }

    public long getLastPostTimestamp() {
        return _tsLastPost;
    }

    public void setPostNodes(Node[] postNodes) {
        _postNodes = postNodes;
    }

    public Node[] getPostNodes() {
        return _postNodes;
    }
}
