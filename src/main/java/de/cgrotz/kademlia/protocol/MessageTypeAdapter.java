package de.cgrotz.kademlia.protocol;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by christoph on 15.01.17.

 */
public class MessageTypeAdapter implements JsonDeserializer<Message> {

        private static final String CLASSNAME = "type";;


        @Override
        public Message deserialize(JsonElement json, Type typeOfT,
                                   JsonDeserializationContext context) throws JsonParseException  {
            JsonObject jsonObject =  json.getAsJsonObject();
            JsonPrimitive prim = (JsonPrimitive) jsonObject.get(CLASSNAME);
            String typeName = prim.getAsString();

            return context.deserialize(jsonObject, MessageType.valueOf(typeName.toUpperCase()).getMsgClass());
        }
    }