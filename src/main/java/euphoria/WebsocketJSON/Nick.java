package euphoria.WebsocketJSON;

public class Nick extends DataPacket{
  private String name;
  
  public Nick(String nick) {
    name=nick;
  }
}
