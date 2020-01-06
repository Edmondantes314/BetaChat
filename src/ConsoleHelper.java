import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleHelper {

    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void writeMessage(String message){
        System.out.println(message);
    }

    public static String readString(){

            String res = "";
            while (true){
                try {
               res = reader.readLine();
               return res;
                }
                catch (IOException e){
                    System.out.println("Incorrect input. Try again.");
                    readString();
                }
            }
    }

    public static int readInt() {
        try{
        return Integer.parseInt(readString());}
        catch (NumberFormatException e){
            System.out.println("Incorrect input. Try again.");
           return readInt();
        }
    }
}
