/**
 * Created by ASUS on 2018/4/24.
 */
import java.io.*;
import java.math.RoundingMode;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;

class ServerGlobal{
    public static int SERVER_PORT = 8889;
}


public class P2pUpdateServer extends ServerSocket{

    private static DecimalFormat df = null;

    private static String filePath = "C:\\Users\\ASUS\\Desktop\\P2P\\p2p_resources\\"; //cache the files path

    private InputStream inputStream;

    private FileInputStream fis;

    private DataOutputStream dos;



    static{
        df = new DecimalFormat("#0.0");
        df.setRoundingMode(RoundingMode.HALF_UP);
        df.setMinimumFractionDigits(1);
        df.setMaximumFractionDigits(1);
    }

    public P2pUpdateServer() throws Exception{
        super(ServerGlobal.SERVER_PORT);
    }

    public void load() throws Exception{
        while (Global.semp != -1){

            Socket socket = this.accept();
            System.out.println("[UpdateServer info]: New downloader!");

            inputStream = socket.getInputStream();
            byte[] bytes;
            /**
             *Rev HELLO and client ip
             * format: "HELLO DOWNLOADERNAME DOWNLOADERIP"
             *  **/
            /*int first = inputStream.read();

            if(first == -1){
                break;
            }

            int second = inputStream.read();
            int length = (first<<8)+second;

            bytes = new byte[length];
            inputStream.read(bytes);
            System.out.println("[UpdateServer info]: Get message from downloader:" + new String(bytes,"UTF-8"));
            String str1[];
            String box = new String(bytes,"UTF-8");
            str1 = box.split(" ");*/
            sendFile(socket);
        }

    }
    public void sendFile(Socket socket) throws Exception{
        try{
            inputStream =  socket.getInputStream();

            /**rev update filename
                format: "DOWNLOAD FILENAME"
             **/
            int first = inputStream.read();

            int second = inputStream.read();
            int length1 = (first<<8)+second;

            byte[] bytes1;
            bytes1 = new byte[length1];
            inputStream.read(bytes1);
            System.out.println("[info]: Get message from downloader:"  + new String(bytes1,"UTF-8"));
            String str1[];
            String box = new String(bytes1,"UTF-8");
            str1 = box.split(" ");

            filePath = filePath + str1[1];

            File file = new File(filePath);//Input file path

            if(file.exists()){
                fis = new FileInputStream(file);
                dos = new DataOutputStream(socket.getOutputStream());

                dos.writeUTF(file.getName());
                dos.flush();
                dos.writeLong(file.length());
                dos.flush();

                System.out.println("[UpdateServer info]: File sharing start.");
                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes,0,bytes.length))!= -1){
                    dos.write(bytes,0,length);
                    dos.flush();
                }

                System.out.println("\n[UpdateServer info]: File sharing is finished");
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally{
            if(fis != null)
                fis.close();
            if(dos != null)
                dos.close();
            socket.close();
        }
    }
}
