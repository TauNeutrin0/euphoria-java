package euphoria.packets.commands;

import euphoria.packets.DataPacket;

public class Nick extends DataPacket{
  private String name;
  
  public Nick(String nick) { name=nick; }
  
  public String getName() { return name; }
}
