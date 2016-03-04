package euphoria.events;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import euphoria.*;
import euphoria.events.PacketEvent;

public class ConnectMessageEventListener implements PacketEventListener {
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
      if(dataFile.getJson().has("rooms")){
        if(dataFile.getJson().get("rooms").isJsonArray()) {
          hasDataFile = true;
        } else {
          throw new JsonParseException("Invalid 'room' member found.");
        }
      } else {
        JsonArray arrayObject = new JsonArray();
        arrayObject.add("bots");
        data.add("rooms", arrayObject);
        try {
          dataFile.setJson(data);
        } catch (IOException e) {
          e.printStackTrace(); //TODO Handle this?
        }
        hasDataFile = true;
      }
    }
  }
  public ConnectMessageEventListener connectAll() {
    for(int i=0;i<dataFile.getJson().getAsJsonArray("rooms").size();i++){
      bot.connectRoom(dataFile.getJson().getAsJsonArray("rooms").get(i).getAsString());
    }
    return this;
  }

  @Override
  public void onHelloEvent(PacketEvent arg0) {}

  @Override
  public void onJoinEvent(PacketEvent arg0) {}

  @Override
  public void onNickEvent(PacketEvent arg0) {}

  @Override
  public void onPartEvent(PacketEvent arg0) {}

  @Override
  public void onSendEvent(MessageEvent evt) {
    if(evt.getMessage().matches("^!sendbot @"+nick+" &[A-Za-z]+$")) {
      Pattern r = Pattern.compile("^!sendbot @"+nick+" &([A-Za-z]+)$");
      Matcher m = r.matcher(evt.getMessage());
      if (m.find()) {
        if(bot.isConnected(m.group(1))){
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
          final Matcher matcher = m;
          RoomConnection c = bot.createRoomConnection(m.group(1));
          event.reply("/me is attempting to join...");
          c.listeners.add(ConnectionEventListener.class,new ConnectionEventListener(){
              @Override
              public void onConnect(ConnectionEvent arg0) {
                event.reply("/me has been added to &"+matcher.group(1));
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
              @Override
              public void onConnectionError(ConnectionEvent arg0) {
                event.reply("/me could not find &"+matcher.group(1));
              }
              @Override
              public void onDisconnect(ConnectionEvent arg0) {}
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

        if(bot.isConnected(m.group(1))){
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
  }

  @Override
  public void onSnapshotEvent(PacketEvent arg0) {}
  @Override
  public void onBounceEvent(PacketEvent arg0) {}
}
