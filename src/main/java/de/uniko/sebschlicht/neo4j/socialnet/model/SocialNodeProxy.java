package de.uniko.sebschlicht.neo4j.socialnet.model;

import org.neo4j.graphdb.Node;

public abstract class SocialNodeProxy {

    protected Node node;

    public SocialNodeProxy(
            Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }
}
