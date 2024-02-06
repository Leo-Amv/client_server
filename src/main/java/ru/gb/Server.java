package ru.gb;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private static int PORT;
    private static long clientIDCounter =1l;
    private static Map<Long,Socket> clients = new HashMap<>();
    private ServerSocket server;
}
