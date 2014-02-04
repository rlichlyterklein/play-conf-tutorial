package actors.messages;

import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.WebSocket;

/**
 * Created by lichlyterklein on 12/29/13.
 */
public class NewConnectionEvent {


    private final String uuid;
    private final WebSocket.Out<JsonNode> out;

    public NewConnectionEvent(String uuid, WebSocket.Out<JsonNode> out) {
        this.uuid = uuid;
        this.out = out;
    }

    public String uuid() {
        return uuid;
    }

    public WebSocket.Out<JsonNode> out() {
        return out;
    }
}
