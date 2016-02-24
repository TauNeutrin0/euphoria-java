
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonObject;
import euphoria.*;
import euphoria.ConnectionEvent;
import euphoria.RoomNotConnectedException;

public class ExampleBot extends Bot{
  FileIO dataFile;
  
  public ExampleBot() {
    super("TauBot");
    try {
      initConsole();
    } catch(java.awt.HeadlessException e) {
      System.err.println("Could not find display.");
    }
    dataFile = new FileIO("exampleBot_data");
    connectRoom("bots");
    final MessageEventListener announceListener = new MessageEventListener(){
      @Override
      public void onSendEvent(MessageEvent evt) {
        System.out.println("Announce!");
        if(evt.getSender().equals("TauNeutrin0")&&Math.random()>0.9){
          evt.reply("@TauNeutrin0 has spoken!");
        }
      }
    };
    
    addConnectionEventListener(new ConnectionEventListener() {
        @Override
        public void onConnect(ConnectionEvent evt) {
          if(evt.getRoomConnection().getRoom().equals("bots")){
            evt.getRoomConnection().addPacketEventListener(announceListener);
          }
        }
        @Override
        public void onConnectionError(ConnectionEvent evt) {}
        @Override
        public void onDisconnect(ConnectionEvent evt) {}
    });
    
    addPacketEventListener(new StandardEventListener("TauBot","I'm a test bot made by @TauNeutrin0. Hi!"));
    addPacketEventListener(new ConnectMessageEventListener("TauBot",this,dataFile));
    addPacketEventListener(new MessageEventListener(){
      @Override
      public void onSendEvent(MessageEvent evt) {
        
        if(evt.getMessage().matches("^!set [0-9] [\\s\\S]+$")) {
          Pattern r = Pattern.compile("^!set ([0-9]) ([\\s\\S]+)$");
          Matcher m = r.matcher(evt.getMessage());
          if (m.find()) {
            JsonObject obj = dataFile.getJson();
            obj.addProperty(m.group(1), m.group(2));
            try {
              dataFile.setJson(obj);
              evt.reply("Set "+m.group(1)+": "+m.group(2)+".");
            } catch (IOException e) {
                evt.reply("Could not set "+m.group(1)+".");
            }
          }
        }
        
        
        else if(evt.getMessage().matches("^!get [0-9]$")) {
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
