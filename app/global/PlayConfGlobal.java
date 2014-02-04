package global;

import java.util.concurrent.TimeUnit;

import actors.EventPublisher;
import actors.messages.RandomlySelectedTalkEvent;
import akka.actor.ActorRef;

import models.Proposal;

import play.Application;
import play.GlobalSettings;
import play.Play;
import play.libs.Akka;
import play.libs.F;
import play.libs.F.Callback;
import play.libs.F.Promise;
import play.mvc.Http;
import play.mvc.Results;
import play.mvc.SimpleResult;
import scala.concurrent.duration.Duration;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;

import external.services.OAuthService;
import external.services.TwitterOAuthService;

/**
 * Created by lichlyterklein on 12/24/13.
 */
public class PlayConfGlobal extends GlobalSettings {

	public Injector injector = Guice.createInjector(new AbstractModule(){

		@Override
		protected void configure() {
			// TODO Auto-generated method stub
			bind(ActorRef.class).toProvider(new Provider<ActorRef>(){

				@Override
				public ActorRef get() {
					// TODO Auto-generated method stub
					return EventPublisher.ref;
				}
								
			});
			
			bind(OAuthService.class).toProvider(new Provider<OAuthService>(){

				@Override
				public OAuthService get() {
					// TODO Auto-generated method stub
					return new TwitterOAuthService(
							Play.application().configuration().getString("twitter.consumer.key"),
				    		Play.application().configuration().getString("twitter.consumer.secret"));
				}
				
			});
			
		}
		
	});
	
	@Override
	public <A> A getControllerInstance(Class<A> clazz) throws Exception{
		return injector.getInstance(clazz);
	}
	
	@Override
	public void onStart(Application app) {
		// TODO Auto-generated method stub
		super.onStart(app);
		Akka.system().scheduler().schedule(Duration.create(1,TimeUnit.SECONDS),
				Duration.create(10, TimeUnit.SECONDS), 
				selectRandomTalks(),
				Akka.system().dispatcher());
	}

    private Runnable selectRandomTalks() {
		return new Runnable(){
			@Override
			public void run() {
				Promise<Proposal> proposal = Proposal.selectRandomTalks();
				proposal.onRedeem(new Callback<Proposal>(){
					@Override
					public void invoke(Proposal a) throws Throwable {
						EventPublisher.ref.tell(new RandomlySelectedTalkEvent(a), ActorRef.noSender());
					}
				});
			}
		};
	}

	@Override
    public F.Promise<SimpleResult> onHandlerNotFound(Http.RequestHeader request) {
        return F.Promise.<SimpleResult>pure(Results.notFound(views.html.error.render()));
    }

    @Override
    public F.Promise<SimpleResult> onError(Http.RequestHeader request, Throwable t) {
        return F.Promise.<SimpleResult>pure(Results.internalServerError(views.html.error.render()));
    }
}
