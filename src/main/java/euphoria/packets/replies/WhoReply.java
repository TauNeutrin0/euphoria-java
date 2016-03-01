package euphoria.packets.replies;

import euphoria.packets.DataPacket;
import euphoria.packets.fields.SessionView;

import java.util.ArrayList;

public class WhoReply extends DataPacket{
  private ArrayList<SessionView> listing;
  
  public ArrayList<SessionView> getListing() { return listing; }
}
