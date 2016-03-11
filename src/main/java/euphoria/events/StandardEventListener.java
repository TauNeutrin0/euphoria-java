package euphoria.events;

import euphoria.events.PacketEvent;

public class StandardEventListener implements PacketEventListener{
  String nick;
  String helpText;
  public StandardEventListener(String nick, String helpText){
    this.nick=nick;
    this.helpText=helpText;
  }
  @Override
  public void onSendEvent(MessageEvent evt) {
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
  @Override
  public void onSnapshotEvent(PacketEvent evt) {
    evt.getRoomConnection().changeNick(nick);
  }
  public void onHelloEvent(PacketEvent evt) {}
  @Override
  public void onNickEvent(PacketEvent evt) {}
  @Override
  public void onJoinEvent(PacketEvent evt) {}
  @Override
  public void onPartEvent(PacketEvent evt) {}
  @Override
  public void onBounceEvent(PacketEvent arg0) {}
}
