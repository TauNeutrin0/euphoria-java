package euphoria;

import euphoria.ConnectionEvent;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.EventListenerList;

public abstract class Bot {
  List<RoomConnection> connections = new ArrayList<RoomConnection>();
  private EventListenerList roomListeners = new EventListenerList();
  
  public void addListener(PacketEventListener eL) {
    roomListeners.add(PacketEventListener.class,eL);
    for(int i=0;i<connections.size();i++) {
      connections.get(i).addPacketEventListener(eL);
    }
  }
  
  public void removeListener(PacketEventListener eL) {
    roomListeners.remove(PacketEventListener.class,eL);
    for(int i=0;i<connections.size();i++) {
      connections.get(i).removePacketEventListener(eL);
    }
  }
  
  public void connectRoom(String roomName) {
    RoomConnection connection = new RoomConnection(roomListeners);
    connection.createConnection(roomName);
    connection.addConnectionEventListener(new ConnectionEventListener(){
        @Override
        public void onConnect(ConnectionEvent evt) {
          Bot.this.connections.add(evt.getRoomConnection());
        }
        @Override
        public void onDisconnect(ConnectionEvent evt) {
          Bot.this.connections.remove(evt.getRoomConnection());
        }
    });
  }
  
  public void closeAll() {
    for(int i=0;i<connections.size();i++) {
      connections.get(i).closeConnection("Program exiting...");
    }
  }
  
  
  
  public void disconnectRoom(String roomName) {
    for(int i=0;i<connections.size();i++) {
      if(connections.get(i).getRoom().equals(roomName)){
        connections.get(i).closeConnection("Bot disconnecting...");
        connections.remove(i);
      }
    }
  }
}
