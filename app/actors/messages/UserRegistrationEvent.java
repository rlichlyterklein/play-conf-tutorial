package actors.messages;

import play.libs.Json;
import models.RegisteredUser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class UserRegistrationEvent implements UserEvent {

	private RegisteredUser registeredUser;

	public UserRegistrationEvent(RegisteredUser registeredUser){
		this.registeredUser = registeredUser;
	}

	@Override
	public JsonNode json() {
        ObjectNode result = Json.newObject();
        result.put("messageType","registeredUser");
        result.put("name", registeredUser.name);
        result.put("twitterId", registeredUser.twitterId);
        result.put("description", registeredUser.description);
        result.put("pictureUrl", registeredUser.pictureUrl);
        return result;
	}

}
