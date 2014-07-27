package de.uniko.sebschlicht.neo4j.graphity;

// TODO documentation
public class StatusUpdate {

    protected long published;

    protected String message;

    public StatusUpdate(
            long published,
            String message) {
        this.published = published;
        this.message = message;
    }
}
