<<<<<<< HEAD:src/main/java/de/hsh/capstoneris/socketMessages/client/KickMemberMessage.java
package de.hsh.capstoneris.socketMessages.client;

public class KickMemberMessage implements ClientMessage {
    public KickMemberMessage() {
    }
=======
package de.hsh.capstoneris.socket.messages.client;
>>>>>>> master:src/main/java/de/hsh/capstoneris/socket/messages/client/KickMemberMessage.java

public class KickMemberMessage implements ClientMessage {
    /* TODO kick-member
    → Server entfernt von allen anderen Feldern, die von diesem Mitglied fokussiert sind, den Fokus und schickt dann ggf. Ein inputfield-changed für diese Felder an alle Mitglieder (außer dieses)
    → Server setzt Nutzerzustand zurück
    → Server sendet session-leaved an Mitglied
    → Server sendet member-list-update an alle Mitglieder (auch den Host)
            */
}