package euphoria;
import euphoria.WebsocketJSON.*;
public class StandardEventListener implements PacketEventListener{
  String nick;
  String helpText;
  public StandardEventListener(String nick, String helpText){
    this.nick=nick;
    this.helpText=helpText;
  }
  public void onSendEvent(MessageEvent evt) {
    SendEvent data = (SendEvent)evt.getPacket().getData();
    if(evt.getMessage().matches("^!ping(?: @"+nick+")?$")){
      evt.reply("Pong!");
    }
    if(evt.getMessage().matches("^!help @"+nick+"$")){
      evt.reply(helpText);
    }
    if(evt.getMessage().matches("^!kill @"+nick+"$")){
      evt.reply("/me is now exiting.");
      evt.getRoomConnection().closeConnection("Killed by room user.");
    }
    if(evt.getMessage().matches("^!pause @"+nick+"$")){
      evt.reply("/me has been paused.");
      evt.getRoomConnection().pause(nick);
    }
  }
  public void onSnapshotEvent(PacketEvent evt) {
    
  }
  public void onHelloEvent(PacketEvent evt) {
    evt.getRoomConnection().changeNick(nick);
  }
  public void onNickEvent(PacketEvent evt) {
    
  }
  public void onJoinEvent(PacketEvent evt) {
    
  }
  public void onPartEvent(PacketEvent evt) {
    
  }
}
