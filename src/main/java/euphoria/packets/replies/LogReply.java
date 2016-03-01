package euphoria.packets.replies;

import euphoria.packets.DataPacket;
import euphoria.packets.fields.Message;

import java.util.ArrayList;

public class LogReply extends DataPacket{
  ArrayList<Message> log;
  String             before;
  
  public ArrayList<Message> getLog()    { return log;    }
  public String             getBefore() { return before; }
}
