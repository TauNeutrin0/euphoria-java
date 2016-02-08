
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import euphoria.*;
import euphoria.ConnectionEvent;

public class ExampleBot extends Bot{
  
  public ExampleBot() {
    super("TauBot");
    initConsole();
    connectRoom("bots");
    addListener(new StandardEventListener("TauBot","I'm a test bot made by @TauNeutrin0. Hi!"));
    addListener(new MessageEventListener(){
        @Override
        public void onSendEvent(MessageEvent evt) {
          if(evt.getSender().equals("TauNeutrin0")&&Math.random()>0.9){
            evt.reply("@TauNeutrin0 has spoken!");
          }
          if(evt.getMessage().matches("^!add @TauBot &[A-Za-z]+$")) {
            Pattern r = Pattern.compile("^!add @TauBot &([A-Za-z]+)$");
            Matcher m = r.matcher(evt.getMessage());
            if (m.find()) {
              if(isConnected(m.group(1))){
                evt.reply("/me is already in &"+m.group(1));
              } else {
                final MessageEvent event = evt;
                final Matcher matcher = m;
                RoomConnection c = createRoomConnection(m.group(1));
                event.reply("/me is attempting to join...");
                c.addConnectionEventListener(new ConnectionEventListener(){
                    @Override
                    public void onConnect(ConnectionEvent arg0) {
                      event.reply("/me has been added to &"+matcher.group(1));
                    }
                    @Override
                    public void onConnectionError(ConnectionEvent arg0) {
                      event.reply("/me could not find &"+matcher.group(1));
                    }
                    @Override
                    public void onDisconnect(ConnectionEvent arg0) {}
                });
                startRoomConnection(c);
              }
            }
          }
        }
    });
  }
  public static void main(String[] args) {
    new ExampleBot();
  }
}
