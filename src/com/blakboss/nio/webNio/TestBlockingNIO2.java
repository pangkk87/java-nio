package com.blakboss.nio.webNio;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class TestBlockingNIO2 {

    //客户端
    @Test
    public void client() throws IOException {
        //1 获取通道
        SocketChannel sChannel = SocketChannel.open(
                new InetSocketAddress("127.0.0.1",9898));

        FileChannel inChannel = FileChannel.open(
                Paths.get("1.jpg"),
                StandardOpenOption.READ);

        //2 分配指定大小的缓冲区
        ByteBuffer buf = ByteBuffer.allocate(1024);

        //3 读取本地文件，并发送到服务器
        while (inChannel.read(buf) != -1) {
            buf.flip();
            sChannel.write(buf);
            buf.clear();
        }

        //额外

        sChannel.shutdownOutput();

        //接收服务端的反馈
        int len = 0;
        while ((len = sChannel.read(buf)) != -1) {
            buf.flip();
            System.out.println(new String(buf.array(), 0, len));
            buf.clear();
        }

        // 关闭通道
        inChannel.close();
        sChannel.close();
    }

    //服务端
    @Test
    public void server() throws IOException {

        //1 获取通道
        ServerSocketChannel ssChannel = ServerSocketChannel.open();

        FileChannel outChannel = FileChannel.open(
                Paths.get("2.jpg"),
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE);

        //2 绑定链接
        ssChannel.bind(new InetSocketAddress(9898));

        //3 获取客户端链接的通道
        SocketChannel sChannel = ssChannel.accept();

        //4 分配指定大小的缓冲区
        ByteBuffer buf = ByteBuffer.allocate(1024);

        //5 接受客户端数据，并保存到本地
        while (sChannel.read(buf) != -1) {
            buf.flip();
            outChannel.write(buf);
            buf.clear();
        }

        //返回数据到客户端
        buf.put("服务端接收数据成功".getBytes());
        buf.flip();
        sChannel.write(buf);

        // 关闭通道
        sChannel.close();
        outChannel.close();
        ssChannel.close();
    }
}
