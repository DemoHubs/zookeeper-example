package com.peachyy.zk.example;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>描述:</p>
 *
 * @author Tao xs
 * @since 2.0
 * <p>Created by Tao xs on 2016/12/23.</p>
 */
public class Test1 {
    private static final int DEFAULT_SESSION_TIMEOUT = 5000;
    static ZooKeeper zk;
    private static Object o=new Object();
    private static CountDownLatch latch = new CountDownLatch(1);
    private static CountDownLatch latch2 = new CountDownLatch(1);
    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
            zk = new ZooKeeper("119.84.15.168:2181", DEFAULT_SESSION_TIMEOUT,
                    new Watcher() {

                        @Override
                        public void process(WatchedEvent event) {
                            if (event.getState().equals(
                                    Event.KeeperState.SyncConnected)) {
                                System.out.println("链接成功");
                                latch.countDown();
                            }

                        }
                    });
        latch.await();
        //创建节点
        if(null ==zk.exists("/taoxs",null)){
            String c= zk.create("/taoxs","i love you".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
            System.out.println("创建成功->"+c);

        }
        //创建临时子节点
        zk.create("/taoxs/ch1","i love you1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL);
        zk.create("/taoxs/ch11","i love you1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL);
        zk.create("/taoxs/ch2","i love you1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
        zk.create("/taoxs/ch2","i love you1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
        zk.create("/taoxs/ch3","i love you1".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
        zk.exists("/taoxs/ch2", new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if(event.getType().equals(Event.EventType.NodeDataChanged)){
                    System.out.println(String.format("%s节点数据被更新过",event.getPath()));
                }
                if(event.getType().equals(Event.EventType.NodeDeleted)){
                    System.out.println(String.format("%s节点数据被删除了",event.getPath()));
                }
                if(event.getType().equals(Event.EventType.NodeCreated)){
                    System.out.println(String.format("%s节点创建了",event.getPath()));
                }
                if(event.getType().equals(Event.EventType.NodeChildrenChanged)){
                    System.out.println(String.format("%当前节点或子节点也被更新了",event.getPath()));
                }
            }
        });
        List<String> s= zk.getChildren("/taoxs",false);
        s.stream().sorted().forEach(System.out::println);
        Collections.sort(s);
        int index=s.indexOf("ch11");
        System.out.println("index:"+index);
        if(index!=0 && index!=-1){//不是最小节点
            Lock lock=new ReentrantLock();

           Stat tmpPath= zk.exists("/taoxs/ch11", new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType().equals(Event.EventType.NodeDeleted)&&event.getPath().equals("/taoxs/ch11")) {
                        latch2.countDown();
                    }
                }
            });
            if(null !=tmpPath){
                latch2.await();
            }
            System.out.println("得到lock了");
        }
        Thread.sleep(Integer.MAX_VALUE);
//        zk.exists("/taoxs", new Watcher() {
//            @Override
//            public void process(WatchedEvent event) {
//                if(event.getState().){
//
//                }
//            }
//        });
    }

}
