package euphoria.packets.commands;

import euphoria.packets.DataPacket;
import euphoria.packets.StandardPacket;

public class Auth extends DataPacket{
  private String type;
  private String passcode;
  
  public Auth(String type, String passcode) {
    this.type=type;
    this.passcode=passcode;
  }
  
  public static StandardPacket createPasswordAttempt(String password) {
    return new Auth("passcode",password).createPacket();
  }
  
  public String getAuthType() { return type;     }
  public String getPasscode() { return passcode; }
}
