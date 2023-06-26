import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {

        //GET USER INPUT FROM 1-4
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter Datagram socket to use [1-4]: ");
        String socketNo = scanner.nextLine();
        scanner.close();

        ArrayList<String> acceptedStrings = new ArrayList<>();
        acceptedStrings.add("1");
        acceptedStrings.add("2");
        acceptedStrings.add("3");
        acceptedStrings.add("4");

        if(!acceptedStrings.contains(socketNo)) {
            throw new Exception("Invalid socket specified!");
        }

        //MAKE THE SENDER/RECEIVER CLASSES WITH THE SPECIFIED SOCKET THE USER INPUT
        Sender sender = new Sender(socketNo);
        Receiver receiver = new Receiver(socketNo);
        Thread senderThread = new Thread(sender);
        Thread receiverThread = new Thread(receiver);
        senderThread.start();
        receiverThread.start();
    }
}
