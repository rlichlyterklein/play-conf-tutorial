package actors;

import actors.messages.CloseConnectionEvent;
import actors.messages.NewConnectionEvent;
import actors.messages.UserEvent;

import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;
import play.libs.Akka;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import play.mvc.WebSocket;
import play.mvc.WebSocket.Out;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lichlyterklein on 12/29/13.
 */
public class EventPublisher extends UntypedActor {

    public static ActorRef ref = Akka.system().actorOf(Props.create(EventPublisher.class));

    private Map<String, WebSocket.Out<JsonNode>> connections = new HashMap<>();

    @Override
    public void onReceive(Object message) throws Exception {

        if(message instanceof NewConnectionEvent){
            final NewConnectionEvent newConnectionEvent = (NewConnectionEvent) message;
            connections.put(newConnectionEvent.uuid(),newConnectionEvent.out());
            Logger.info("new browser connected " + newConnectionEvent.uuid());
        } else if( message instanceof CloseConnectionEvent ){
            final CloseConnectionEvent closeConnectionEvent = (CloseConnectionEvent) message;
            final String uuid = closeConnectionEvent.uuid();
            connections.remove(uuid);
            Logger.info("Browser "+ uuid + " is disconnected");
        } else if( message instanceof UserEvent ){
        	broadcastMessage((UserEvent) message);
        	
        } else {
            unhandled(message);
        }

    }

	private void broadcastMessage(UserEvent message) {
		for(Out<JsonNode> out: connections.values()){
			out.write(message.json());
		}
	}
}
