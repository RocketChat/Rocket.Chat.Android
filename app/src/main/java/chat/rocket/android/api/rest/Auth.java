package chat.rocket.android.api.rest;

public class Auth {
    public String userId;
    public String authToken;

    public Auth(String userId, String authToken) {
        this.userId = userId;
        this.authToken = authToken;
    }
}
