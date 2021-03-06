package de.hsh.capstoneris.socketio;

import com.corundumstudio.socketio.SocketIOServer;
import de.hsh.capstoneris.rest.json.JsonInvitation;
import de.hsh.capstoneris.rest.json.JsonUser;
import de.hsh.capstoneris.socketio.messages.server.InvitationListUpdateMessage;
import de.hsh.capstoneris.util.Logger;
import de.hsh.capstoneris.util.Service;

import java.util.ArrayList;
import java.util.UUID;

public class Manager {
    private final ArrayList<SharedSession> sessions;
    private final ArrayList<User> users;

    public Manager() {
        sessions = new ArrayList<>();
        users = new ArrayList<>();
    }

    public void addSession(SharedSession session) {
        if (!sessions.contains(session)) {
            sessions.add(session);
        }
    }

    public void removeSession(SharedSession session) {
        if (sessions.contains(session)) {
            sessions.remove(session);
        }
    }

    public ArrayList<SharedSession> getSessions() {
        return sessions;
    }

    public void addUser(User user) {
        if (!users.contains(user)) {
            users.add(user);
        }
    }

    public User getUserByNameIfExist(String name) {
        User user = null;
        for (User u : users) {
            if (u.getUsername().equals(name)) {
                user = u;
                break;
            }
        }
        return user;
    }

    public User getUserBySessionIdIfExist(UUID sessionID) {
        User user = null;
        for (User u : users) {
            if (sessionID.equals(u.getSessionID()) && u.getState() != State.OFFLINE) {
                user = u;
                break;
            }
        }
        return user;
    }

    public boolean isLoggedIn(User user) {
        return users.contains(user) && user.getState() != State.OFFLINE;
    }

    public boolean isLoggedInBySessionID(UUID sessionID) {
        User user = getUserBySessionIdIfExist(sessionID);
        return user != null;
    }

    public void sendInvitationListUpdate(User user, SocketIOServer socketIOServer) {
        if (user.getState() != State.OFFLINE) {
            ArrayList<JsonInvitation> invites = new ArrayList<>();
            for (SharedSession sessions : user.getInvitedTo()) {
                invites.add(new JsonInvitation(new JsonUser(sessions.getHost()), sessions.getInviteMessage(), sessions.getTimeStamp()));
            }
            Logger.log(Service.SOCKET, "Updating user " + user.getUsername() + " invitation list. Now invited to " + invites.size() + " session(s)");
            socketIOServer.getClient(user.getSessionID()).sendEvent(SocketMessageTypes.INVITATION_LIST_UPDATE, new InvitationListUpdateMessage(invites));
        }
    }

}
