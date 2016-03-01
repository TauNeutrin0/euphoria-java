package euphoria.packets.events;

import euphoria.packets.DataPacket;
import euphoria.packets.fields.*;

public class HelloEvent extends DataPacket{
  private String              id;
  private PersonalAccountView account;
  private SessionView         session;
  private boolean             account_has_access;
  private boolean             account_email_verified;
  private boolean             room_is_private;
  private String              version;
  
  public String              getId()                   { return id;                     }
  public PersonalAccountView getAccount()              { return account;                }
  public SessionView         getSession()              { return session;                }
  public boolean             getAccountHasAccess()     { return account_has_access;     }
  public boolean             getAccountEmailVerified() { return account_email_verified; }
  public boolean             getRoomIsPrivate()        { return room_is_private;        }
  public String              getVersion()              { return version;                }
}
