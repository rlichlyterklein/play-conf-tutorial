package actors.messages;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by lichlyterklein on 12/29/13.
 */
public interface UserEvent {

    public JsonNode json();
}
