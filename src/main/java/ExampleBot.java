
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonObject;
import euphoria.*;
import euphoria.RoomNotConnectedException;
import euphoria.events.*;
import euphoria.events.ConnectionEvent;
import euphoria.events.PacketEvent;

public class ExampleBot extends Bot{
  FileIO dataFile;
  
  public ExampleBot() {
    dataFile = new FileIO("exampleBot_data");
    useCookies(dataFile);
    
    listeners.add(PacketEventListener.class,new StandardEventListener(this,"TauBot","I'm a test bot made by @TauNeutrin0. Hi!"));
    
    ConnectMessageEventListener cMEL = new ConnectMessageEventListener("TauBot",this,dataFile).connectAll();
    addConsoleListener(cMEL);
    listeners.add(PacketEventListener.class,cMEL);
    
    connectRoom("test");
    
    addConsoleListener(new ConsoleEventListener() {
      @Override
      public void onCommand(String command) {
        Pattern r = Pattern.compile("^!boop &(\\S+)");
        Matcher m = r.matcher(command);
        if(m.find()){
          try {
              getRoomConnection(m.group(1)).sendMessage("Boop");
              System.out.println("Booped &"+m.group(1)+"!");
          } catch (RoomNotConnectedException e) {
              System.out.println("Not connected to &"+m.group(1)+"!");
          }
        }
      }
    });
    
    final MessageEventListener announceListener = new MessageEventListener(){
      @Override
      public void onSendEvent(MessageEvent evt) {
        if(evt.getSender().equals("TauNeutrin0")&&Math.random()>0.9){
          System.out.println("Announce!");
          evt.reply("@TauNeutrin0 has spoken!");
        }
      }
    };
    listeners.add(ConnectionEventListener.class,new ConnectionEventListener() {
        @Override
        public void onConnect(ConnectionEvent evt) {
          if(evt.getRoomName().equals("bots")){
            evt.getRoomConnection().listeners.add(PacketEventListener.class, announceListener);
          }
        }
        @Override
        public void onConnectionError(ConnectionEvent evt) {}
        @Override
        public void onDisconnect(ConnectionEvent evt) {}
    });
    
    listeners.add(PacketEventListener.class,new MessageEventListener(){
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
              evt.replyTracked(obj.get(m.group(1)).getAsString(),new ReplyEventListener() {
                @Override
                public void onReplyEvent(PacketEvent arg0) {System.out.println("Got reply.");}
                @Override
                public void onReplyFail(PacketEvent arg0) {System.out.println("Reply fail.");}
                @Override
                public void onReplySuccess(PacketEvent arg0) {System.out.println("Reply success.");}
              });
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
