package chat.rocket.android.model;

import ollie.Model;
import ollie.annotation.Column;
import ollie.annotation.Table;

@Table("server")
public class Server extends Model {
    @Column("dbname")
    public String dbname;

    @Column("displayname")
    public String displayname;

    @Column("hostname")
    public String hostname;
}
