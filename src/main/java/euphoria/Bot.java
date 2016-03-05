package euphoria;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import euphoria.events.ConnectionEvent;
import euphoria.events.ConnectionEventListener;
import euphoria.events.ConsoleEventListener;
import euphoria.RoomConnection;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.EventListenerList;

public abstract class Bot {
  
  private List<RoomConnection> connections = new ArrayList<RoomConnection>();
  private List<RoomConnection> pendingConnections = new ArrayList<RoomConnection>();
  public EventListenerList listeners = new EventListenerList();
  private Console console;
  private String botName;
  private boolean usesCookies = false;
  private FileIO cookieFile;
  
  public Bot(String botName, boolean startConsole) {
    this.botName = botName;
    if(startConsole){
      try {
        initConsole();
      } catch(java.awt.HeadlessException e) {
        System.err.println("Could not find display.");
      }
    }
    listeners.add(ConnectionEventListener.class, new ConnectionEventListener(){
          @Override
          public void onConnect(ConnectionEvent evt) {
            Bot.this.connections.add(evt.getRoomConnection());
            if(Bot.this.pendingConnections.contains(evt.getRoomConnection())) {
              Bot.this.pendingConnections.remove(evt.getRoomConnection());
            }
          }
          @Override
          public void onDisconnect(ConnectionEvent evt) {
            Bot.this.connections.remove(evt.getRoomConnection());
          }
          @Override
          public void onConnectionError(ConnectionEvent evt) {
            if(Bot.this.pendingConnections.contains(evt.getRoomConnection())) {
              Bot.this.pendingConnections.remove(evt.getRoomConnection());
            }
          }
      });
  }
  
  public RoomConnection getRoomConnection(String room) throws RoomNotConnectedException{
    for(int i=0;i<connections.size();i++) {
      if(connections.get(i)!=null) {
        if(connections.get(i).getRoom().equals(room)){
          return connections.get(i);
        }
      }
    }
    throw new RoomNotConnectedException();
  }
  
  public void connectRoom(String roomName) {
    if(!isConnected(roomName)&&!isPending(roomName)) {
      RoomConnection connection = new RoomConnection(roomName);
      setupRoomConnection(connection);
      pendingConnections.add(connection);
      new Thread(connection).start();
    }
  }
  
  public RoomConnection createRoomConnection(String roomName){
    if(isConnected(roomName)){
      return null;
    } else {
      RoomConnection connection = new RoomConnection(roomName);
      setupRoomConnection(connection);
      return connection;
    }
  }
  
  public void startRoomConnection(RoomConnection connection) { new Thread(connection).start();}
  
  private void setupRoomConnection(RoomConnection connection) {
      connection.setSharedListeners(listeners);
      if(usesCookies) {
        if(!cookieFile.getJson().get("cookie").getAsString().isEmpty())
          connection.setCookies(cookieFile.getJson().get("cookie").getAsString());
        connection.listeners.add(ConnectionEventListener.class,new ConnectionEventListener(){
          @Override
          public void onConnect(ConnectionEvent evt) {
            synchronized(Bot.this.cookieFile){
              JsonObject data = Bot.this.cookieFile.getJson();
              data.remove("cookie");
              data.addProperty("cookie", evt.getRoomConnection().getCookiesAsString());
              try {
                Bot.this.cookieFile.setJson(data);
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }
          @Override
          public void onConnectionError(ConnectionEvent evt) {}
          @Override
          public void onDisconnect(ConnectionEvent evt) {}
        });
      }
    
  }
  
  public void closeAll() {
    for(int i=0;i<connections.size();i++) {
      if(connections.get(i)!=null) {
        connections.get(i).closeConnection("Program exiting.");
      } else {
        System.out.println("Already closed connection.");
      }
    }
  }
  
  public void disconnectRoom(String roomName) {
    for(int i=0;i<connections.size();i++) {
      if(connections.get(i).getRoom().equals(roomName)){
        connections.get(i).closeConnection("Bot disconnecting...");
      }
    }
  }
  
  public boolean isConnected(String roomName) {
    boolean connected=false;
    for(int i=0;i<connections.size();i++) {
      if(connections.get(i).getRoom().equals(roomName)){
        connected=true;
        break;
      }
    }
    return connected;
  }
  
  public boolean isPending(String roomName) {
    boolean pending=false;
    for(int i=0;i<pendingConnections.size();i++) {
      if(pendingConnections.get(i).getRoom().equals(roomName)){
        pending=true;
        break;
      }
    }
    return pending;
  }
  
  public void useCookies(FileIO cookieFile) {
    this.cookieFile = cookieFile;
    
    JsonObject data = cookieFile.getJson();
    if(cookieFile.getJson().has("cookie")){
      if(cookieFile.getJson().get("cookie").isJsonPrimitive()) {
        usesCookies = true;
      } else {
        throw new JsonParseException("Invalid 'cookie' member found.");
      }
    } else {
      data.addProperty("cookie", "");
      try {
        cookieFile.setJson(data);
        usesCookies = true;
      } catch (IOException e) {
        throw new JsonParseException("Could not create 'cookie' field.");
      }
    }
  }
  
  public void initConsole() {
    console = new euphoria.Console(botName);
    console.addWindowListener(new WindowListener(){
        @Override
        public void windowActivated(WindowEvent arg0) {}
        @Override
        public void windowClosed(WindowEvent arg0) {}
        @Override
        public void windowClosing(WindowEvent arg0) {
          System.out.println("Closing connections...");
          closeAll();
          System.exit(0);
        }
        @Override
        public void windowDeactivated(WindowEvent arg0) {}
        @Override
        public void windowDeiconified(WindowEvent arg0) {}
        @Override
        public void windowIconified(WindowEvent arg0) {}
        @Override
        public void windowOpened(WindowEvent arg0) {}
    });
  }
  
  public void addConsoleListener(ConsoleEventListener evtLst) {
    if(console!=null){
      console.addListener(evtLst);
    }
  }
  
  public void removeConsoleListener(ConsoleEventListener evtLst) {
    if(console!=null){
      console.removeListener(evtLst);
    }
  }
}
