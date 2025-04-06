package speed.daemon.codex;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import speed.daemon.MessageTypes;
import speed.daemon.clientMessages.ClientMessage;
import speed.daemon.clientMessages.WantHeartbeat;
import speed.daemon.exceptions.ExpectedMoreBytesException;
import speed.daemon.exceptions.ImpossibleEncodingException;
import speed.daemon.exceptions.SocketIsDeadException;
import speed.daemon.exceptions.UnexpectedMessageTypeException;
import speed.daemon.serverMessages.Error;
import speed.daemon.serverMessages.Heartbeat;
import speed.daemon.serverMessages.ServerMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SocketHolder {
    private static final Logger logger = LogManager.getLogger();

    private final Socket Socket;
    private final InputStream InputStream;
    private final OutputStream OutputStream;

    private boolean SendingHeartbeat = false;

    private final BlockingQueue<ClientMessage> MessageQueue = new LinkedBlockingQueue<>();

    private final Set<MessageTypes> ExpectedMessageTypes = new HashSet<>();

    @Getter
    private final ClientMessage InitialMessage;

    @Getter
    private volatile boolean ConnectionAlive = false;

    public SocketHolder(Socket socket) {
        Socket = socket;

        InputStream inputStream;
        OutputStream outputStream;

        try {
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            ConnectionAlive = true;
        } catch (IOException e) {
            logger.debug("An error occurred wile obtaining Input and Output streams for for a Socket. The Socket will be discarded.");
            inputStream = null;
            outputStream = null;

            this.close();
        }

        InputStream = inputStream;
        OutputStream = outputStream;

        ClientMessage initialMessage = null;

        while (true) {
            try {
                initialMessage = null;
                initialMessage = MessageReceiver.receiveClientMessage(InputStream, Set.of(
                        MessageTypes.I_AM_CAMERA, MessageTypes.I_AM_DISPATCHER, MessageTypes.WANT_HEARTBEAT));
                logger.info("(Initial) Received {} in socket {}.", initialMessage.toString(), this.hashCode());
            } catch (ExpectedMoreBytesException e) {
                this.sendError(Error.ErrorTypes.EXPECTED_MORE_BYTES);

                break;
            } catch (UnexpectedMessageTypeException e) {
                this.sendError(Error.ErrorTypes.UNEXPECTED_MESSAGE_TYPE);

                break;
            } catch (IOException e) {
                break;
            }

            if (initialMessage.getMessageType() == MessageTypes.WANT_HEARTBEAT) {
                manageHeartbeat(initialMessage);
                continue;
            }

            break;
        }

        if (initialMessage == null || !ConnectionAlive) {
            InitialMessage = null;
            this.close();
            return;
        }

        InitialMessage = initialMessage;

        switch (InitialMessage.getMessageType()) {
            case I_AM_CAMERA -> this.setExpectedMessageTypes(MessageTypes.PLATE);
            case I_AM_DISPATCHER -> this.setExpectedMessageTypes();
        }

        new Thread(this::run).start();
    }

    private void run() {
        while (true) {
            ClientMessage clientMessage;

            try {
                clientMessage = MessageReceiver.receiveClientMessage(InputStream, ExpectedMessageTypes);
                logger.info("Received {} in socket {}.", clientMessage.toString(), this.hashCode());
            } catch (ExpectedMoreBytesException e) {
                this.sendError(Error.ErrorTypes.EXPECTED_MORE_BYTES);

                this.close();
                return;
            } catch (UnexpectedMessageTypeException e) {
                this.sendError(Error.ErrorTypes.UNEXPECTED_MESSAGE_TYPE);

                this.close();
                return;
            } catch (IOException e) {
                this.close();
                return;
            }

            if (clientMessage.getMessageType() == MessageTypes.WANT_HEARTBEAT) {
                manageHeartbeat(clientMessage);
            }

            MessageQueue.add(clientMessage);
        }
    }

    /**
     * Blocking method.
     */
    public ClientMessage getNextClientMessage() throws SocketIsDeadException {
        while (true) {
            try {
                ClientMessage clientMessage = MessageQueue.poll(1, TimeUnit.SECONDS);
                if (!isConnectionAlive()) {
                    throw new SocketIsDeadException("Attempted to receive a message form a dead socket.");
                } else if (clientMessage != null) {
                    logger.info("Retrieved {} from MessageQueue in socket {}.", clientMessage.toString(), this.hashCode());
                    return clientMessage;
                }
            } catch (InterruptedException e) {
                this.close();
                throw new SocketIsDeadException("Attempted to receive a message form a dead socket.");
            }
        }
    }

    private void setExpectedMessageTypes(MessageTypes... expectedMessageTypes) {
        ExpectedMessageTypes.clear();

        ExpectedMessageTypes.add(MessageTypes.WANT_HEARTBEAT);
        ExpectedMessageTypes.addAll(Arrays.asList(expectedMessageTypes));
    }

    public void sendError(Error.ErrorTypes errorType) {
        new Thread(() -> {
            try {
                this.sendMessage(Error.ERRORS.get(errorType));
            } catch (SocketIsDeadException e) {
                logger.debug("Attempted to send an Error message of type \"{}\" to a dead socket.", errorType.name());
            }
        }).start();
    }

    public synchronized void sendMessage(ServerMessage serverMessage) throws SocketIsDeadException {
        if (!isConnectionAlive()) {
            throw new SocketIsDeadException("Attempted to send message over dead socket.");
        }

        byte[] encodedMessage;
        try {
            encodedMessage = serverMessage.encode();
        } catch (ImpossibleEncodingException e) {
            logger.error("Attempted to send {} but it cannot be encoded.", serverMessage.toString());
            return;
        }

        try {
            OutputStream.write(encodedMessage);
            logger.info("Sent message {} in socket {}.", serverMessage, this.hashCode());
        } catch (IOException e) {
            this.close();
        }
    }

    private void manageHeartbeat(ClientMessage clientMessage) {
        WantHeartbeat wantHeartbeat = (WantHeartbeat) clientMessage;

        if (SendingHeartbeat) {
            logger.debug("Received 2 WantHeartbeat messages from the came client.");
            this.sendError(Error.ErrorTypes.DOUBLE_HEARTBEAT_ERROR);
            this.close();
            return;
        }

        SendingHeartbeat = true;

        if (wantHeartbeat.getInterval() != 0) {
            new Thread(() -> this.sendHeartbeat(wantHeartbeat.getInterval())).start();
        }
    }

    private void sendHeartbeat(long interval) {
        while (true) {
            long intervalMilliseconds = interval * 100;

            try {
                //noinspection BusyWait
                Thread.sleep(intervalMilliseconds);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            try {
                this.sendMessage(Heartbeat.INSTANCE);
            } catch (SocketIsDeadException e) {
                return;
            }
        }
    }

    public void close() {
        logger.debug("Closing socket {}.", this.hashCode());
        ConnectionAlive = false;

        try {
            Socket.shutdownInput();
            Socket.shutdownOutput();

            Socket.close();
        } catch (IOException ex) {
            logger.debug("An error occurred while attempting to close this Socket.");
        }
    }
}
