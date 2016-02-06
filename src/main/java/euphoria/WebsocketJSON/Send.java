package euphoria.WebsocketJSON;

public class Send extends DataPacket{
  String content;
  String parent;
  
  public Send(String message, String prnt) {
    content = message;
    parent = prnt;
  }
  public Send(String message) {
    content = message;
  }
  
}
