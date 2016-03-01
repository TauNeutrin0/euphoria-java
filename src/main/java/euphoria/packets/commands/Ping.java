package euphoria.packets.commands;

import euphoria.packets.DataPacket;

public class Ping extends DataPacket{
  private int time;
  
  public Ping(int time) { this.time=time; }
  
  public int getTime() { return time; }
}
