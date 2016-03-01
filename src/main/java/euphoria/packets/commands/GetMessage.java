package euphoria.packets.commands;

import euphoria.packets.DataPacket;

public class GetMessage extends DataPacket{
  private String id;
  
  public GetMessage(String id) { this.id=id; }
  
  public String getId() { return id; }
}
