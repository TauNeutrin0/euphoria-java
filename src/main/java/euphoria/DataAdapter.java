package euphoria;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import euphoria.packets.DataPacket;
import euphoria.packets.StandardPacket;


public class DataAdapter implements JsonDeserializer<StandardPacket>{
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
