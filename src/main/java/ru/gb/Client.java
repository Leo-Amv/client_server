package ru.gb;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket client;

    public Client(int port){
        try {
            this.client = new Socket("localhost",port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void start(){
        new Thread(()->{
            boolean flag = true;
            try(Scanner input = new Scanner(client.getInputStream())) {
                while (flag){
                    System.out.println(input.nextLine());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }catch (Exception e){
                System.out.println("Client is disconnected");
            }
        }).start();
        new Thread(()->{
            boolean flag = true;
            try(PrintWriter output = new PrintWriter(client.getOutputStream(),true)) {
                Scanner consoleScanner = new Scanner(System.in);
                while(flag){
                    String consoleInput = consoleScanner.nextLine();
                    output.println(consoleInput);
                    if (consoleInput.equals("q")){
                        flag = false;
                        client.close();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
