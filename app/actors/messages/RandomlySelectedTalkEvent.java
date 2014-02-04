package actors.messages;

import models.Proposal;
import play.libs.Json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RandomlySelectedTalkEvent implements UserEvent {

	private Proposal proposal;

	public RandomlySelectedTalkEvent(Proposal proposal) {
		this.proposal = proposal;
	}
	
    @Override
    public JsonNode json() {
        ObjectNode result = Json.newObject();
        result.put("messageType","proposalSubmission");
        result.put("speakerName", proposal.speaker.name);
        result.put("twitterId", proposal.speaker.twitterId);
        result.put("title", proposal.title);
        result.put("proposal", proposal.proposal);
        result.put("pictureUrl", proposal.speaker.pictureUrl);
        return result;
    }

}
