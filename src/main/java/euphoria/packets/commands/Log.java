package euphoria.packets.commands;

import euphoria.packets.DataPacket;

public class Log extends DataPacket{
  private int    n;
  private String before;
  
  public Log(int n, String before) {
    this.n = n;
    this.before = before;
  }
  
  public int getN()         { return n;      }
  public String getBefore() { return before; }
}
