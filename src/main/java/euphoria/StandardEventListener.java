package euphoria;
import euphoria.WebsocketJSON.*;
public class StandardEventListener implements PacketEventListener{
  String nick;
  String helpText;
  public StandardEventListener(String nick, String helpText){
    this.nick=nick;
    this.helpText=helpText;
  }
  public void SendEvent(PacketEvent evt) {
    SendEvent data = (SendEvent)evt.getPacket().getData();
    if(data.getMessage().matches("^!ping(?: @"+nick+")?$")){
      evt.getRoomConnection().sendServerMessage(data.createReply("Pong!"));
    }
    if(data.getMessage().matches("^!help @"+nick+"$")){
      evt.getRoomConnection().sendServerMessage(data.createReply(helpText));
    }
    if(data.getMessage().matches("^!kill @"+nick+"$")){
      evt.getRoomConnection().closeConnection("Killed by room user.");
    }
  }
  public void SnapshotEvent(PacketEvent evt) {
    
  }
  public void HelloEvent(PacketEvent evt) {
    evt.getRoomConnection().changeNick(nick);
  }
  public void NickEvent(PacketEvent evt) {
    
  }
  public void JoinEvent(PacketEvent evt) {
    
  }
  public void PartEvent(PacketEvent evt) {
    
  }
}
