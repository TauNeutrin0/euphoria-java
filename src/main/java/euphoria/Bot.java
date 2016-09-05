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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.swing.event.EventListenerList;

public abstract class Bot {
    private Map<String, RoomConnection> connectionsMap = new HashMap<String, RoomConnection>();
    private Map<String, RoomConnection> pendingConnectionsMap = new HashMap<String, RoomConnection>();
    public final Object          connectionLock     = new Object();
    public EventListenerList     listeners          = new EventListenerList();
    private static Console       console            = null;
    private boolean              usesCookies        = false;
    private FileIO               cookieFile;
    private Calendar             startupTime;

    public Bot() {
        if (console == null)
            Bot.initConsole();
        if (console != null)
            console.addWindowListener(new WindowListener() {
                @Override
                public void windowClosing(WindowEvent arg0) {
                    System.out.println("Closing connections...");
                    Bot.this.closeAll();
                    System.exit(0);
                }

                public void windowActivated(WindowEvent arg0) {
                }

                public void windowClosed(WindowEvent arg0) {
                }

                public void windowDeactivated(WindowEvent arg0) {
                }

                public void windowDeiconified(WindowEvent arg0) {
                }

                public void windowIconified(WindowEvent arg0) {
                }

                public void windowOpened(WindowEvent arg0) {
                }
            });
        startupTime = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
    }
    
    // Get a connection that is connected.
    public RoomConnection getRoomConnection(String room) throws RoomNotConnectedException {
        synchronized (connectionLock) {
            if(connectionsMap.containsKey(room)){
                return connectionsMap.get(room);  
            }
        }
        throw new RoomNotConnectedException();
    }

    // Get connection that is either pending or connected.
    public RoomConnection getAnyRoomConnection(String room) throws RoomNotConnectedException {
        synchronized (connectionLock) {
            if(connectionsMap.containsKey(room)){
                return connectionsMap.get(room);  
            } else if(pendingConnectionsMap.containsKey(room)){
                return pendingConnectionsMap.get(room);  
            }
        }
        throw new RoomNotConnectedException();
    }

    public void connectRoom(String roomName) {
        synchronized (connectionLock) {
            if (!isConnected(roomName) && !isPending(roomName)) {
                RoomConnection connection = new RoomConnection(roomName);
                setupRoomConnection(connection);
                pendingConnectionsMap.put(roomName, connection);
                new Thread(connection).start();
            }
        }

    }

    public void connectRoom(String roomName, ConnectionEventListener eL) {
        synchronized (connectionLock) {
            if (!isConnected(roomName) && !isPending(roomName)) {
                RoomConnection connection = new RoomConnection(roomName);
                setupRoomConnection(connection);
                connection.listeners.add(ConnectionEventListener.class, eL);
                pendingConnectionsMap.put(roomName, connection);
                new Thread(connection).start();
            }
        }
    }

    public RoomConnection createRoomConnection(String roomName) {
        if (isConnected(roomName)) {
            return null;
        } else {
            RoomConnection connection = new RoomConnection(roomName);
            setupRoomConnection(connection);
            return connection;
        }
    }

    public void startRoomConnection(RoomConnection connection) {
        synchronized (connectionLock) {
            pendingConnectionsMap.put(connection.getRoom(), connection);
            new Thread(connection).start();
        }
    }

    private void setupRoomConnection(RoomConnection connection) {
        //Make room add and remove itself to/from connections automatically
        connection.listeners.add(ConnectionEventListener.class, new ConnectionEventListener() {
            @Override
            public void onConnect(ConnectionEvent evt) {
                synchronized (connectionLock) {
                    Bot.this.connectionsMap.put(evt.getRoomName(),evt.getRoomConnection());
                    if (Bot.this.pendingConnectionsMap.containsValue(evt.getRoomConnection())) {
                        Bot.this.pendingConnectionsMap.remove(evt.getRoomName());
                    }
                }
            }

            @Override
            public void onDisconnect(ConnectionEvent evt) {
                synchronized (connectionLock) {
                    Bot.this.connectionsMap.remove(evt.getRoomName());
                }
            }

            @Override
            public void onConnectionError(ConnectionEvent evt) {
                synchronized (connectionLock) {
                    if (Bot.this.pendingConnectionsMap.containsValue(evt.getRoomConnection())) {
                        Bot.this.pendingConnectionsMap.remove(evt.getRoomName());
                    }
                }
            }
        });

        connection.setSharedListeners(listeners); //Set connection's shared listeners

        //Set cookies
        if (usesCookies) {
            if (!cookieFile.getJson().get("cookie").getAsString().isEmpty())
                connection.setCookies(cookieFile.getJson().get("cookie").getAsString());
            connection.listeners.add(ConnectionEventListener.class, new ConnectionEventListener() {
                @Override
                public void onConnect(ConnectionEvent evt) {
                    synchronized (Bot.this.cookieFile) {
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
                public void onConnectionError(ConnectionEvent evt) {
                }

                @Override
                public void onDisconnect(ConnectionEvent evt) {
                }
            });
        }
    }

    public void closeAll() {
        synchronized (connectionLock) {
            for (Map.Entry<String, RoomConnection> entry : connectionsMap.entrySet()) {
                final RoomConnection conn = entry.getValue();
                (new Thread() {public void run() {conn.closeConnection("Program exiting.");}}).start();
            }
        }
    }

    public void disconnectRoom(String roomName) {
        synchronized (connectionLock) {
            final RoomConnection conn = connectionsMap.get(roomName);
            (new Thread() {public void run() {conn.closeConnection("Bot disconnecting...");}}).start();
        }
    }

    public boolean isConnected(String roomName) {
        boolean connected = false;
        synchronized (connectionLock) {
            connected=connectionsMap.containsKey(roomName);
        }
        return connected;
    }

    public boolean isPending(String roomName) {
        boolean pending = false;
        synchronized (connectionLock) {
            pending = pendingConnectionsMap.containsKey(roomName);
        }
        return pending;
    }

    public void useCookies(FileIO cookieFile) {
        this.cookieFile = cookieFile;

        JsonObject data = cookieFile.getJson();
        if (cookieFile.getJson().has("cookie")) {
            if (cookieFile.getJson().get("cookie").isJsonPrimitive()) {
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

    public static void initConsole() {
        System.out.println("Initialising console");
        try {
            console = new euphoria.Console();
        } catch (java.awt.HeadlessException e) {
            System.err.println("Could not find display.");
        }
    }

    public static void addConsoleListener(ConsoleEventListener evtLst) {
        if (console != null) {
            console.addListener(evtLst);
        }
    }

    public static void removeConsoleListener(ConsoleEventListener evtLst) {
        if (console != null) {
            console.removeListener(evtLst);
        }
    }

    public Calendar getStartupTime() {
        return startupTime;
    }
}
