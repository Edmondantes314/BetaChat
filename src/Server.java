package src;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



public class Server {

    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        int PORT = readInt();
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Serever's started!");

        try{
        while(true){
                Socket socket = serverSocket.accept();
                Handler socketHandler = new Handler(socket);
                socketHandler.start();
        }
    }
        catch (Exception e){
            serverSocket.close();
            System.out.println("Error");
        }
    }


    public static void sendBroadcastMessage(Message message){
        try{
            Iterator<Map.Entry<String, Connection>> itr = connectionMap.entrySet().iterator();
            while (itr.hasNext()){
                itr.next().getValue().send(message);
            }
        }catch (IOException e){
            System.out.println("Message has not been sent.");
        }
    }

    private static class Handler extends Thread{
        private Socket socket;

        public Handler(Socket socket){
            this.socket = socket;
        }


        private  String serverHandshake(Connection connection) throws IOException, ClassNotFoundException{

            while ( true){
                connection.send(new Message(MessageType.NAME_REQUEST,"Name?"));
                Message message = connection.receive();
                boolean isNameTaken = false;
                if(message.getType()==MessageType.USER_NAME && message!=null && !message.getData().equals("")){
                    Iterator<Map.Entry<String, Connection>> itr = connectionMap.entrySet().iterator();
                    while (itr.hasNext()){
                        if(itr.next().getKey().equals(message.getData())){
                            isNameTaken = true;
                            break;
                        }
                    }

                    if(!isNameTaken){
                        connectionMap.put(message.getData(),connection);
                        connection.send(new Message(MessageType.NAME_ACCEPTED,"Name accepted"));
                        return message.getData();
                    }
                }
            }
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            for(String name : connectionMap.keySet()){
                if(!name.equals(userName)){
                   connection.send(new Message(MessageType.USER_ADDED,  name));
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true){
               Message message = connection.receive();
               if(message.getType()==MessageType.TEXT){
                   Message message1 = new Message(MessageType.TEXT, userName +": "+message.getData());
                   sendBroadcastMessage(message1);
               }
               else {
                   ConsoleHelper.writeMessage("Error!");
               }
            }
        }

        @Override
        public void run() {
            ConsoleHelper.writeMessage(socket.getRemoteSocketAddress().toString());
            String userName = null;
            try {
                Connection connection = new Connection(socket);
                 userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                notifyUsers(connection,userName);
                serverMainLoop(connection,userName);

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            finally {
                if (userName != null) {
                    connectionMap.remove(userName);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
                }
            }
            System.out.println("Server closed");
        }
    }
}
