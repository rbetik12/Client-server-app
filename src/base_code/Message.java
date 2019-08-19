package base_code;

public class Message {
    public int id;
    public String username;
    public String text;
    public long date;

    public Message(int id, String username, long date, String text){
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
