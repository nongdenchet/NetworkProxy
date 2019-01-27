package com.rain.networkproxy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class CommandLine {

    public static void main(String[] args) {
        final Scanner scanner = new Scanner(System.in);
        try (Socket socket = new Socket("127.0.0.1", 8000)) {
            System.out.println("Server connected");

            final DataInputStream inputStream = new DataInputStream(socket.getInputStream());
            final DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeUTF("start");

            while (!socket.isClosed()) {
                System.out.println("Waiting for message");

                final String message = inputStream.readUTF();
                System.out.println("Message: " + message);

                if (message.equals("[]")) {
                    System.out.println("No pending response --> Skipping input");
                } else {
                    System.out.println("Your input:");
                    dataOutputStream.writeUTF(scanner.nextLine().trim());
                }
            }

            inputStream.close();
            dataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Connection closed");
        }
    }
}
