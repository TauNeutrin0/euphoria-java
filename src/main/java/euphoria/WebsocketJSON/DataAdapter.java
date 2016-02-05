package euphoria.WebsocketJSON;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /*@Override
    public JsonElement serialize(DataPacket src, Type typeOfSrc, JsonSerializationContext context) throws JsonParseException {
        JsonObject result = new JsonObject();
        String className = src.getClass().getSimpleName().replaceAll("([a-z])([A-Z])","$1-$2");
        result.add("type", new JsonPrimitive(className.toLowerCase()));
        result.add("data", context.serialize(src, src.getClass()));
 
        return result;
    }

    @Override
    public DataPacket deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
      JsonObject jsonObject = json.getAsJsonObject();
      String type = jsonObject.get("type").getAsString();
      Matcher m = Pattern.compile("(?<=-)[a-z]").matcher(type);

      StringBuilder sb = new StringBuilder();
      int last = 0;
      while (m.find()) {
        sb.append(type.substring(last, m.start()));
        sb.append(m.group(0).toUpperCase());
        last = m.end();
      }
      sb.append(type.substring(last));
      type=sb.toString().replaceAll("(-)","");
      JsonElement element = jsonObject.get("data");

      try {
          return context.deserialize(element, Class.forName(type));
      } catch (ClassNotFoundException cnfe) {
          throw new JsonParseException("Unknown element type: " + type, cnfe);
      }
    }*/
  @Override
    public JsonElement serialize(StandardPacket src, Type typeOfSrc, JsonSerializationContext context) throws JsonParseException {
        JsonObject result = new JsonObject();
        String className = src.getData().getClass().getSimpleName().replaceAll("([a-z])([A-Z])","$1-$2");
        result.add("type", new JsonPrimitive(className.toLowerCase()));
        result.add("data", context.serialize(src.getData(), src.getData().getClass()));
 
        return result;
    }
  @Override
    public StandardPacket deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
      JsonObject jsonObject = json.getAsJsonObject();
      String type = jsonObject.get("type").getAsString();
      Matcher m = Pattern.compile("(?<=-)[a-z]").matcher(type);

      StringBuilder sb = new StringBuilder();
      int last = 0;
      while (m.find()) {
        sb.append(type.substring(last, m.start()));
        sb.append(m.group(0).toUpperCase());
        last = m.end();
      }
      sb.append(type.substring(last));
      type=sb.toString().replaceAll("(-)","");
      type = type.substring(0, 1).toUpperCase() + type.substring(1);
      JsonElement element = jsonObject.get("data");
      jsonObject.remove("data");
      Gson gson = new Gson();
      StandardPacket packet = gson.fromJson(jsonObject,StandardPacket.class);
      try {
        packet.setData((DataPacket)context.deserialize(element,Class.forName("euphoria.WebsocketJSON."+type)));
      } catch (ClassNotFoundException e) {
          throw new JsonParseException("Unknown element type: " + type, e);
      }
      
      return packet;
    }
}
