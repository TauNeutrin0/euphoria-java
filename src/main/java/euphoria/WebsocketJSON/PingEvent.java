package euphoria.WebsocketJSON;

public class PingEvent extends DataPacket {
  private static final String type = "ping-event";
  private int time;
  private int next;
  
  public PingEvent(){}
  
  public int getTime() {
    return time;
  }
  
  public StandardPacket createPingReply() {
    PingReply reply = new PingReply(time);
    return reply.createPacket();
  }
}
