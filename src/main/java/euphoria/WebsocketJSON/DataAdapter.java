package euphoria.WebsocketJSON;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import euphoria.WebsocketJSON.DataPacket;

public class DataAdapter implements JsonSerializer<StandardPacket>, JsonDeserializer<StandardPacket>{
  @Override
    public JsonElement serialize(StandardPacket src, Type typeOfSrc, JsonSerializationContext context) throws JsonParseException {
        JsonObject result = new JsonObject();
        result.add("type", new JsonPrimitive(DataPacket.classToType(src.getData().getClass())));
        result.add("data", context.serialize(src.getData(), src.getData().getClass()));
        return result;
    }
  @Override
    public StandardPacket deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
      JsonObject jsonObject = json.getAsJsonObject();
      Class classType = null;
      try {
        classType = DataPacket.typeToClass(jsonObject.get("type").getAsString());
      } catch (ClassNotFoundException e1) {
          throw new JsonParseException(e1);
      }
      JsonElement element = jsonObject.get("data");
      jsonObject.remove("data");
      Gson gson = new Gson();
      StandardPacket packet = gson.fromJson(jsonObject,StandardPacket.class);
      packet.setData((DataPacket)context.deserialize(element,classType));
      return packet;
    }
}
