package euphoria.events;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import euphoria.*;

public class ConnectConsoleEventListener implements ConsoleEventListener{
  Bot bot;
  String nick;
  boolean hasDataFile = false;
  FileIO dataFile;
  
  public ConnectConsoleEventListener(Bot bot) {
    this.bot=bot;
  }
  public ConnectConsoleEventListener(Bot bot, FileIO dataFile) throws JsonParseException {
    this.bot=bot;
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
  
  public ConnectConsoleEventListener connectAll() {
    for(int i=0;i<dataFile.getJson().getAsJsonArray("rooms").size();i++){
      bot.connectRoom(dataFile.getJson().getAsJsonArray("rooms").get(i).getAsString());
    }
    return this;
  }

  @Override
  public void onCommand(String message) {
    if(message.matches("^!sendbot &[A-Za-z]+$")) {
      Pattern r = Pattern.compile("^!sendbot @"+nick+" &([A-Za-z]+)$");
      Matcher m = r.matcher(message);
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
              System.out.println("/me has been added to &"+m.group(1));
            } else {
              System.out.println("/me is already in &"+m.group(1));
            }
          }
        } else {
          final Matcher matcher = m;
          RoomConnection c = bot.createRoomConnection(m.group(1));
          System.out.println("/me is attempting to join...");
          c.addConnectionEventListener(new ConnectionEventListener(){
              @Override
              public void onConnect(ConnectionEvent arg0) {
                System.out.println("/me has been added to &"+matcher.group(1));
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
                System.out.println("/me could not find &"+matcher.group(1));
              }
              @Override
              public void onDisconnect(ConnectionEvent arg0) {}
          });
          bot.startRoomConnection(c);
        }
      }
    }


    else if(message.matches("^!removebot &[A-Za-z]+$")) {
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
          System.out.println("/me has been removed from &"+m.group(1));
        } else {
          System.out.println("/me is not in &"+m.group(1));
        }
      }
    }
  }
}
