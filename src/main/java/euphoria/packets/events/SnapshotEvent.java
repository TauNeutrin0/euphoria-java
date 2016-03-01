package euphoria.packets.events;

import euphoria.packets.DataPacket;
import euphoria.packets.fields.Message;
import euphoria.packets.fields.SessionView;

import java.util.ArrayList;

public class SnapshotEvent extends DataPacket{
  private String identity;
  private String session_id;
  private String version;
  private ArrayList<SessionView> listing;
  private ArrayList<Message> log;
  private String nick;
  private String pm_with_nick;
  private String pm_with_user_id;
  
  public String                 getIdentity()     { return identity;        }
  public String                 getSessionId()    { return session_id;      }
  public String                 getVersion()      { return version;         }
  public ArrayList<SessionView> getListing()      { return listing;         }
  public ArrayList<Message>     getLog()          { return log;             }
  public String                 getNick()         { return nick;            }
  public String                 getPmWithNick()   { return pm_with_nick;    }
  public String                 getPmWithUserId() { return pm_with_user_id; }
}
