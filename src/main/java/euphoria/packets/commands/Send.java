package euphoria.packets.commands;

import euphoria.packets.DataPacket;

public class Send extends DataPacket{
  private String content;
  private String parent;
  
  public Send(String message, String parent) {
    content = message;
    this.parent = parent;
  }
  public Send(String message) { content = message; }
  
  public String getContent() { return content; }
  public String getParent()  { return parent;  }
}
