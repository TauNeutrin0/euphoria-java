package euphoria.packets.events;

import java.util.ArrayList;

import euphoria.RoomConnection;
import euphoria.events.ReplyEventListener;
import euphoria.packets.DataPacket;
import euphoria.packets.commands.Auth;

public class BounceEvent extends DataPacket{
  private String reason;
  private ArrayList<String> auth_options;
  private String agent_id;
  private String ip;
  
  public void attemptPasscode(RoomConnection room, String passcode, ReplyEventListener evtLst) {
    room.sendTrackedMessage(new Auth("passcode",passcode).createPacket(),evtLst);
  }
  
  public String            getReason()      { return reason;       }
  public ArrayList<String> getAuthOptions() { return auth_options; }
  public String            getAgentId()     { return agent_id;     }
  public String            getIp()          { return ip;           }
}
