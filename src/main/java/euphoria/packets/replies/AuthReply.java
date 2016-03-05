package euphoria.packets.replies;

public class AuthReply {
  private boolean success;
  private String  reason;
  
  public boolean getSuccess() { return success; }
  public String  getReason()  { return reason;  }
}
