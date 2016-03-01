package euphoria.packets.events;

import euphoria.packets.DataPacket;

public class NickEvent extends DataPacket{
  private String session_id;
  private String id;
  private String from;
  private String to;
  
  public String getSessionId() { return session_id; }
  public String getId()        { return id;         }
  public String getFrom()      { return from;       }
  public String getTo()        { return to;         }
}
