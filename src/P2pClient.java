import java.io.*;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;

class Global
{
    public static String SERVER_IP = "";//server's IP
    public static int SERVER_PORT;//server's port
    public static int semp = 0;

}

public class P2pClient extends Socket {

    private static DecimalFormat df = null;

    private Socket client;

    private OutputStream outputStream;

    private InputStream inputStream;

    private static ArrayList<String> arrayList = new ArrayList<String>();

    private static long[] fileSize;

    private static String filePath="C:\\Users\\ASUS\\Desktop\\P2P\\p2p_resources\\";

    private static String filePaths ="C:\\Users\\ASUS\\Desktop\\P2P\\p2p_resources";    //resources path

    private static Scanner scanner = new Scanner(System.in);

    static{
        df = new DecimalFormat("#0.0");
        df.setRoundingMode(RoundingMode.HALF_UP);
        df.setMinimumFractionDigits(1);
        df.setMaximumFractionDigits(1);
    }

    public P2pClient()throws Exception {
        super(Global.SERVER_IP, Global.SERVER_PORT);
        new Thread(new Task()).start();
    }
    public void load() throws Exception{

        this.client=this;
        System.out.println("Cliect[port:"+client.getLocalPort()+"] connect successful!");

        outputStream = this.client.getOutputStream();

        InetAddress ia = null;
        ia = ia.getLocalHost();
        String localname = ia.getHostName();
        String localip = ia.getHostAddress();

        /**
         * Send HELLO and IP
         * **/
        String message = "HELLO " + localname + " " + localip;
        byte[] sendBytes = message.getBytes("UTF-8");
        outputStream.write(sendBytes.length>>8);
        outputStream.write(sendBytes.length);
        outputStream.write(sendBytes);
        outputStream.flush();

        String message1 = "";

        try{
            message1 = arrayList.get(0);
            for(int i =1;i<arrayList.size();i++){
                message1 = message1 + " "+ arrayList.get(i);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        /**
         * Send file name form to server
         * **/

        byte[] sendBytes2 = message1.getBytes("UTF-8");
        outputStream.write(sendBytes2.length>>8);
        outputStream.write(sendBytes2.length);
        outputStream.write(sendBytes2);
        outputStream.flush();

        /**
         * Send file size form to server
         * **/
        message1 = "";

        try{
            message1 = Long.toString(fileSize[0]);
            for(int i =1;i<fileSize.length;i++){
                message1 = message1 + " "+  Long.toString(fileSize[i]);
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        sendBytes2 = message1.getBytes("UTF-8");
        outputStream.write(sendBytes2.length>>8);
        outputStream.write(sendBytes2.length);
        outputStream.write(sendBytes2);
        outputStream.flush();
        for(int a = 0; a!= -1 ;){
            System.out.println("[Tips]: \n1. Get file list from server.\n-1. Exit");
            a=scanner.nextInt();
            if(a == 1){

                message1 = "GETTABLE";
                sendBytes2 = message1.getBytes("UTF-8");
                outputStream.write(sendBytes2.length>>8);
                outputStream.write(sendBytes2.length);
                outputStream.write(sendBytes2);
                outputStream.flush();

                inputStream =  client.getInputStream();
                //get file table
                int first = inputStream.read();
                System.out.println(first);
                if(first == -1){
                    break;
                }
                int second = inputStream.read();
                int length = (first<<8)+second;




                byte[] bytes1;
                bytes1 = new byte[length];
                inputStream.read(bytes1);
                System.out.println("[info]: Get message from server:" + new String(bytes1,"UTF-8"));
                String meg;
                meg = new String(bytes1,"UTF-8");
                String[] filetable ;
                filetable = meg.split(" ");
                int i = 0;
                for(String s:filetable){
                    System.out.println((i) + ". " +s);
                    i++;
                }

                int num;

                System.out.println("[Tips]; Input the num what you want to download:");
                num = scanner.nextInt();

                //send id of files to servers
                message1 = Integer.toString(num);
                sendBytes2 = message1.getBytes("UTF-8");
                outputStream.write(sendBytes2.length>>8);
                outputStream.write(sendBytes2.length);
                outputStream.write(sendBytes2);
                outputStream.flush();

                //get peer ip
                first = inputStream.read();

                second = inputStream.read();
                length = (first<<8)+second;

                bytes1 = new byte[length];
                inputStream.read(bytes1);
                System.out.println("[info]: Get message from server:" + new String(bytes1,"UTF-8"));

                String[] getIp;
                meg = new String(bytes1,"UTF-8");
                getIp = meg.split(" ");
                P2pDownloadClient p2pClient = new P2pDownloadClient(getIp[0],filetable[num]);
                p2pClient.load();
            }
            else if(a == -1){
                message1 = "EXIT";
                sendBytes2 = message1.getBytes("UTF-8");
                outputStream.write(sendBytes2.length>>8);
                outputStream.write(sendBytes2.length);
                outputStream.write(sendBytes2);
                outputStream.flush();
            }
        }
        Global.semp = -1;
        client.close();
    }

    private static boolean readfile(String filepath)throws FileNotFoundException,IOException{
        try{
            int id=1;
            File file = new File(filepath);

            if (!file.isDirectory()) {

                System.out.println("name=" + file.getName());

            } else if (file.isDirectory()) {
                //System.out.println("文件夹");
                String[] filelist = file.list();
                fileSize = new long[filelist.length];
                for (int i = 0; i < filelist.length; i++) {
                    File readfile = new File(filepath + "\\" + filelist[i]);
                    if (!readfile.isDirectory()) {

                        System.out.println("[file]: "+id + ". " + readfile.getName() + "   size: "+ getFormatFileSize(readfile.length()) + "   hash: " + readfile.hashCode());
                        arrayList.add(readfile.getName());
                        fileSize[id - 1] = readfile.length();
                        id++;
                    }
                    /* else if (readfile.isDirectory()) {
                        readfile(filepath + "\\" + filelist[i]);
                    }*/
                }
            }
        }catch (Exception e){
            System.out.println("[error]: readfile()  Exception:" + e.getMessage());
        }
        return true;
    }
    private static String getFormatFileSize(long length){
        double size = ((double) length)/(1<<30);
        if(size>=1){
            return df.format(size) + "GB";
        }
        size = ((double) length) / (1 << 20);
        if(size >= 1) {
            return df.format(size) + "MB";
        }
        size = ((double) length) / (1 << 10);
        if(size >= 1) {
            return df.format(size) + "KB";
        }
        return length + "B";
    }

    class Task implements Runnable{
        public Task(){
        }

        @Override

        public void run(){
            try {
                P2pUpdateServer p2pUpdateServer = new P2pUpdateServer();
                p2pUpdateServer.load();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }


    public static void main(String[] args){
        try{
            Scanner scanner =new Scanner(System.in);

            System.out.print("Input the servers IP>>");
            Global.SERVER_IP = scanner.nextLine();
            System.out.print("Input the port>>");
            Global.SERVER_PORT = scanner.nextInt();
            readfile(filePaths);

            P2pClient client = new P2pClient();
            client.load();
            /*client.sendFile();*/
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("[error]");
            System.exit(-1);
        }
    }
}
