package euphoria.WebsocketJSON;

public class Send extends DataPacket{
  private String content;
  private String parent;
  
  public Send(String message, String prnt) {
    content = message;
    parent = prnt;
  }
  public Send(String message) {
    content = message;
  }
  
}
