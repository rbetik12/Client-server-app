package base_code;

/**
 *  Message object contains id, author name, date and text
 */
public class Message {
    public final int id;
    public final String username;
    public final String text;
    public final long date;

    public Message(int id, String username, long date, String text) {
        this.id = id;
        this.username = username;
        this.text = text;
        this.date = date;
    }

    @Override
    public String toString() {
        return id + "\n" + username + "\n" + date + "\n" + text;
    }

}
