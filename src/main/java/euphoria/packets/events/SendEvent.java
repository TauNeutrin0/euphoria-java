package euphoria.packets.events;

import euphoria.packets.DataPacket;
import euphoria.packets.StandardPacket;
import euphoria.packets.commands.GetMessage;
import euphoria.packets.commands.Send;
import euphoria.packets.fields.SessionView;

public class SendEvent extends DataPacket{
  private String      id;
  private String      parent;
  private String      previous_edit_id;
  private int         time;
  private SessionView sender;
  private String      content;
  private String      encryption_key_id;
  private int         edited;
  private int         deleted;
  private boolean     truncated;
  
  public StandardPacket createReply(String message) {
    return new Send(message,id).createPacket();
  }
  
  public StandardPacket createFullMessageRequest() {
    return new GetMessage(id).createPacket();
  }
  
  public String      getId()              { return id;                };
  public String      getParent()          { return parent;            };
  public String      getPreviousEditId()  { return previous_edit_id;  };
  public int         getTime()            { return time;              };
  public SessionView getSender()          { return sender;            };
  public String      getContent()         { return content;           };
  public String      getEncryptionKeyId() { return encryption_key_id; };
  public int         getEdited()          { return edited;            };
  public int         getDeleted()         { return deleted;           };
  public boolean     getTruncated()       { return truncated;         };
}
