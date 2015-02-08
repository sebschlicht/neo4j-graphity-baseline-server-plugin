package de.uniko.sebschlicht.neo4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.neo4j.server.rest.repr.ListRepresentation;
import org.neo4j.server.rest.repr.MappingRepresentation;
import org.neo4j.server.rest.repr.MappingSerializer;
import org.neo4j.server.rest.repr.Representation;
import org.neo4j.server.rest.repr.RepresentationType;
import org.neo4j.server.rest.repr.ValueRepresentation;

public class JsonRepresentation extends MappingRepresentation {

    private Map<String, Object> map;

    public JsonRepresentation(
            Map<String, Object> map) {
        super(Representation.MAP);
        this.map = map;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    @Override
    protected void serialize(final MappingSerializer serializer) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            serializeMapEntry(entry.getKey(), entry.getValue(), serializer);
        }
    }

    @SuppressWarnings("unchecked")
    protected void serializeMapEntry(
            String key,
            Object value,
            MappingSerializer serializer) {
        Class<? extends Object> valueClass = value.getClass();
        if (valueClass.equals(String.class)) {
            serializer.putString(key, (String) value);
        } else if (valueClass.equals(boolean.class)
                || valueClass.equals(Boolean.class)) {
            serializer.putBoolean(key, (Boolean) value);
        } else if (Number.class.isAssignableFrom(valueClass)) {
            serializer.putNumber(key, (Number) value);
        } else if (Map.class.isAssignableFrom(valueClass)) {
            serializer.putMapping(key, new JsonRepresentation(
                    (Map<String, Object>) value));
        } else if (valueClass.isArray()) {//TODO safe to delete?
            serializer.putList(key,
                    getJsonArrayRepresentation(Arrays.asList(value)));
        } else if (Iterable.class.isAssignableFrom(valueClass)) {
            serializer.putList(key,
                    getJsonArrayRepresentation((Iterable<?>) value));
        } else {
            throw new IllegalArgumentException("Class \"" + valueClass
                    + "\" can not be serialized to Json.");
        }
    }

    public static ListRepresentation getJsonArrayRepresentation(
            Iterable<?> value) {
        List<Representation> repList = new ArrayList<Representation>();
        Iterable<?> iterable = value;
        for (Object o : iterable) {
            repList.add(getJsonFieldRepresentation(o));
        }
        // I have absolutely no idea what this "type" should be good for.
        return new ListRepresentation(RepresentationType.TEMPLATE, repList);
    }

    @SuppressWarnings("unchecked")
    protected static Representation getJsonFieldRepresentation(Object value) {
        Class<? extends Object> valueClass = value.getClass();
        if (Map.class.isAssignableFrom(valueClass)) {
            return new JsonRepresentation((Map<String, Object>) value);
        } else if (valueClass.equals(String.class)) {
            return ValueRepresentation.string((String) value);
        } else if (valueClass.isArray()) {//TODO safe to delete?
            return getJsonArrayRepresentation(Arrays.asList(value));
        } else if (Iterable.class.isAssignableFrom(valueClass)) {
            return getJsonArrayRepresentation((Iterable<?>) value);
        } else if (valueClass.equals(boolean.class)
                || valueClass.equals(Boolean.class)) {
            return ValueRepresentation.bool((Boolean) value);
        } else if (Number.class.isAssignableFrom(valueClass)) {
            return getNumberRepresentation((Number) value, valueClass);
        }
        throw new IllegalArgumentException("Class \"" + valueClass
                + "\" can not be serialized to Json.");
    }

    public static <T extends Number >Representation getNumberRepresentation(
            T value,
            Class<?> valueClass) {
        if (valueClass.equals(int.class) || valueClass.equals(Integer.class)) {
            return ValueRepresentation.number((Integer) value);
        } else if (valueClass.equals(double.class)
                || valueClass.equals(Double.class)) {
            return ValueRepresentation.number((Double) value);
        } else if (valueClass.equals(long.class)
                || valueClass.equals(Long.class)) {
            return ValueRepresentation.number((Long) value);
        }
        throw new IllegalArgumentException("Class \"" + valueClass
                + "\" can not be serialized to Json.");
    }
}
