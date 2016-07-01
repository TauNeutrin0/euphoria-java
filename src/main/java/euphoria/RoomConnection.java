package euphoria;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.event.EventListenerList;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import euphoria.events.ConnectionEvent;
import euphoria.events.ConnectionEventListener;
import euphoria.events.MessageEvent;
import euphoria.events.PacketEvent;
import euphoria.events.PacketEventListener;
import euphoria.events.PausedEventListener;
import euphoria.events.ReplyEventListener;
import euphoria.packets.DataPacket;
import euphoria.packets.StandardPacket;
import euphoria.packets.commands.Auth;
import euphoria.packets.commands.Nick;
import euphoria.packets.commands.Send;
import euphoria.packets.events.PingEvent;
import euphoria.packets.replies.AuthReply;
import euphoria.packets.replies.NickReply;

@WebSocket
public class RoomConnection implements Runnable {
    private Thread                  initThread;
    private Session                 session;
    private WebSocketClient         client;
    private Calendar                startupTime;
    public EventListenerList        listeners      = new EventListenerList();
    private EventListenerList       sharedListeners;
    private PausedEventListener     pauseListener  = new PausedEventListener(this);
    Map<String, ReplyEventListener> replyListeners = new HashMap<String, ReplyEventListener>();
    private String                  nextId         = "0";
    private final Object            idLock         = new Object();
    private boolean                 isPaused       = false;
    private String                  room;
    private String                  currNick;
    private List<HttpCookie>        cookies        = new ArrayList<HttpCookie>();
    private String                  password;
    private boolean                 isBounced      = false;
    private ReplyEventListener      passwordListener;

    public RoomConnection(String room) {
        this.room = room;
    }

    public void run() {
        String destUri = "wss://euphoria.io/room/" + room + "/ws";
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setTrustAll(true);
        client = new WebSocketClient(sslContextFactory);
        initThread = Thread.currentThread();
        try {
            URI echoUri = new URI(destUri);
            try {
                client.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setCookies(cookies);
            client.connect(this, echoUri, request);
            System.out.printf("Connecting to : %s%n", echoUri);
            Thread.sleep(20000);
            try {
                client.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Could not connect to " + room + ".");
            Object[] lns = listeners.getListenerList();
            ConnectionEvent evt = new ConnectionEvent(this, room);
            for (int i = 0; i < lns.length; i = i + 2) {
                if (lns[i] == ConnectionEventListener.class) {
                    ((ConnectionEventListener)lns[i + 1]).onConnectionError(evt);
                }
            }
            lns = sharedListeners.getListenerList();
            for (int i = 0; i < lns.length; i = i + 2) {
                if (lns[i] == ConnectionEventListener.class) {
                    ((ConnectionEventListener)lns[i + 1]).onConnectionError(evt);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection(String reason) {
        session.close(StatusCode.NORMAL, reason);
        try {
            client.stop();
        } catch (Exception e) {
            System.out.println("Caught exception at connection close.");
        }
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.printf("Connection to " + room + " closed: %d - %s%n", statusCode, reason);
        this.session = null;
        Object[] lns = listeners.getListenerList();
        ConnectionEvent evt = new ConnectionEvent(this, room);
        for (int i = 0; i < lns.length; i = i + 2) {
            if (lns[i] == ConnectionEventListener.class) {
                ((ConnectionEventListener)lns[i + 1]).onDisconnect(evt);
            }
        }
        lns = sharedListeners.getListenerList();
        for (int i = 0; i < lns.length; i = i + 2) {
            if (lns[i] == ConnectionEventListener.class) {
                ((ConnectionEventListener)lns[i + 1]).onDisconnect(evt);
            }
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("Connected to " + room + "!");
        this.session = session;
        session.getPolicy().setMaxTextMessageSize(1024 * 1024);
        cookies = HttpCookie.parse(session.getUpgradeResponse().getHeader("Set-Cookie"));
        initThread.interrupt();
        Object[] lns = listeners.getListenerList();
        ConnectionEvent evt = new ConnectionEvent(this, room);
        for (int i = 0; i < lns.length; i = i + 2) {
            if (lns[i] == ConnectionEventListener.class) {
                ((ConnectionEventListener)lns[i + 1]).onConnect(evt);
            }
        }
        lns = sharedListeners.getListenerList();
        for (int i = 0; i < lns.length; i = i + 2) {
            if (lns[i] == ConnectionEventListener.class) {
                ((ConnectionEventListener)lns[i + 1]).onConnect(evt);
            }
        }
        startupTime = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
    }

    @OnWebSocketMessage
    public void onMessage(String msg) {
        JsonObject jsonObj = new JsonParser().parse(msg).getAsJsonObject();
        try {
            StandardPacket packet = createPacketFromJson(jsonObj);
            if (packet.getData() == null) {
                Object object = new Object();
                Class< ? > clazz;
                try {
                    clazz = DataPacket.typeToClass(packet.getType());
                    Constructor< ? > ctor = clazz.getConstructor();
                    object = ctor.newInstance();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                packet.setData((DataPacket)object);
            }
            if (isPaused) {
                if (packet.getType().equals("send-event")) {
                    pauseListener.onSendEvent(new MessageEvent(this, packet));
                }
            }
            if (packet.getType().equals("ping-event")) {
                ((PingEvent)packet.getData()).handle(this);
            } else if (packet.getType().equals("nick-reply")) {
                currNick = ((NickReply)packet.getData()).getTo();
            } else if (packet.getType().equals("bounce-event")) {
                if (password != null) {
                    if (passwordListener == null)
                        sendPacket(Auth.createPasswordAttempt(password));
                    else
                        sendPacket(Auth.createPasswordAttempt(password), passwordListener);
                } else {
                    isBounced = true;
                }
            }
            PacketEvent evt = new PacketEvent(this, packet);


            Object[] lns = sharedListeners.getListenerList();
            for (int i = 0; i < lns.length; i = i + 2) {
                if (lns[i] == PacketEventListener.class && !isPaused) {
                    if (packet.getType().equals("send-event")) {
                        ((PacketEventListener)lns[i + 1]).onSendEvent(new MessageEvent(this, packet));
                    } else {
                        java.lang.reflect.Method method;
                        try {
                            method =
                                     ((PacketEventListener)lns[i + 1]).getClass()
                                                                      .getMethod("on" + packet.getData().getClass().getSimpleName(),
                                                                                 PacketEvent.class);
                            if (!method.isAccessible()) {
                                method.setAccessible(true);
                            }
                            method.invoke(((PacketEventListener)lns[i + 1]), evt);
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        } catch (NoSuchMethodException e) {
                            //System.out.println("No handler provided for "+packet.getType()+".");
                        }
                    }
                }
            }


            lns = listeners.getListenerList();
            for (int i = 0; i < lns.length; i = i + 2) {
                if (lns[i] == PacketEventListener.class && !isPaused) {
                    if (packet.getType().equals("send-event")) {
                        ((PacketEventListener)lns[i + 1]).onSendEvent(new MessageEvent(this, packet));
                    } else {
                        java.lang.reflect.Method method;
                        try {
                            method =
                                     ((PacketEventListener)lns[i + 1]).getClass()
                                                                      .getMethod("on" + packet.getData().getClass().getSimpleName(),
                                                                                 PacketEvent.class);
                            if (!method.isAccessible()) {
                                method.setAccessible(true);
                            }
                            method.invoke(((PacketEventListener)lns[i + 1]), evt);
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.getCause().printStackTrace();
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        } catch (NoSuchMethodException e) {
                            //System.out.println("No handler provided for "+packet.getType()+".");
                        }
                    }
                }
            }

            if (packet.getType().matches("\\S*-reply$")) {
                if (packet.getId() != null) {
                    if (replyListeners.containsKey(packet.getId())) {
                        ReplyEventListener evtLst = replyListeners.get(packet.getId());
                        evtLst.onReplyEvent(evt);
                        if (packet.getError() == null) {
                            evtLst.onReplySuccess(evt);
                        } else {
                            evtLst.onReplyFail(evt);
                        }
                        replyListeners.remove(packet.getId());
                    }
                }
            }
        } catch (JsonParseException e) {
            //System.out.println("Could not recognise type.");
        }

    }

    public void setSharedListeners(EventListenerList listeners) {
        sharedListeners = listeners;
    }

    public void setCookies(List<HttpCookie> cookies) {
        this.cookies = cookies;
    }

    public void setCookies(String cookies) {
        this.cookies = HttpCookie.parse(cookies);
    }

    public void setCustomPauseListener(PausedEventListener listener) {
        pauseListener = listener;
    }

    public void pause(String nick) {
        changeNick(nick);
        pauseListener.setNick(nick);
        isPaused = true;
    }

    public void unpause() {
        isPaused = false;
    }

    public void tryPassword(String pw) {
        if (isBounced) {
            sendPacket(Auth.createPasswordAttempt(pw), new ReplyEventListener() {

                @Override
                public void onReplyEvent(PacketEvent evt) {
                    if (((AuthReply)evt.getPacket().getData()).getSuccess()) {
                        isBounced = false;
                    }
                }

                @Override
                public void onReplyFail(PacketEvent arg0) {
                }

                @Override
                public void onReplySuccess(PacketEvent arg0) {
                }
            });
        } else {
            password = pw;
        }
    }

    public void tryPassword(String pw, ReplyEventListener eL) {
        if (isBounced) {
            final ReplyEventListener passwordListener = eL;
            sendPacket(Auth.createPasswordAttempt(pw), new ReplyEventListener() {

                @Override
                public void onReplyEvent(PacketEvent evt) {
                    passwordListener.onReplyEvent(evt);
                    if (((AuthReply)evt.getPacket().getData()).getSuccess()) {
                        isBounced = false;
                        passwordListener.onReplySuccess(evt);
                    } else
                        passwordListener.onReplyFail(evt);
                }

                @Override
                public void onReplyFail(PacketEvent arg0) {
                }

                @Override
                public void onReplySuccess(PacketEvent arg0) {
                }
            });
        } else {
            password = pw;
            passwordListener = eL;
        }
    }

    public void sendPacket(String message) {
        try {
            Future<Void> fut;
            fut = session.getRemote().sendStringByFuture(message);
            fut.get(2, TimeUnit.SECONDS);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void sendPacket(JsonObject message) {
        Gson gson = new Gson();
        try {
            Future<Void> fut;
            fut = session.getRemote().sendStringByFuture(gson.toJson(message));
            fut.get(2, TimeUnit.SECONDS);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void sendPacketRaw(StandardPacket pckt) {
        Gson gson = new Gson();
        try {
            Future<Void> fut;
            fut = session.getRemote().sendStringByFuture(gson.toJson(pckt));
            fut.get(2, TimeUnit.SECONDS);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public String sendPacket(StandardPacket pckt) {
        return sendPacket(pckt,null);
    }

    public String sendPacket(StandardPacket pckt, ReplyEventListener l) {
        synchronized(idLock){
            nextId = Long.toString(Long.valueOf(nextId, 36) + 1, 36);
            pckt.setId(nextId);
            if(l!=null)replyListeners.put(nextId, l);
            sendPacketRaw(pckt);
        }
        return nextId;
    }

    private StandardPacket createPacketFromJson(JsonObject json) {
        GsonBuilder gsonBilder = new GsonBuilder();
        gsonBilder.registerTypeAdapter(StandardPacket.class, new DataAdapter());
        Gson gson = gsonBilder.create();
        StandardPacket packet = gson.fromJson(json, StandardPacket.class);
        return packet;
    }

    private String createJsonFromPacket(StandardPacket pckt) {
        Gson gson = new Gson();
        String json = gson.toJson(pckt);
        return json;
    }

    public void sendMessage(String message) {
        sendPacket(new Send(message).createPacket());
    }

    public void sendMessage(String message, String replyId) {
        sendPacket(new Send(message, replyId).createPacket());
    }

    public void changeNick(String nick) {
        sendPacket(new Nick(nick).createPacket());
    }

    public String getNick() {
        return currNick;
    }

    public String getRoom() {
        return room;
    }

    public Calendar getStartupTime() {
        return startupTime;
    }

    public List<HttpCookie> getCookies() {
        return cookies;
    }

    public String getCookiesAsString() {
        return cookies.toString();
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isBounced() {
        return isBounced;
    }
}
