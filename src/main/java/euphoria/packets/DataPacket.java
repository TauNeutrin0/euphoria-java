package euphoria.packets;

import euphoria.RoomConnection;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class DataPacket {
  private static String type;
  
  public StandardPacket createPacket() {
    StandardPacket p = new StandardPacket(this);
    return p;
  }
  
  public boolean handle(RoomConnection rmCom) {
    return true;
  }
  
  public final String getType() {
    return classToType(this.getClass());
  }
  
  public static Class typeToClass(String type) throws ClassNotFoundException{
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
    String path = "euphoria.packets.";
    if(type.substring(last-2).equals("Event")){
      path+="events.";
    } else if(type.substring(last-2).equals("Reply")) {
      path+="replies.";
    } else {
      path+="commands.";
    }
    return Class.forName(path+type);
  }
  public static String classToType(Class cls) {
    String className = cls.getSimpleName().replaceAll("([a-z])([A-Z])","$1-$2");
    return className.toLowerCase();
  }
}
