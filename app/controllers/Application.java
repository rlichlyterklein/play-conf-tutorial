package controllers;

import actors.EventPublisher;
import actors.messages.CloseConnectionEvent;
import actors.messages.NewConnectionEvent;
import actors.messages.NewProposalEvent;
import actors.messages.UserRegistrationEvent;
import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;

import external.services.OAuthService;
import external.services.TwitterOAuthService;
import models.Proposal;
import models.RegisteredUser;
import play.Play;
import play.data.Form;
import play.libs.F;
import play.libs.F.Callback;
import play.libs.F.Promise;
import play.libs.F.Tuple;
import play.libs.OAuth.RequestToken;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.WebSocket;
import views.html.index;

import java.util.UUID;

public class Application extends Controller {

    public static final Form<Proposal> PROPOSAL_FORM = Form.form(Proposal.class);

    public static final OAuthService twitterOAuthService = new TwitterOAuthService(
    		Play.application().configuration().getString("twitter.consumer.key"),
    		Play.application().configuration().getString("twitter.consumer.secret"));
    
    public static Result register(){
    	String callbackUrl = routes.Application.registerCallback().absoluteURL(request());
    	Tuple<String,RequestToken> requestToken = twitterOAuthService.retrieveRequestToken(callbackUrl);
    	flash("request_token",requestToken._2.token);
    	flash("request_secret", requestToken._2.secret);
    	return redirect(requestToken._1);
    }
    
    public static Result registerCallback(){
    	RequestToken token = new RequestToken(flash("request_token"), flash("request_secret"));
    	String authVerifier = request().getQueryString("oauth_verifier");
    	Promise<JsonNode> registerUserProfile = twitterOAuthService.registerUserProfile(token, authVerifier);
    	registerUserProfile.onRedeem(new Callback<JsonNode>(){

			@Override
			public void invoke(JsonNode a) throws Throwable {
				RegisteredUser user = RegisteredUser.fromJson(a);
				user.save();
				EventPublisher.ref.tell(new UserRegistrationEvent(user), ActorRef.noSender());
			}	
    	});
    	
    	return redirect(routes.Application.index());
    }
    
    public static F.Promise<Result> index() {
        F.Promise<Proposal> proposal = Proposal.findKeynote();
        F.Promise<Result> map = proposal.map(new F.Function<Proposal, Result>() {
            @Override
            public Result apply(Proposal proposal) throws Throwable {
                return ok(index.render(proposal));
            }
        });
        return map;
    }

    public static Result newProposal() {
        return ok(views.html.newProposal.render(PROPOSAL_FORM))
;    }

    public static F.Promise<Result> submit() {
        Form<Proposal> submittedForm = PROPOSAL_FORM.bindFromRequest();
        if(submittedForm.hasErrors()) {
            return F.Promise.<Result>pure(ok(views.html.newProposal.render(submittedForm)));
        } else {
            final Proposal proposal = submittedForm.get();
            F.Promise<Result> r = proposal.asyncSave().map(new F.Function<Void, Result>(){
                @Override
                public Result apply(Void arg0) throws Throwable {
                    flash ("message", "Thanks for submitting a proposal");
                    EventPublisher.ref.tell(new NewProposalEvent(proposal), ActorRef.noSender());
                    return redirect(routes.Application.index());
                }
            });
            return r;
        }
    }

    public static WebSocket<JsonNode> buzz() {
        return new WebSocket<JsonNode>(){

            @Override
            public void onReady(In<JsonNode> in, Out<JsonNode> out) {
                final String uuid = UUID.randomUUID().toString();
                EventPublisher.ref.tell(new NewConnectionEvent(uuid, out), ActorRef.noSender());
                in.onClose(new F.Callback0() {
                    @Override
                    public void invoke() throws Throwable {
                        EventPublisher.ref.tell(new CloseConnectionEvent(uuid), ActorRef.noSender());

                    }
                });
            }
        };
    }
}
