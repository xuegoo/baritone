/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.behavior;

import baritone.Baritone;
import baritone.api.event.events.ChatEvent;
import baritone.api.event.events.TickEvent;
import baritone.utils.Helper;
import comms.*;
import comms.downward.MessageChat;
import comms.upward.MessageStatus;

import java.io.IOException;
import java.util.List;

public class ControllerBehavior extends Behavior implements IMessageListener {
    public ControllerBehavior(Baritone baritone) {
        super(baritone);
    }

    private BufferedConnection<SerializableMessage> conn;

    @Override
    public void onTick(TickEvent event) {
        if (event.getType() == TickEvent.Type.OUT) {
            return;
        }
        trySend(new MessageStatus(ctx.player().posX, ctx.player().posY, ctx.player().posZ));
    }

    private void readAndHandle() {
        if (conn == null) {
            return;
        }
        try {
            List<SerializableMessage> msgs = conn.receiveMessagesNonBlocking();
            msgs.forEach(msg -> ((HandlableMessage) msg).handle(this));
        } catch (IOException e) {
            e.printStackTrace();
            disconnect();
        }
    }

    public boolean trySend(SerializableMessage msg) {
        if (conn == null) {
            return false;
        }
        try {
            conn.sendMessage(msg);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            disconnect();
            return false;
        }
    }

    public void connectTo(IConnection conn) {
        disconnect();
        if (conn instanceof BufferedConnection) {
            this.conn = (BufferedConnection) conn;
        } else {
            this.conn = new BufferedConnection(conn);
        }
    }

    public void disconnect() {
        if (conn != null) {
            conn.close();
        }
        conn = null;
    }

    @Override
    public void handle(MessageChat msg) { // big brain
        ChatEvent event = new ChatEvent(ctx.player(), msg.msg);
        baritone.getGameEventHandler().onSendChatMessage(event);
    }

    @Override
    public void unhandled(iMessage msg) {
        Helper.HELPER.logDebug("Unhandled message received by ControllerBehavior " + msg);
    }
}