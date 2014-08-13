package de.uniko.sebschlicht.socialnet;

import org.json.simple.JSONObject;

// TODO documentation
public class StatusUpdate {

    //TODO needs author

    protected JSONObject jsonObject;

    protected String author;

    protected long published;

    protected String message;

    public StatusUpdate(
            String author,
            long published,
            String message) {
        this.author = author;
        this.published = published;
        this.message = message;
    }

    public String getAuthor() {
        return author;
    }

    public long getPublished() {
        return published;
    }

    public String getMessage() {
        return message;
    }

    public JSONObject getJsonObject() {
        if (jsonObject == null) {
            jsonObject = generateJsonObject(this);
        }
        return jsonObject;
    }

    private static JSONObject generateJsonObject(StatusUpdate statusUpdate) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("author", statusUpdate.getAuthor());
        jsonObject.put("published", statusUpdate.getPublished());
        jsonObject.put("message", statusUpdate.getMessage());
        return jsonObject;
    }
}
