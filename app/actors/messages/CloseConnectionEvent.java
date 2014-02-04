package actors.messages;

/**
 * Created by lichlyterklein on 12/29/13.
 */
public class CloseConnectionEvent {
    private String uuid;

    public CloseConnectionEvent(String uuid) {
        this.uuid = uuid;
    }

    public String uuid() {
        return uuid;
    }
}
