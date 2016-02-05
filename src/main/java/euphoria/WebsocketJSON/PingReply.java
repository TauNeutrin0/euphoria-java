package euphoria.WebsocketJSON;

public class PingReply extends DataPacket{
  private static final String type = "ping-reply";
  private int time;
  
  public PingReply(int t){
    time=t;
  }
}
