package external.services;

import com.fasterxml.jackson.databind.JsonNode;

import play.libs.F.*;
import play.libs.OAuth.*;


public interface OAuthService {

	public Tuple<String, RequestToken> retrieveRequestToken(String callbackurl);
	
	public Promise<JsonNode> registerUserProfile(RequestToken requestToken, String authVerifier);
	
}
