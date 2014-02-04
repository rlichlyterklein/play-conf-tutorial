package external.services;


import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.F.Tuple;
import play.libs.OAuth;
import play.libs.OAuth.ConsumerKey;
import play.libs.OAuth.OAuthCalculator;
import play.libs.OAuth.RequestToken;
import play.libs.OAuth.ServiceInfo;
import play.libs.WS;
import play.libs.WS.Response;
import play.libs.WS.WSRequestHolder;

import com.fasterxml.jackson.databind.JsonNode;
import com.ning.http.util.Base64;
import common.Functions;


public class TwitterOAuthService implements OAuthService {

	private final String consumerKey;
	private final String consumerSecret;
	private final OAuth oauthHelper;
	private final ConsumerKey key;

	public TwitterOAuthService(String consumerKey, String consumerSecret){
		play.Logger.debug(consumerKey);
		play.Logger.debug(consumerSecret);
		
		this.consumerKey = consumerKey;
		this.consumerSecret = consumerSecret;
        this.key = new ConsumerKey(consumerKey, consumerSecret);
        this.oauthHelper = new OAuth(new ServiceInfo(
                "https://api.twitter.com/oauth/request_token",
                "https://api.twitter.com/oauth/access_token",
                "https://api.twitter.com/oauth/authorize", 
                this.key 
                ));
		
	}
	
	@Override
	public Tuple<String, RequestToken> retrieveRequestToken(String callbackurl) {
        RequestToken rt = oauthHelper.retrieveRequestToken(callbackurl);
        play.Logger.info(rt.toString());	
        return new Tuple<String, RequestToken>(oauthHelper.redirectUrl(rt.token), rt);
	}

	@Override
	public Promise<JsonNode> registerUserProfile(RequestToken requestToken,
			String authVerifier) {
		// TODO Auto-generated method stub
		RequestToken accessToken = oauthHelper.retrieveAccessToken(requestToken, authVerifier);
		WSRequestHolder url = WS.url("https://api.twitter.com/1.1/account/settings.json");
		Promise<Response> promise = url.sign(new OAuthCalculator(key,accessToken)).get();
		Promise<String> screenName = promise.map(Functions.responseToJson).map(Functions.findTextElement("screen_name"));
		return screenName.flatMap(userProfile);
	}
	
    
    public Function<String, Promise<JsonNode>> userProfile = new Function<String, Promise<JsonNode>>() {
        public Promise<JsonNode> apply(final String screenName) {
            Promise<String> response = authenticateApplication().map(
                    Functions.responseToJson).map(Functions.findTextElement("access_token"));
            return response.flatMap(fetchProfile(screenName)).recover(Functions.fetchUserError);
        }
    };

    private static Function<String, Promise<JsonNode>> fetchProfile(
            final String screenName) {
        return new Function<String, Promise<JsonNode>>() {
            public Promise<JsonNode> apply(String accessToken) {
                WSRequestHolder req = WS
                        .url("https://api.twitter.com/1.1/users/show.json")
                        .setQueryParameter("screen_name", screenName)
                        .setHeader("Authorization", "Bearer " + accessToken);
                Promise<Response> promise = req.get();
                return promise.map(Functions.responseToJson);
            }
        };
    }

    private Promise<Response> authenticateApplication() {
        WSRequestHolder req = WS
                .url("https://api.twitter.com/oauth2/token")
                .setHeader("Authorization", "Basic " + bearerToken())
                .setContentType(
                        "application/x-www-form-urlencoded;charset=UTF-8");
        return req.post("grant_type=client_credentials");
    }

    private String bearerToken() {
        return Base64.encode((consumerKey + ":" + consumerSecret).getBytes());
    }

}
