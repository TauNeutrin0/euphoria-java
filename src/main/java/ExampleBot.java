
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonObject;
import euphoria.*;

public class ExampleBot extends Bot{
  FileIO dataFile;
  
  public ExampleBot() {
    super("TauBot");
    initConsole();
    dataFile = new FileIO("data");
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
          } else if(evt.getMessage().matches("^!set [0-9] [\\s\\S]+$")) {
            Pattern r = Pattern.compile("^!set ([0-9]) ([\\s\\S]+)$");
            Matcher m = r.matcher(evt.getMessage());
            if (m.find()) {
              JsonObject obj = dataFile.getJson();
              obj.addProperty(m.group(1), m.group(2));
              //System.out.println(m.group(1)+" "+m.group(2));
              try {
                dataFile.setJson(obj);
                evt.reply("Set "+m.group(1)+": "+m.group(2)+".");
              } catch (IOException e) {
                  evt.reply("Could not set "+m.group(1)+".");
              }
            }
          } else if(evt.getMessage().matches("^!get [0-9]$")) {
            Pattern r = Pattern.compile("^!get ([0-9])$");
            Matcher m = r.matcher(evt.getMessage());
            if (m.find()) {
              JsonObject obj = dataFile.getJson();
              if(obj.has(m.group(1))) {
                evt.reply(obj.get(m.group(1)).getAsString());
              } else {
                evt.reply("Nothing stored in "+m.group(1)+".");
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
