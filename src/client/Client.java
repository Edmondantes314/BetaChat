package client;

import com.javarush.task.task30.task3008.Connection;
import com.javarush.task.task30.task3008.ConsoleHelper;
import com.javarush.task.task30.task3008.Message;
import com.javarush.task.task30.task3008.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;

    public class SocketThread extends Thread{

        protected void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName){
                ConsoleHelper.writeMessage(userName + "has entered the chat");
        }

        protected void informAboutDeletingNewUser(String userName){
            ConsoleHelper.writeMessage(userName + "has left the chat");

        }

        protected void notifyConnectionStatusChanged(boolean clientConnected){
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this){
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException,ClassNotFoundException{
            while (true){
                Message message = connection.receive();
                if(message.getType()==MessageType.NAME_REQUEST){
                    String userName = getUserName();
                    connection.send(new Message(MessageType.USER_NAME,userName));
                }
                if(message.getType()==MessageType.NAME_ACCEPTED){
                    notifyConnectionStatusChanged(true);
                    return;
                }
                if(message.getType()!=MessageType.NAME_ACCEPTED && message.getType()!=MessageType.NAME_REQUEST){
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException{
            while (true){
                Message message = connection.receive();
                if(message.getType()!=MessageType.TEXT && message.getType()!=MessageType.USER_ADDED && message.getType()!=MessageType.USER_REMOVED){
                    throw new IOException("Unexpected MessageType");
                }
                switch (message.getType()){
                    case TEXT:
                        processIncomingMessage(message.getData());
                        break;
                    case USER_ADDED:
                        informAboutAddingNewUser(message.getData());
                        break;
                    case USER_REMOVED:
                        informAboutDeletingNewUser(message.getData());
                        break;

                }
            }
        }

        @Override
        public void run() {
            String serverAdress = getServerAddress();
            int port = getServerPort();
            try {
                Socket socket = new Socket(serverAdress,port);
                Connection connection = new Connection(socket);
                Client.this.connection = connection;
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    protected String getServerAddress(){
       return ConsoleHelper.readString();
    }

    protected int getServerPort(){
        return ConsoleHelper.readInt();
    }
    protected String getUserName(){
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole(){
        return true;
    }

   protected SocketThread getSocketThread(){
        return new SocketThread();
   }

   protected void sendTextMessage(String text) {
        try {
        connection.send(new Message(MessageType.TEXT,text));}
        catch (IOException e){
            ConsoleHelper.writeMessage("Error");
            clientConnected = false;
        }
   }
    public void run(){
        Thread thread = getSocketThread();
        thread.setDaemon(true);
        thread.start();
        synchronized (this){
            try {
            wait();

            }
            catch (Exception e){
                ConsoleHelper.writeMessage("Error");
            }
        }
        if(clientConnected==true){
            ConsoleHelper.writeMessage("Соединение установлено.\n" +
                    "Для выхода наберите команду 'exit'.");
        }
        else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }
            String message;
        while (clientConnected){
            message = ConsoleHelper.readString();
            if(message.equals("exit")){
                break;
            }
            if(shouldSendTextFromConsole()){
                this.sendTextMessage(message);
            }
        }
    }
}
