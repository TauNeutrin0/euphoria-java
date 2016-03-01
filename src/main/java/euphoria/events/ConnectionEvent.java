package euphoria.events;

import euphoria.RoomConnection;

import java.util.EventObject;

public class ConnectionEvent extends EventObject {
  protected RoomConnection roomConnection;
  private String roomName;
  
  public ConnectionEvent(RoomConnection roomCon,String name) {
    super(roomCon);
    roomConnection=roomCon;
    roomName=name;
  }
  public String getRoomName() {
    return roomName;
  }
    
  public RoomConnection getRoomConnection() {
    return roomConnection;
  }
}
