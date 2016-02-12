package euphoria;

import euphoria.ConnectionEvent;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.event.EventListenerList;

public abstract class Bot {
  
  List<RoomConnection> connections = new LinkedList<RoomConnection>();
  private EventListenerList roomListeners = new EventListenerList();
  Console console;
  String botName;
  
  public Bot(String botName) {
    this.botName = botName;
    roomListeners.add(ConnectionEventListener.class, new ConnectionEventListener(){
          @Override
          public void onConnect(ConnectionEvent evt) {
            Bot.this.connections.add(evt.getRoomConnection());
          }
          @Override
          public void onDisconnect(ConnectionEvent evt) {
            Bot.this.connections.remove(evt.getRoomConnection());
          }
          @Override
          public void onConnectionError(ConnectionEvent evt) {
          }
      });
  }
  
  public void addListener(PacketEventListener eL) {
    roomListeners.add(PacketEventListener.class, eL);
  }
  
  public void removeListener(PacketEventListener eL) {
    roomListeners.remove(PacketEventListener.class, eL);
  }
  
  public void connectRoom(String roomName) {
    if(!isConnected(roomName)) {
      RoomConnection connection = new RoomConnection(roomName, roomListeners);
      new Thread(connection).start();
    }
  }
  
  public RoomConnection createRoomConnection(String roomName){
    if(isConnected(roomName)){
      return null;
    } else {
      RoomConnection connection = new RoomConnection(roomName, roomListeners);
      return connection;
    }
  }
  
  public void startRoomConnection(RoomConnection connection) { new Thread(connection).start();}
  
  
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
}
