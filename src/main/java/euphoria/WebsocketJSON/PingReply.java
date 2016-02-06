package euphoria.WebsocketJSON;

public class PingReply extends DataPacket{
  private int time;
  
  public PingReply(int t){
    time=t;
  }
}
