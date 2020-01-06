package client;



import com.sun.deploy.uitoolkit.ui.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BotClient extends Client {

    public static void main(String[] args) {
        BotClient bot = new BotClient();
        bot.run();
    }
    public class BotSocketThread extends Client.SocketThread{

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            Calendar calendar = Calendar.getInstance();
            Date date = calendar.getTime();
            Map<String,SimpleDateFormat> formats = new HashMap<>();
            formats.put("дата",new SimpleDateFormat("d.MM.YYYY"));
            formats.put("день",new SimpleDateFormat("d"));
            formats.put("месяц",new SimpleDateFormat("MMMM"));
            formats.put("год",new SimpleDateFormat("YYYY"));
            formats.put("время",new SimpleDateFormat("H:mm:ss"));
            formats.put("час",new SimpleDateFormat("H"));
            formats.put("минуты",new SimpleDateFormat("m"));
            formats.put("секунды",new SimpleDateFormat("s"));


                String[] data = new String[2];
                if(!message.contains(":")){
                    return;
                }
                data = message.split(": ");
                if(!formats.containsKey(data[1])){
                    return;
                }
                SimpleDateFormat dateFormat =formats.get(data[1]);
                if(dateFormat!=null){

                String mess="Информация для " + data[0] + ": "+dateFormat.format(date);
                sendTextMessage(mess);
                }

        }
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        double randNumber = Math.random();
       double d = randNumber * 100;

        int randomInt = (int)d;
        return "date_bot_" + randomInt;
    }
}
