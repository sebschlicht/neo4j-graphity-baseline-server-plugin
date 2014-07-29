package de.uniko.sebschlicht.socialnet;

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

    public long getPublished() {
        return published;
    }

    public String getMessage() {
        return message;
    }
}
