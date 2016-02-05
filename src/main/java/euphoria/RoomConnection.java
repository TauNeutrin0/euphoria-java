package euphoria;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import euphoria.WebsocketJSON.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

@org.eclipse.jetty.websocket.api.annotations.WebSocket
public class RoomConnection {
  
  private final CountDownLatch closeLatch;
  private Session session;
  private String sessionID;
  private boolean connected = false;
                             
  public RoomConnection() {
    this.closeLatch = new CountDownLatch(1);
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
    /*try {
      Future<Void> fut;
      fut = session.getRemote().sendStringByFuture("Hello");
      fut.get(2, TimeUnit.SECONDS);
      fut = session.getRemote().sendStringByFuture("Thanks for the conversation.");
      fut.get(2, TimeUnit.SECONDS);
      session.close(StatusCode.NORMAL, "I'm done");
    } catch (Throwable t) {
      t.printStackTrace();
    }*/
  }

  @OnWebSocketMessage
  public void onMessage(String msg) {
    JsonObject jsonObj = new JsonParser().parse(msg).getAsJsonObject();
    System.out.println("Got msg: "+jsonObj.get("type").getAsString());
    if(jsonObj.get("type").getAsString().equals("ping-event")){
      StandardPacket packet = createPacketFromJson(jsonObj);
      StandardPacket reply = ((PingEvent)packet.getData()).createPingReply();
      sendServerMessage(createJsonFromPacket(reply));
      /*System.out.println("Ping time: "+((PingEvent)packet.getData()).getTime());
      Gson gson = new Gson();
      JsonObject reply = new JsonObject();
      reply.addProperty("type", "ping-reply");
      JsonObject data = new JsonObject();
      data.addProperty("time",jsonObj.get("data").getAsJsonObject().get("time").getAsInt());
      reply.add("data", data);
      sendServerMessage(reply);*/
      //sendServerMessage(gson.toJson(new PingReply()));
      System.out.println("Sent ping-reply: "+createJsonFromPacket(reply));
    } else if(jsonObj.get("type").getAsString().equals("hello-event")){
      sessionID = jsonObj.get("data").getAsJsonObject().get("id").getAsString();
      changeNick("TauBot");
      connected=true;
    } else if(jsonObj.get("type").getAsString().equals("send-event")){
      if(connected&&jsonObj.get("data").getAsJsonObject().get("sender").getAsJsonObject().get("name").getAsString().equals("TauNeutrin0")){
        System.out.println("TAU HAS SPOKEN!");
        if(Math.random()>0.9){
          sendMessage("Master!",jsonObj.get("data").getAsJsonObject().get("id").getAsString());
        }
      }
    }
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
  
  public static RoomConnection createConnection(String room) {
    String destUri = "wss://euphoria.io/room/"+room+"/ws";
    SslContextFactory sslContextFactory = new SslContextFactory();
    sslContextFactory.setTrustAll(true);
    WebSocketClient client = new WebSocketClient(sslContextFactory);
    RoomConnection socket = new RoomConnection();
    try {
      client.start();
      URI echoUri = new URI(destUri);
      ClientUpgradeRequest request = new ClientUpgradeRequest();
      client.connect(socket, echoUri, request);
      System.out.printf("Connecting to : %s%n", echoUri);
      socket.awaitClose(60, TimeUnit.SECONDS);
    } catch (Throwable t) {
      t.printStackTrace();
    } finally {
      try {
        client.stop();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return socket;
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
  
  public int sendMessage(String message, String parentID) {
    JsonObject send = new JsonObject();
    send.addProperty("type", "send");
    JsonObject data = new JsonObject();
    data.addProperty("content",message);
    data.addProperty("parent",parentID);
    send.add("data", data);
    sendServerMessage(send);
    return 0;
  }
  public int sendMessage(String message) {
    JsonObject send = new JsonObject();
    send.addProperty("type", "send");
    JsonObject data = new JsonObject();
    data.addProperty("content",message);
    send.add("data", data);
    sendServerMessage(send);
    return 0;
  }
  
  public int changeNick(String nick) {
    JsonObject nickReq = new JsonObject();
    nickReq.addProperty("type", "nick");
    JsonObject data = new JsonObject();
    data.addProperty("name",nick);
    nickReq.add("data", data);
    sendServerMessage(nickReq);
    return 0;
  }
}
