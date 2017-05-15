/**
 *  Copyright (C) 2002-2017   The FreeCol Team
 *
 *  This file is part of FreeCol.
 *
 *  FreeCol is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.freecol.common.networking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.stream.XMLStreamException;

import net.sf.freecol.client.FreeColClient;
import net.sf.freecol.common.io.FreeColXMLReader;
import net.sf.freecol.common.io.FreeColXMLWriter;
import net.sf.freecol.common.io.FreeColXMLWriter.WriteScope;
import net.sf.freecol.common.model.FreeColGameObject;
import net.sf.freecol.common.model.FreeColObject;
import net.sf.freecol.common.model.Game;
import net.sf.freecol.common.model.Player;
import net.sf.freecol.server.FreeColServer;
import net.sf.freecol.server.ai.AIPlayer;
import net.sf.freecol.server.model.ServerPlayer;

import net.sf.freecol.common.util.DOMUtils;
import org.w3c.dom.Element;


/**
 * The message sent by the server to partially update client game objects.
 */
public class PartialMessage extends AttributeMessage {

    public static final String TAG = "partial";
    // ChangeSet needs to see this
    public static final String ID_TAG = FreeColObject.ID_ATTRIBUTE_TAG;


    /**
     * Create a new {@code PartialMessage}.
     *
     * @param map A map of key,value pairs to update.
     */
    public PartialMessage(Map<String,String> map) {
        super(TAG, map);
    }

    /**
     * Create a new {@code PartialMessage} from a supplied
     * element.
     *
     * @param game The {@code Game} this message belongs to.
     * @param element The {@code Element} to use to create the message.
     */
    public PartialMessage(Game game, Element element) {
        super(TAG, DOMUtils.getAttributeMap(element));
    }


    /**
     * Get the object to update.
     *
     * @param game The {@code Game} to look for the object in.
     * @return The {@code FreeColGameObject} found.
     */
    private FreeColGameObject getObject(Game game) {
        return game.getFreeColGameObject(getStringAttribute(ID_TAG));
    }

    /**
     * Get the attribute map for this message.
     *
     * @return The attribute map.
     */
    private Map<String, String> getAttributeMap() {
        return this.attributes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessagePriority getPriority() {
        return Message.MessagePriority.UPDATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void aiHandler(FreeColServer freeColServer, AIPlayer aiPlayer) {
        // Ignored
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clientHandler(FreeColClient freeColClient) {
        final Game game = freeColClient.getGame();
        final String id = getStringAttribute(ID_TAG);

        if (id == null) {
            logger.warning("Partial update is missing identifier attribute");
            return;
        }
        FreeColGameObject fcgo = game.getFreeColGameObject(id);
        if (fcgo == null) {
            logger.warning("Partial update of missing object: " + id);
            return;
        }
                
        if (freeColClient.isInGame()) {
            igc(freeColClient).partialHandler(fcgo, getAttributeMap());
        } else {
            logger.warning("Partial update when not in game.");
        }
    }
}
