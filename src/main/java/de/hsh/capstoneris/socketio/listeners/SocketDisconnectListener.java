package de.hsh.capstoneris.socketio.listeners;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DisconnectListener;
import de.hsh.capstoneris.rest.json.JsonUser;
import de.hsh.capstoneris.socketio.*;
import de.hsh.capstoneris.socketio.messages.server.MemberListUpdateMessage;
import de.hsh.capstoneris.socketio.messages.server.SessionLeftMessage;
import de.hsh.capstoneris.util.ConsoleColors;
import de.hsh.capstoneris.util.Logger;
import de.hsh.capstoneris.util.Service;

import java.util.ArrayList;

public class SocketDisconnectListener implements DisconnectListener {
    private final Manager manager;
    private final SocketIOServer socketIOServer;

    public SocketDisconnectListener(Manager manager, SocketIOServer socketIOServer) {
        super();
        this.socketIOServer = socketIOServer;
        this.manager = manager;
    }


    @Override
    public void onDisconnect(SocketIOClient socketIOClient) {
        // IMPORTANT: Clients get removed from rooms before this method is called!!!
        Logger.log(Service.SOCKET, "Client disconnecting (" + socketIOClient.getSessionId() + ") from " + socketIOClient.getRemoteAddress(), ConsoleColors.YELLOW);
        Logger.log(Service.SOCKET, "Checking if user was logged in");
        User user = manager.getUserBySessionIdIfExist(socketIOClient.getSessionId());

        // Only if user is actually logged in
        if (user != null) {
            Logger.log(Service.SOCKET, "User was logged in. Cleaning up...");
            if (user.getState() == State.HOSTING) {
                Logger.log(Service.SOCKET, "User was hosting a session. Ending session...");
                SharedSession currentSession = user.getCurrentSession();
                currentSession.end();
                ArrayList<User> joinedUsers = (ArrayList<User>) currentSession.getJoinedUsers().clone();
                ArrayList<User> invitedUsers = currentSession.getInvitedUsers();
                socketIOServer.getRoomOperations(currentSession.getRoom().getName()).sendEvent(SocketMessageTypes.SESSION_LEFT, new SessionLeftMessage("The Host disconnected"));

                for (User next : joinedUsers) {
                    if (!next.equals(user)) {
                        currentSession.kick(next);
                        SocketIOClient client = socketIOServer.getClient(next.getSessionID());
                        client.leaveRoom(currentSession.getRoom().getName());
                    }
                }

                Logger.log(Service.SOCKET, "Removing remaining session invitations");
                for (User invited : invitedUsers) {
                    invited.removeInvite(currentSession);
                    manager.sendInvitationListUpdate(invited, socketIOServer);
                }

                // TODO SAVE SESSION INFORMATION TO THE DATABASE (CHAT HISTORY ETC)
            } else if (user.getState() == State.JOINED) {
                Logger.log(Service.SOCKET, "User joined a session. Leaving session...");
                SharedSession currentSession = user.getCurrentSession();
                currentSession.kick(user);

                ArrayList<JsonUser> joinedUsers = new ArrayList<>();
                for (User u : currentSession.getJoinedUsers()) {
                    joinedUsers.add(new JsonUser(u));
                }

                socketIOClient.sendEvent(SocketMessageTypes.SESSION_LEFT, new SessionLeftMessage("You disconnected"));
                socketIOServer.getRoomOperations(currentSession.getRoom().getName()).sendEvent(SocketMessageTypes.MEMBER_LIST_UPDATE, new MemberListUpdateMessage(joinedUsers));
            }
            Logger.log(Service.SOCKET, "Disconnect complete", ConsoleColors.GREEN);
            user.setSessionID(null);
            user.setState(State.OFFLINE);

        }

    }


}
