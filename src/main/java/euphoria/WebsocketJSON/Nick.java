package euphoria.WebsocketJSON;

public class Nick extends DataPacket{
  String name;
  
  public Nick(String nick) {
    name=nick;
  }
}
