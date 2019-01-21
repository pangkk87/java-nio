package com.blackboss.nio;

import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * 一 缓冲区（buffer） ： 在java NIO 中负责数据的存取，
 *      缓冲区就是数组，用于存储不同数据类型的数据
 *
 * 根据数据类型不同（boolean除外），提供了响应类型的缓冲区“
 *  ByteBuffer
 *  CharBuffer
 *  ShortBuffer
 *  IntBuffer
 *  LongBuffer
 *  FloatBuffer
 *  DoubleBuffer
 *
 *  上述缓冲区管理方式几乎一致，用过allocate()获取缓冲区
 *
 *  二 缓冲区获取数据的两个核心方法
 *  put() : 存入数据到缓冲区中
 *  get() ： 获取缓冲区中的数据
 *
 *  三 缓冲区中的四个核心属性：
 *  capacity ： 容量，表示缓冲区中最大存储数据容量。一旦声明不能改变。
 *  limit ： 接线，表示缓冲区中可以操作数据的大小。（limit 后面的数据不能进行读写）
 *  position ： 位置，表示缓冲区中正在操作数据的位置
 *
 *  mark : 标记，表示基础当前 position 的位置
 *          可以通过 rest（） 恢复到 mark 的位置
 *
 *  0 <= mark <= position <= limit <= capacity
 *
 *
 *  四 直接缓冲区于非直接缓冲区
 *  非直接缓冲区：通过 allocate() 方法分配缓冲区，
 *  将缓冲区建立在 JVM 的内存中
 *  直接缓冲区： 通过 allocateDirect（）方法分配直接缓冲区，
 *  将缓冲区简历在物理内存中（能提高效率，弊端在于开辟出来的内存
 *  不可控制）
 *
 *  扩展 ： 当应用程序发起磁盘read()时，是调用os（操作系统）
 *  的io操作，此时os会将数据放置os的内核地址空间（查询资料）
 *  然后从内核地址空间 copy 到用户地址空间（jvm内存），程序
 *  再向jvm内存地址读取数据出来
 *
 *  allocateDirect（） 在物理内存页中开辟内存资源，不安全，
 *  而且开销也大，不比在jvm中开辟内种资源开销合适，需要
 *  手动处理垃圾回收（System.gc，不会立即执行）才会释放内存，
 *  应该用于需要做内存持久化的数据
 */
public class TestBuffer {

    @Test
    public void test3(){
        //分配直接缓冲区
        ByteBuffer buf = ByteBuffer.allocateDirect(1024);

        //判断是否使用直接缓冲区
        System.out.println(buf.isDirect());
    }

    @Test
    public void test2(){
        String str = "abcde";

        ByteBuffer buf = ByteBuffer.allocate(1024);

        buf.put(str.getBytes());

        buf.flip();

        byte[] dst = new byte[buf.limit()];
        buf.get(dst,0,2);
        System.out.println(new String(dst,0,2));

        System.out.println(buf.position());

        //mark() ： 标记
        buf.mark();

        buf.get(dst,2,2);
        System.out.println(new String(dst,2,2));

        System.out.println(buf.position());

        //reset() : 恢复到mark的位置
        buf.reset();

        System.out.println(buf.position());

        // 判断缓冲区是否还有剩余数据，可操作的数据
        if(buf.hasRemaining()){
            //获取缓冲区可以操作的数量，如果有，我看看还有几个
            System.out.println(buf.remaining());
        }
    }

    @Test
    public void test1(){
        String str = "abcde";
        //1 分配一个指定大小的缓冲区
        ByteBuffer buf = ByteBuffer.allocate(1024);

        System.out.println("-------------allocate()----------------------");
        System.out.println(buf.capacity());
        System.out.println(buf.limit());
        System.out.println(buf.position());

        //2 利用put（）存入数据到缓冲区
        buf.put(str.getBytes());

        System.out.println("-------------put()----------------------");
        System.out.println(buf.capacity());
        System.out.println(buf.limit());
        System.out.println(buf.position());

        //3 切换到读取数据模式
        buf.flip();
        System.out.println("-------------flip()----------------------");
        System.out.println(buf.capacity());
        System.out.println(buf.limit());
        System.out.println(buf.position());

        //4 利用get（） 读取缓冲区数据
        byte[] dst = new byte[buf.limit()];
        buf.get(dst);
        System.out.println("-------------get()----------------------");
        System.out.println(new String(dst,0,dst.length));
        System.out.println(buf.capacity());
        System.out.println(buf.limit());
        System.out.println(buf.position());

        //5 rewind（） ： 可重复读数据
        buf.rewind();

        System.out.println("-------------rewind()----------------------");
        System.out.println(buf.capacity());
        System.out.println(buf.limit());
        System.out.println(buf.position());

        //6 clear() 清空缓冲区,但是缓冲区的数据仍然存在，但是处于“被遗忘”状态
        buf.clear();
        System.out.println("-------------clear()----------------------");
        System.out.println(buf.capacity());
        System.out.println(buf.limit());
        System.out.println(buf.position());

        System.out.println((char)buf.get());

    }


}
