package speed.daemon.serverMessages;

import speed.daemon.MessageTypes;
import speed.daemon.codex.MessageEncoder;

public class Error implements ServerMessage {
    private final String Message;

    public Error(ErrorTypes errorType) {
        Message = errorType.Text;
    }

    @Override
    public byte[] encode() {
        byte[] encoded = new byte[Message.length() + 2];
        encoded[0] = MessageTypes.ERROR.getFlag();

        byte[] encodedMessage = MessageEncoder.encodeString(Message);
        System.arraycopy(encodedMessage, 0, encoded, 1, encodedMessage.length);

        return encoded;
    }

    public enum ErrorTypes {
        UNEXPECTED_MESSAGE_TYPE("The client began a message with an unexpected message type.");

        private final String Text;

        ErrorTypes(String text) {
            Text = text;
        }
    }
}
