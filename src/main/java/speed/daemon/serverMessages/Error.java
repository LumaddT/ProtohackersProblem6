package speed.daemon.serverMessages;

public class Error implements ServerMessage {
    private final String Message;

    Error(ErrorTypes errorType) {
        Message = errorType.Text;
    }

    @Override
    public int[] encode() {
        int[] encoded = new int[Message.length() + 1];

        encoded[0] = Message.length();
        for (int i = 0; i < Message.length(); i++) {
            encoded[i + 1] = Message.charAt(i);
        }

        return encoded;
    }

    enum ErrorTypes {
        UNEXPECTED_MESSAGE_TYPE("The client began a message with an unexpected message type.");

        private final String Text;

        ErrorTypes(String text) {
            Text = text;
        }
    }
}
