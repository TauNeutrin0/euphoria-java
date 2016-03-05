package euphoria.packets.replies;

import euphoria.packets.DataPacket;

public class AuthReply extends DataPacket {
  private boolean success;
  private String  reason;
  
  public boolean getSuccess() { return success; }
  public String  getReason()  { return reason;  }
}
