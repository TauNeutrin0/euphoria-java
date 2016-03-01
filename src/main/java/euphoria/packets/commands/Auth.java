package euphoria.packets.commands;

import euphoria.packets.DataPacket;

public class Auth extends DataPacket{
  private String type;
  private String passcode;
  
  public Auth(String type, String passcode) {
    this.type=type;
    this.passcode=passcode;
  }
}
