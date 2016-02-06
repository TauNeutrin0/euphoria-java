package euphoria;

import java.util.ArrayList;
import java.util.List;

public abstract class Bot {
  List<RoomConnection> connections = new ArrayList<RoomConnection>();
  
  public void addListener(PacketEventListener eL) {
    for(int i=0;i<connections.size();i++) {
      connections.get(i).addPacketEventListener(eL);
    }
  }
  
  public void removeListener(PacketEventListener eL) {
    for(int i=0;i<connections.size();i++) {
      connections.get(i).removePacketEventListener(eL);
    }
  }
  
  public void connectRoom(String roomName) {
    RoomConnection connection = new RoomConnection();
    connection.createConnection(roomName);
    connections.add(connection);
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
