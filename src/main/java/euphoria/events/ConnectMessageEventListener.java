package euphoria.events;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import euphoria.*;
import euphoria.RoomNotConnectedException;
import euphoria.events.MessageEvent;
import euphoria.events.PacketEvent;

public class ConnectMessageEventListener implements PacketEventListener, ConsoleEventListener {
  Bot bot;
  String nick;
  boolean hasDataFile = false;
  FileIO dataFile;
  
  public ConnectMessageEventListener(String nick, Bot bot) {
    this.bot=bot;
    this.nick=nick;
  }
  public ConnectMessageEventListener(String nick, Bot bot, FileIO dataFile) throws JsonParseException {
    this.bot=bot;
    this.nick=nick;
    this.dataFile = dataFile;
    synchronized(dataFile){
      JsonObject data = dataFile.getJson();
      if(data.has("rooms")){
        if(!data.get("rooms").isJsonArray()) {
          throw new JsonParseException("Invalid 'room' member found.");
        }
      } else {
        JsonArray arrayObject = new JsonArray();
        arrayObject.add("bots");
        data.add("rooms", arrayObject);
        try {
          dataFile.setJson(data);
        } catch (IOException e) {
          throw new JsonParseException("Could not create 'room' member.");
        }
      }
      if(dataFile.getJson().has("room-passwords")){
        if(!dataFile.getJson().get("room-passwords").isJsonObject()) {
          throw new JsonParseException("Invalid 'room-passwords' member found.");
        }
      } else {
        data.add("room-passwords", new JsonObject());
        try {
          dataFile.setJson(data);
        } catch (IOException e) {
          throw new JsonParseException("Could not create 'room-passwords' member.");
        }
      }
    }
    hasDataFile = true;
  }
  public ConnectMessageEventListener connectAll() {
    synchronized(dataFile){
      final JsonObject data = dataFile.getJson();
      for(int i=0;i<data.getAsJsonArray("rooms").size();i++){
        final String room = data.getAsJsonArray("rooms").get(i).getAsString();
        if(!(bot.isConnected(room)||bot.isPending(room))){
          RoomConnection c = bot.createRoomConnection(room);
          
          c.listeners.add(PacketEventListener.class, new PacketEventListener() {
            @Override
            public void onBounceEvent(PacketEvent evt) {
              if(data.getAsJsonObject("room-passwords").has(room)){
                try {
                  RoomConnection rmCon = bot.getRoomConnection(room);
                  if(rmCon.isBounced()){
                    rmCon.tryPassword(data.getAsJsonObject("room-passwords").get(room).getAsString(), new ReplyEventListener() {
                        @Override
                        public void onReplyEvent(PacketEvent arg0) {}
                        @Override
                        public void onReplyFail(PacketEvent arg0) {
                          System.out.println("The password for &"+room+" supplied in the data file is incorrect.");
                        }
                        @Override
                        public void onReplySuccess(PacketEvent arg0) {
                          //Connected successfully
                          System.out.println("Password accepted. "+nick+" has been added to &"+room);
                        }
                    });
                  } else {
                    //Already connected
                  }
                } catch (RoomNotConnectedException e) {
                  //Couldn't find connection
                }
              } else {
                System.out.println("&"+room+" is private.\n"
                                   +"Use \"!trypass @"+nick+" &"+room+" [password]\" to attempt to connect with a password.");
              }
            }
            public void onHelloEvent(PacketEvent arg0) {}
            public void onJoinEvent(PacketEvent arg0) {}
            public void onNickEvent(PacketEvent arg0) {}
            public void onPartEvent(PacketEvent arg0) {}
            public void onSendEvent(MessageEvent arg0) {}
            public void onSnapshotEvent(PacketEvent arg0) {}
          });
          
          bot.startRoomConnection(c);
        }
      }
    }
    return this;
  }

  @Override
  public void onSendEvent(MessageEvent evt) {
    if(evt.getMessage().matches("^!sendbot @"+nick+" &[A-Za-z]+$")) {
      Pattern r = Pattern.compile("^!sendbot @"+nick+" &([A-Za-z]+)$");
      final Matcher m = r.matcher(evt.getMessage());
      if (m.find()) {
        if(bot.isConnected(m.group(1))||bot.isPending(m.group(1))){
          if(hasDataFile) {
            boolean isStored = false;
            for(int i=0;i<dataFile.getJson().getAsJsonArray("rooms").size();i++){
              if(dataFile.getJson().getAsJsonArray("rooms").get(i).getAsString().equals(m.group(1))&&!m.group(1).equals("bots")){
                isStored = true;
              }
            }
            if(!isStored) {
              synchronized(dataFile){
                JsonObject data = dataFile.getJson();
                data.getAsJsonArray("rooms").add(m.group(1));
                try {
                  dataFile.setJson(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
              }
              evt.reply("/me has been added to &"+m.group(1));
            } else {
              evt.reply("/me is already in &"+m.group(1));
            }
          }
        } else {
          final MessageEvent event = evt;
          RoomConnection c = bot.createRoomConnection(m.group(1));
          event.reply("/me is attempting to join...");
          
          c.listeners.add(ConnectionEventListener.class,new ConnectionEventListener(){
              @Override
              public void onConnectionError(ConnectionEvent arg0) {
                event.reply("/me could not find &"+m.group(1));
              }
              public void onConnect(ConnectionEvent arg0) {}
              public void onDisconnect(ConnectionEvent arg0) {}
          });
          
          c.listeners.add(PacketEventListener.class, new PacketEventListener() {
            @Override
            public void onBounceEvent(PacketEvent evt) {
              /*event.reply("&"+matcher.group(1)+" is private. "
                          +"Use \"!trypass @"+nick+" &"+matcher.group(1)+" [password]\" to attempt to connect.\n"
                          +"Please be aware that this will be visible to anyone in this room.");*/
              event.reply("&"+m.group(1)+" is private.");
              evt.getRoomConnection().closeConnection("Room is private.");
            }
            
            @Override
            public void onSnapshotEvent(PacketEvent evt) {
              event.reply("/me has been added to &"+m.group(1));
              if(hasDataFile) {
                boolean isStored = false;
                for(int i=0;i<dataFile.getJson().getAsJsonArray("rooms").size();i++){
                  if(dataFile.getJson().getAsJsonArray("rooms").get(i).getAsString().equals(m.group(1))&&!m.group(1).equals("bots")){
                    isStored = true;
                  }
                }
                if(!isStored) {
                  synchronized(dataFile){
                    JsonObject data = dataFile.getJson();
                    data.getAsJsonArray("rooms").add(m.group(1));
                    try {
                      dataFile.setJson(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                  }
                }
              }
            }
            public void onHelloEvent(PacketEvent arg0) {}
            public void onJoinEvent(PacketEvent arg0) {}
            public void onNickEvent(PacketEvent arg0) {}
            public void onPartEvent(PacketEvent arg0) {}
            public void onSendEvent(MessageEvent arg0) {}
          });
          
          bot.startRoomConnection(c);
        }
      }
    }

    else if(evt.getMessage().matches("^!removebot @"+nick+" &[A-Za-z]+$")) {
      Pattern r = Pattern.compile("^!removebot @"+nick+" &([A-Za-z]+)$");
      Matcher m = r.matcher(evt.getMessage());
      if (m.find()) {
        boolean removed = false;
        if(hasDataFile) {
          
          synchronized(dataFile){
            JsonObject data = dataFile.getJson();
            for(int i=0;i<data.getAsJsonArray("rooms").size();i++){
              if(data.getAsJsonArray("rooms").get(i).getAsString().equals(m.group(1))&&!m.group(1).equals("bots")){
                data.getAsJsonArray("rooms").remove(i);
                removed = true;
              }
            }
            if(removed) {
              try {
                dataFile.setJson(data);
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }
        }

        if(bot.isConnected(m.group(1))||bot.isPending(m.group(1))){
          bot.disconnectRoom(m.group(1));
          removed = true;
        }

        if(removed) {
          evt.reply("/me has been removed from &"+m.group(1));
        } else {
          evt.reply("/me is not in &"+m.group(1));
        }
      }
    }
    
    /*  PASSWORD CODE: Removed due to potential security issues.
      else if(evt.getMessage().matches("^!trypass @"+nick+" &[A-Za-z]+ [\\S\\s]+$")) {
      Pattern r = Pattern.compile("^!trypass @"+nick+" &([A-Za-z]+) ([\\S\\s]+)$");
      final Matcher m = r.matcher(evt.getMessage());
      final MessageEvent event = evt;
      if (m.find()) {
        try {
          RoomConnection rmCon = bot.getRoomConnection(m.group(1));
          if(rmCon.isBounced()){
            rmCon.tryPassword(m.group(2), new ReplyEventListener() {
                @Override
                public void onReplyEvent(PacketEvent arg0) {}
                @Override
                public void onReplyFail(PacketEvent arg0) {
                  event.reply("The password is incorrect.");
                }
                @Override
                public void onReplySuccess(PacketEvent arg0) {
                  event.reply("/me has been added to &"+m.group(1));
                }
            });
          } else {
            evt.reply("/me is already in &"+m.group(1));
          }
        } catch (RoomNotConnectedException e) {
          evt.reply("/me could not find connection to &"+m.group(1));
        }
      }
    }*/
  }
  
  @Override
  public void onCommand(String message) {
    if(message.matches("^!sendbot @"+nick+" &[A-Za-z]+$")) {
      Pattern r = Pattern.compile("^!sendbot @"+nick+" &([A-Za-z]+)$");
      Matcher m = r.matcher(message);
      if (m.find()) {
        if(bot.isConnected(m.group(1))||bot.isPending(m.group(1))){
          if(hasDataFile) {
            boolean isStored = false;
            for(int i=0;i<dataFile.getJson().getAsJsonArray("rooms").size();i++){
              if(dataFile.getJson().getAsJsonArray("rooms").get(i).getAsString().equals(m.group(1))&&!m.group(1).equals("bots")){
                isStored = true;
              }
            }
            if(!isStored) {
              synchronized(dataFile){
                JsonObject data = dataFile.getJson();
                data.getAsJsonArray("rooms").add(m.group(1));
                try {
                  dataFile.setJson(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
              }
              System.out.println(nick+" has been added to &"+m.group(1));
            } else {
              System.out.println(nick+" is already in &"+m.group(1));
            }
          }
        } else {
          
          final Matcher matcher = m;
          RoomConnection c = bot.createRoomConnection(m.group(1));
          System.out.println(nick+" is attempting to join...");
          
          c.listeners.add(ConnectionEventListener.class,new ConnectionEventListener(){
            @Override
            public void onConnectionError(ConnectionEvent arg0) {
              System.out.println(nick+" could not find &"+matcher.group(1));
            }
            public void onConnect(ConnectionEvent arg0) {}
            public void onDisconnect(ConnectionEvent evt) {}
          });
          
          c.listeners.add(PacketEventListener.class, new PacketEventListener() {
            @Override
            public void onBounceEvent(PacketEvent evt) {
              System.out.println("&"+matcher.group(1)+" is private.\n"
                                 +"Use \"!trypass @"+nick+" &"+matcher.group(1)+" [password]\" to attempt to connect with a password.");
            }
            
            @Override
            public void onSnapshotEvent(PacketEvent arg0) {
              System.out.println(nick+" has been added to &"+matcher.group(1));
              if(hasDataFile) {
                boolean isStored = false;
                for(int i=0;i<dataFile.getJson().getAsJsonArray("rooms").size();i++){
                  if(dataFile.getJson().getAsJsonArray("rooms").get(i).getAsString().equals(matcher.group(1))&&!matcher.group(1).equals("bots")){
                    isStored = true;
                  }
                }
                if(!isStored) {
                  synchronized(dataFile){
                    JsonObject data = dataFile.getJson();
                    data.getAsJsonArray("rooms").add(matcher.group(1));
                    try {
                      dataFile.setJson(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                  }
                }
              }
            }
            public void onHelloEvent(PacketEvent arg0) {}
            public void onJoinEvent(PacketEvent arg0) {}
            public void onNickEvent(PacketEvent arg0) {}
            public void onPartEvent(PacketEvent arg0) {}
            public void onSendEvent(MessageEvent arg0) {}
          });
          
          bot.startRoomConnection(c);
        }
      }
    }


    else if(message.matches("^!removebot @"+nick+" &[A-Za-z]+$")) {
      Pattern r = Pattern.compile("^!removebot @"+nick+" &([A-Za-z]+)$");
      Matcher m = r.matcher(message);
      if (m.find()) {
        boolean removed = false;
        if(hasDataFile) {
          
          synchronized(dataFile){
            JsonObject data = dataFile.getJson();
            for(int i=0;i<data.getAsJsonArray("rooms").size();i++){
              if(data.getAsJsonArray("rooms").get(i).getAsString().equals(m.group(1))&&!m.group(1).equals("bots")){
                data.getAsJsonArray("rooms").remove(i);
                removed = true;
              }
            }
            if(data.getAsJsonObject("room-passwords").has(m.group(1))){
              data.getAsJsonObject("room-passwords").remove(m.group(1));
            }
            if(removed) {
              try {
                dataFile.setJson(data);
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }
        }

        if(bot.isConnected(m.group(1))||bot.isPending(m.group(1))){
          bot.disconnectRoom(m.group(1));
          removed = true;
        }

        if(removed) {
          System.out.println(nick+" has been removed from &"+m.group(1));
        } else {
          System.out.println(nick+" is not in &"+m.group(1));
        }
      }
    }
    
    
    else if(message.matches("^!trypass @"+nick+" &[A-Za-z]+ [\\S\\s]+$")) {
      Pattern r = Pattern.compile("^!trypass @"+nick+" &([A-Za-z]+) ([\\S\\s]+)$");
      final Matcher m = r.matcher(message);
      if (m.find()) {
        try {
          RoomConnection rmCon = bot.getRoomConnection(m.group(1));
          if(rmCon.isBounced()){
            rmCon.tryPassword(m.group(2), new ReplyEventListener() {
              public void onReplyEvent(PacketEvent arg0) {}

              @Override
              public void onReplyFail(PacketEvent arg0) {
                System.out.println("The password to &"+m.group(1)+" is incorrect.");
              }
              @Override
              public void onReplySuccess(PacketEvent arg0) {
                System.out.println("Password accepted. "+nick+" has been added to &"+m.group(1));
                synchronized(dataFile){
                  JsonObject data = dataFile.getJson();
                  if(!data.getAsJsonObject("room-passwords").has(m.group(1))){
                    data.getAsJsonObject("room-passwords").addProperty(m.group(1), m.group(2));
                    try {
                      dataFile.setJson(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                  }
                }
              }
            });
          } else {
            System.out.println(nick+" is already in &"+m.group(1));
          }
        } catch (RoomNotConnectedException e) {
          System.out.println(nick+" could not find connection to &"+m.group(1));
        }
      }
    }
  }
  public void onHelloEvent(PacketEvent arg0) {}
  public void onJoinEvent(PacketEvent arg0) {}
  public void onNickEvent(PacketEvent arg0) {}
  public void onPartEvent(PacketEvent arg0) {}
  public void onSnapshotEvent(PacketEvent arg0) {}
  public void onBounceEvent(PacketEvent arg0) {}
}
