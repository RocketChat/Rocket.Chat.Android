package chat.rocket.android.model;

import ollie.Model;
import ollie.annotation.Column;
import ollie.annotation.Table;

@Table("auth")
public class Auth extends Model{
    @Column("server_id")
    public Long serverId;

    @Column("account")
    public String account;

    @Column("auth_token")
    public String authToken;
}
