package euphoria;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import euphoria.WebsocketJSON.*;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.event.EventListenerList;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

@org.eclipse.jetty.websocket.api.annotations.WebSocket
public class RoomConnection {
  
  private final CountDownLatch closeLatch;
  private Session session;
  private WebSocketClient client;
  private String sessionID;
  private boolean connected = false;
  protected EventListenerList listeners = new EventListenerList();
  private String room;
                             
  public RoomConnection() {
    this.closeLatch = new CountDownLatch(1);
  }
  
  public Future<Void> createConnection(String room) {
    this.room=room;
    String destUri = "wss://euphoria.io/room/"+room+"/ws";
    SslContextFactory sslContextFactory = new SslContextFactory();
    sslContextFactory.setTrustAll(true);
    client = new WebSocketClient(sslContextFactory);
    try {
      client.start();
      URI echoUri = new URI(destUri);
      ClientUpgradeRequest request = new ClientUpgradeRequest();
      client.connect(this, echoUri, request);
      System.out.printf("Connecting to : %s%n", echoUri);
    } catch (Throwable t) {
      t.printStackTrace();
    }
    return null;
  }

  public void closeConnection(String reason) {
    session.close(StatusCode.NORMAL, reason);
    try {
      client.stop();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
    return this.closeLatch.await(duration, unit);
  }

  @OnWebSocketClose
  public void onClose(int statusCode, String reason) {
    System.out.printf("Connection closed: %d - %s%n", statusCode, reason);
    this.session = null;
    this.closeLatch.countDown();
  }

  @OnWebSocketConnect
  public void onConnect(Session session) {
    System.out.println("Connected!");
    this.session = session;
  }

  @OnWebSocketMessage
  public void onMessage(String msg) {
    JsonObject jsonObj = new JsonParser().parse(msg).getAsJsonObject();
    System.out.println("Got msg: "+jsonObj.get("type").getAsString());
    try{
      StandardPacket packet = createPacketFromJson(jsonObj);
      if(packet.getData().handle(this)){
        Object[] lns = listeners.getListenerList();
        PacketEvent evt = new PacketEvent(this,packet);
        for (int i = 0; i < lns.length; i = i+2) {
          if (lns[i] == PacketEventListener.class) {
            java.lang.reflect.Method method;
            try {
              method = ((PacketEventListener) lns[i+1]).getClass().getMethod(packet.getData().getClass().getSimpleName(),PacketEvent.class);
              if(!method.isAccessible()) {
                method.setAccessible(true);
              }
              method.invoke(((PacketEventListener) lns[i+1]),evt);
            } catch (IllegalArgumentException e) {e.printStackTrace();
            } catch (IllegalAccessException e) {e.printStackTrace();
            } catch (InvocationTargetException e) {e.printStackTrace();
            } catch (SecurityException e) {e.printStackTrace();
            } catch (NoSuchMethodException e) {
              System.out.println("No handler provided for "+packet.getType()+".");
            }
          }
        }
      }
    } catch (JsonParseException e) {
      System.out.println("Could not recognise type.");
    }
    
  }
  
  public void addPacketEventListener(PacketEventListener listener) {
    listeners.add(PacketEventListener.class, listener);
    System.out.println("Added listener...");
  }
  public void removePacketEventListener(PacketEventListener listener) {
    listeners.remove(PacketEventListener.class, listener);
  }
  
  public void sendServerMessage(String message) {
    try {
      Future<Void> fut;
      fut = session.getRemote().sendStringByFuture(message);
      fut.get(2, TimeUnit.SECONDS);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }
  public void sendServerMessage(JsonObject message) {
    Gson gson = new Gson();
    try {
      Future<Void> fut;
      fut = session.getRemote().sendStringByFuture(gson.toJson(message));
      fut.get(2, TimeUnit.SECONDS);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }
  public void sendServerMessage(StandardPacket pckt) {
    GsonBuilder gsonBilder = new GsonBuilder();
    gsonBilder.registerTypeAdapter(StandardPacket.class, new DataAdapter());
    Gson gson = gsonBilder.create();
    try {
      Future<Void> fut;
      fut = session.getRemote().sendStringByFuture(gson.toJson(pckt));
      fut.get(2, TimeUnit.SECONDS);
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }
  
  public StandardPacket createPacketFromJson(JsonObject json) {
    GsonBuilder gsonBilder = new GsonBuilder();
    gsonBilder.registerTypeAdapter(StandardPacket.class, new DataAdapter());
    Gson gson = gsonBilder.create();
    StandardPacket packet = gson.fromJson(json,StandardPacket.class);
    return packet;
  }
  public String createJsonFromPacket(StandardPacket pckt) {
    GsonBuilder gsonBilder = new GsonBuilder();
    gsonBilder.registerTypeAdapter(StandardPacket.class, new DataAdapter());
    Gson gson = gsonBilder.create();
    String json = gson.toJson(pckt);
    return json;
  }
  
  public void sendMessage(String message){ sendServerMessage(new Send(message).createPacket());}
  public void sendMessage(String message,String replyId){ sendServerMessage(new Send(message,replyId).createPacket());}
  public void changeNick(String nick){ sendServerMessage(new Nick(nick).createPacket());}
  public String getRoom() {return room;}
}
