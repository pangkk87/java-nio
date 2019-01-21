package com.blakboss.nio.webNio;

import org.junit.Test;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.Iterator;

public class TestNonBlockingNIO {

    //客户端
    @Test
    public void cilent() throws IOException {
        //1 获取通道
        SocketChannel sChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9898));

        //2 切换阻塞模式
        sChannel.configureBlocking(false);

        //3 分配指定大小的缓冲区
        ByteBuffer buf = ByteBuffer.allocate(1024);

        //4 发送数据给服务端
        buf.put(LocalDateTime.now().toString().getBytes());
        buf.flip();
        sChannel.write(buf);
        buf.clear();

        //5 关闭通道
        sChannel.close();
    }

    //服务器
    @Test
    public void server() throws IOException {
        //1 获取通道
        ServerSocketChannel ssChannel = ServerSocketChannel.open();

        //2 切换非阻塞模式
        ssChannel.configureBlocking(false);

        //3 绑定链接
        ssChannel.bind(new InetSocketAddress(9898));

        //4 获取选择器
        Selector selector = Selector.open();

        /*
        *
        * 将通道注册到选择器
        * 参数 1 选择器
        * 参数 2 selectionKey 监控类型
        *   读： SelectionKey.OP_READ        1
        *   写： SelectionKey.OP_WRITE       4
        *  链接： SelectionKey.OP_CONNECT    8
        *  接收： SelectionKey.OP_ACCEPT     16
        *  同时监听， 用 | 链接
        *  ssChannel.register(selector,
                SelectionKey.OP_CONNECT |
                    SelectionKey.OP_ACCEPT |
                    SelectionKey.OP_READ |
                    SelectionKey.OP_WRITE);
        * */

        //5 将通道注册到选择器，并且指定“监听事件”
        ssChannel.register(selector, SelectionKey.OP_ACCEPT);

        //6 轮询式的获取选择器上已经“准备就绪”的事件
        while (selector.select() > 0) {
            //7 获取当前选择器中所有注册的“选择键（已就绪的监听事件）”
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

            while (iterator.hasNext()) {
                //8 获取主播就绪的事件
                SelectionKey sk = iterator.next();

                //9 判断具体是什么事件准备就绪
                if (sk.isAcceptable()) {
                    //10 若“接收就绪”，获取客户端链接
                    SocketChannel sChanner = ssChannel.accept();

                    //11 切换阻塞模式
                    sChanner.configureBlocking(false);

                    //12 将通道注册到选择器上
                    sChanner.register(selector, SelectionKey.OP_READ);

                } else if (sk.isReadable()) {
                    //13 获取当前选择器上“读就绪”状态通道
                    SocketChannel sChannel = (SocketChannel) sk.channel();

                    //14 读取数据
                    ByteBuffer buf = ByteBuffer.allocate(1024);

                    int len = 0;
                    while ((len = sChannel.read(buf)) > 0) {
                        buf.flip();
                        System.out.println(new String(buf.array(), 0, len));
                        buf.clear();
                    }
                }

                //15 取消选择键 SelectionKey
                iterator.remove();
            }
        }
    }
}
