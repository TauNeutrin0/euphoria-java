package euphoria.WebsocketJSON;

import euphoria.RoomConnection;

public class PingEvent extends DataPacket {
  private int time;
  private int next;
  
  public PingEvent(){}
  
  public boolean handle(RoomConnection rmCon) {
    rmCon.sendServerMessage(new PingReply(time).createPacket());
    return false;
  }
  
  public int getTime() {
    return time;
  }
  
  public StandardPacket createPingReply() {
    return new PingReply(time).createPacket();
  }
}
