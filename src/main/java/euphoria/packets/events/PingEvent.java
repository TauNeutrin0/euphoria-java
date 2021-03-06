package euphoria.packets.events;

import euphoria.RoomConnection;
import euphoria.packets.DataPacket;
import euphoria.packets.StandardPacket;
import euphoria.packets.replies.PingReply;

public class PingEvent extends DataPacket {
  private int time;
  private int next;
  
  public PingEvent() {

  }
  
  public boolean handle(RoomConnection rmCon) {
    rmCon.sendPacket(new PingReply(time).createPacket());
    return false;
  }
  
  public StandardPacket createPingReply() {
    return new PingReply(time).createPacket();
  }
  
  public int getTime() { return time; }
  public int getNext() { return next; }
}
