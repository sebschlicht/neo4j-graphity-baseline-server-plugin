package de.uniko.sebschlicht.socialnet;

import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;

public class StatusUpdateList {

    protected List<StatusUpdate> statusUpdates;

    public StatusUpdateList() {
        statusUpdates = new LinkedList<StatusUpdate>();
    }

    public void add(StatusUpdate statusUpdate) {
        statusUpdates.add(statusUpdate);
    }

    public int size() {
        return statusUpdates.size();
    }

    @Override
    @SuppressWarnings("unchecked")
    public String toString() {
        JSONArray activities = new JSONArray();
        for (StatusUpdate statusUpdate : statusUpdates) {
            activities.add(statusUpdate.getJsonObject());
        }
        return activities.toJSONString();
    }
}
