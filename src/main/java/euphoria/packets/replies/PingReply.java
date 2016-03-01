package euphoria.packets.replies;

import euphoria.packets.DataPacket;

public class PingReply extends DataPacket{
  private int time;
  
  public PingReply(int t) { time=t; }
  
  public int getTime() { return time; }
}
