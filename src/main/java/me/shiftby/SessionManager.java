package me.shiftby;

import me.shiftby.entity.User;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public class SessionManager {

    private static volatile SessionManager instance;
    private SessionManager() {}
    public static SessionManager getInstance() {
        SessionManager localInstance = instance;
        if (localInstance == null) {
            synchronized (SessionManager.class) {
                localInstance = instance = new SessionManager();
            }
        }
        return localInstance;
    }

    private HashMap<String, Session> sessions = new HashMap<>();

    public Session createSession(Socket socket, User user) throws IOException {
        Session session = new Session(socket, user);
        sessions.put(user.getUsername(), session);
        return session;
    }
    public void stopSession(Session session) throws IOException {
        sessions.remove(session.getUser().getUsername());
        session.stopSession();
    }

    public Session getByUsername(String username) {
        return sessions.get(username);
    }

    public void broadcast(String message, User from) {
        sessions.forEach((login, session) -> {
            if (session.getUser() != from) {
                try {
                    session.send(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void close() {
        sessions.forEach((username, session) -> {
            try {
                stopSession(session);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}