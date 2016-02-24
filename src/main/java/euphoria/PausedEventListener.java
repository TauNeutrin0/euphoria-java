package euphoria;

import euphoria.MessageEvent;

public class PausedEventListener extends MessageEventListener{
  private RoomConnection room;
  private String nick;
  
  public PausedEventListener(RoomConnection room) {
    this.room=room;
  }
  
  public void setNick(String nick) {
    this.nick=nick;
  }
  
  public void onSendEvent(MessageEvent evt) {
    if(evt.getMessage().matches("^!help @"+nick+"$")){
      evt.reply("/me has been paused.");
      evt.reply("To restore me, type \"!restore @"+nick+"\".");
    }
    if(evt.getMessage().matches("^!restore @"+nick+"$")){
      room.unpause();
      evt.reply("/me has been restored.");
    }
  }
  
}
