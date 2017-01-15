package de.cgrotz.kademlia.protocol;

import com.google.gson.*;
import de.cgrotz.kademlia.config.Listener;
import de.cgrotz.kademlia.config.ListenerType;

import java.lang.reflect.Type;

/**
 * Created by christoph on 15.01.17.
 */
public class ListenerMessageTypeAdapter implements JsonDeserializer<Listener>, JsonSerializer<Listener> {

    private static final String CLASSNAME = "type";

    @Override
    public Listener deserialize(JsonElement json, Type typeOfT,
                               JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject =  json.getAsJsonObject();
        JsonPrimitive prim = (JsonPrimitive) jsonObject.get(CLASSNAME);
        String typeName = prim.getAsString();

        return context.deserialize(jsonObject, ListenerType.valueOf(typeName.toUpperCase()).getListenerConfigClass());
    }

    @Override
    public JsonElement serialize(Listener listener, Type type, JsonSerializationContext jsonSerializationContext) {
        return jsonSerializationContext.serialize(listener);
    }
}
