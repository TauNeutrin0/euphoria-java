package euphoria.WebsocketJSON;

public abstract class DataPacket {
  private static String type;
  
  public StandardPacket createPacket() {
    StandardPacket p = new StandardPacket(this);
    return p;
  }
  
  public final String getType() {
    return type;
  }
}
