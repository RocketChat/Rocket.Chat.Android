package chat.rocket.android.api;

public class Auth {
    public String account;
    public String authToken;

    public Auth(String account, String authToken) {
        this.account = account;
        this.authToken = authToken;
    }
}
