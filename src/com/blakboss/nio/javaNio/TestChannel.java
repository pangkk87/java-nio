package com.blakboss.nio.javaNio;

import org.junit.Test;

import javax.sound.midi.Soundbank;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;

/**
 *通道是完全独立的处理器，附属于CPU，相较于原来的dma是完全独立的处理器，专门处理io操作 ，能更好的利用或者说辅助CPU 更好的实现cpu利用率
 *
 * 一 通道（channel） ： 用于源节点与目标节点的链接。
 *      在 Java NIO 中负责缓冲区中的数据传输。
 *      通道本身不存储数据，因此需要配合缓冲区进行传输。
 *
 * 二 通道的主要实现类：
 *  java.nio.channels.Channel 接口：
 *      |--FileChannel          本地文件
 *      |--SocketChannel        网络TCP
 *      |--ServerSocketChannel  网络TCP
 *      |--DatagramChannel      网络UDP
 *
 * 三 获取通道 （三种）
 *  1 Java 针对支持通道的类提供了 getChannel（）方法
 *      本地 io：
 *      FileInputStream/FileOutputStream
 *      RandomAccessFile   随机存取文件流
 *
 *      网络 IO:
 *      Socket
 *      ServerSocket
 *      DatagramSocket
 *
 *  2 在jdk1.7 中的 NIO.2 针对各个通道提供了静态方法 open()
 *
 *  3 在JDK1.7 中的 NIO.2 的Files工具类 的newByteChannel()
 *
 *  四 通道之间的数据传输
 *  transferFrom
 *  transferTo
 *
 *  五 分散（Scatter）于 聚集（Gather）
 *  分散读取（Scattering Reads） : 将通道中的数据分散到多个缓存区中
 *  聚集写入（Gathering Writes） ： 将多个缓存区的数据聚集到通道中
 *
 * 六 字符集：charset
 * 编码： 字符串 -> 字节数组
 * 解码： 字节数组 -> 字符串
 */
public class TestChannel {

    //字符集
    @Test
    public void test6() throws CharacterCodingException {
        Charset cs1 = Charset.forName("GBK");

        //获取编码器
        CharsetEncoder ce = cs1.newEncoder();

        //获取解码器
        CharsetDecoder cd = cs1.newDecoder();

        CharBuffer cbuf = CharBuffer.allocate(1024);
        cbuf.put("黑老板威武！");
        cbuf.flip();

        //编码  字符转字节
        ByteBuffer bbuf = ce.encode(cbuf);

        for (int i = 0; i < 10; i++) {
            System.out.println(bbuf.get());
        }

        //解码
        bbuf.flip();
        CharBuffer cb = cd.decode(bbuf);

        System.out.println(cb.toString());


    }
    @Test
    public void test5() {
        Map<String, Charset> map = Charset.availableCharsets();

        Set<Map.Entry<String, Charset>> set = map.entrySet();

        for (Map.Entry<String, Charset> entry : set) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }
    }


    //分散和聚集
    @Test
    public void test4() throws IOException {
        RandomAccessFile raf1 = new RandomAccessFile("1.txt", "rw");

        //1 获取通道
        FileChannel channel1 = raf1.getChannel();

        //2 分配指定大小的缓冲区
        ByteBuffer buf1 = ByteBuffer.allocate(100);
        ByteBuffer buf2 = ByteBuffer.allocate(1024);

        //3 分散读取
        ByteBuffer[] bufs = {buf1, buf2};
        channel1.read(bufs);

        for (ByteBuffer byteBuffer : bufs) {
            byteBuffer.flip();
        }

        System.out.println(new String(bufs[0].array(), 0, bufs[0].limit()));
        System.out.println("---------------------------------------------");
        System.out.println(new String(bufs[1].array(), 0, bufs[1].limit()));

        System.out.println("----------------------------------------------");

        //4 聚集写入
        RandomAccessFile raf2 = new RandomAccessFile("2.txt", "rw");
        FileChannel channel2 = raf2.getChannel();

        channel2.write(bufs);
    }

    //通道之间的数据传输(直接缓冲区的方式）
    @Test
    public void test3() throws IOException {
        FileChannel inChannel = FileChannel.open(Paths.get(
                "F:/workFiles/plkWorkSp/java/nio/","1.jpg"),
                StandardOpenOption.READ);
        FileChannel outChannel = FileChannel.open(Paths.get("2.jpg"),
                StandardOpenOption.WRITE,
                StandardOpenOption.READ,
                StandardOpenOption.CREATE);

//        inChannel.transferTo(0, inChannel.size(), outChannel);
        outChannel.transferFrom(inChannel, 0, inChannel.size());
        inChannel.close();
        outChannel.close();

    }

    //使用直接缓存区玩文件的复制（内存映射文件）
    @Test
    public void test2() throws IOException {
        //参数1 文件路径 Paths NIO文件工具类，可多个参数组装
        //参数2 操作方式 支持这个参数的枚举类
        FileChannel inChannel = FileChannel.open(Paths.get(
                "F:/workFiles/plkWorkSp/java/nio/","1.jpg"),
                StandardOpenOption.READ);
        //参数3 CREATE存在就覆盖  CREATE_NEW 如果存在就报错
        FileChannel outChannel = FileChannel.open(Paths.get("2.jpg"),
                StandardOpenOption.WRITE,
                StandardOpenOption.READ,
                StandardOpenOption.CREATE);

        //内存映射文件， 只有ByteBuffer支持
        MappedByteBuffer inMappedBuf = inChannel.map(FileChannel.MapMode.READ_ONLY,0,inChannel.size());
        MappedByteBuffer outMappedBuf = outChannel.map(FileChannel.MapMode.READ_WRITE, 0, inChannel.size());

        //直接对缓冲区进行数据的读写操作，这里直接操作内存缓存
        byte[] dst = new byte[inMappedBuf.limit()];
        inMappedBuf.get(dst);
        outMappedBuf.put(dst);

        inChannel.close();
        outChannel.close();

    }

    //1 利用通道完成文件的复制 非直接缓冲区方式
    @Test
    public void test1(){
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel in = null;
        FileChannel out = null;

        try {
            fis = new FileInputStream("1.jpg");
            fos = new FileOutputStream("2.jpg");

            //① 获取通道
            in = fis.getChannel();
            out = fos.getChannel();

            //② 分配指定大小的缓冲区
            ByteBuffer buf = ByteBuffer.allocate(1024);

            //③ 将通道中的数据存入缓冲区中
            while (in.read(buf) != -1) {
                buf.flip();//切换成读取数据的模式

                //④ 将缓冲区中的数据写入通道中
                out.write(buf);
                //清空已读出来的数据，并且为下次循环提供空置缓冲区
                buf.clear();
            }

        } catch (IOException e) {

        } finally {
            if (out != null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }


    }
}
