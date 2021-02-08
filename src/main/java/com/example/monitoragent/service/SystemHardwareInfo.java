package com.example.monitoragent.service;

//import cn.hutool.core.util.NumberUtil;
import com.example.monitoragent.config.RabbitMQConfig;
import com.example.monitoragent.tools.IpTool;
import lombok.Data;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.*;
import oshi.hardware.*;
import oshi.hardware.CentralProcessor.TickType;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.Util;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.*;

//import com.gw.ard.common.tools.IpUtil;

@Component
@Data
@EnableScheduling
public class SystemHardwareInfo implements Serializable {


    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final long serialVersionUID = 1L;

    private static final int OSHI_WAIT_SECOND = 1000;

    static HashMap cpuRet = new HashMap();
    static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");





    @Scheduled(cron="*/5 * * * * ?")
    public void copyTo() throws Exception {


        System.out.println("收集信息...");
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        OperatingSystem os=si.getOperatingSystem();
        setCpuInfo(hal.getProcessor());
        setMemInfo(hal.getMemory());
        setJvmInfo();
        setDiskInfo(os);
        setCpuInfo(hal.getProcessor());
        setNetworkInterfaces(hal.getNetworkIFs());
        System.out.println("完成");

    }


    /**
     * 设置CPU信息
     */
    private void setCpuInfo(CentralProcessor processor) throws Exception {


        InetAddress ia=null;
        ia=ia.getLocalHost();
        String localname=ia.getHostName();
        String localip=ia.getHostAddress();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Util.sleep(1000);
        long[] ticks = processor.getSystemCpuLoadTicks();
        long user = ticks[TickType.USER.getIndex()] - prevTicks[TickType.USER.getIndex()];
        long nice = ticks[TickType.NICE.getIndex()] - prevTicks[TickType.NICE.getIndex()];
        long sys = ticks[TickType.SYSTEM.getIndex()] - prevTicks[TickType.SYSTEM.getIndex()];
        long idle = ticks[TickType.IDLE.getIndex()] - prevTicks[TickType.IDLE.getIndex()];
        long iowait = ticks[TickType.IOWAIT.getIndex()] - prevTicks[TickType.IOWAIT.getIndex()];
        long irq = ticks[TickType.IRQ.getIndex()] - prevTicks[TickType.IRQ.getIndex()];
        long softirq = ticks[TickType.SOFTIRQ.getIndex()] - prevTicks[TickType.SOFTIRQ.getIndex()];
        long steal = ticks[TickType.STEAL.getIndex()] - prevTicks[TickType.STEAL.getIndex()];
        long totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal;
        cpuRet.put("ip",localip);
        cpuRet.put("time",df.format(new Date()));
        cpuRet.put("contextSwitches",String.format("%d",processor.getContextSwitches()));
        cpuRet.put("interrupts",String.format("%d",processor.getInterrupts()));
        cpuRet.put("user",String.format("%.2f",100d * user/totalCpu));
        cpuRet.put("nice",String.format("%.2f",100d * nice / totalCpu));
        cpuRet.put("sys",String.format("%.2f",100d * sys / totalCpu));
        cpuRet.put("idle",String.format("%.2f",100d * idle / totalCpu));
        cpuRet.put("iowait",String.format("%.2f",100d * iowait / totalCpu));
        cpuRet.put("irq",String.format("%.2f",100d * irq / totalCpu));
        cpuRet.put("sofrtirq",String.format("%.2f",100d * softirq / totalCpu));
        cpuRet.put("steal",String.format("%.2f",100d * steal / totalCpu));
        rabbitTemplate.convertAndSend(RabbitMQConfig.ITEM_TOPIC_EXCHANGE_CPU ,"cpu_metrics" ,cpuRet);



    }




    private  void setNetworkInterfaces(NetworkIF[] networkIFs) throws Exception {


        List netRet = new ArrayList();
        for (NetworkIF net : networkIFs) {
            HashMap hm = new HashMap();
            hm.put("time",df.format(new Date()));
            hm.put("netCardName", net.getDisplayName());
            hm.put("macAddr", net.getMacaddr());
            hm.put("ip", Arrays.toString(net.getIPv4addr()));
            hm.put("receiveBytes", convertFileSize(net.getBytesRecv()));
            hm.put("receivePackets",net.getPacketsRecv());
            hm.put("sentPackets",net.getPacketsSent());
            hm.put("receivePacketsErrors", net.getInErrors());
            hm.put("sentPacketsErrors", net.getPacketsSent());
            hm.put("sentBytes",convertFileSize(net.getBytesSent()));
            hm.put("sentPacketsErrors", net.getOutErrors());
            netRet.add(hm);

        }

        rabbitTemplate.convertAndSend(RabbitMQConfig.ITEM_TOPIC_EXCHANGE_NET,"net_metrics",netRet);


    }
    /*
       获取磁盘数据
     */
    private   void setDiskInfo(OperatingSystem os) throws Exception {

        List diskRet = new ArrayList();
        FileSystem fileSystem = os.getFileSystem();
        OSFileStore[] fsArray = fileSystem.getFileStores();
        for (OSFileStore fs : fsArray) {

            HashMap hm = new HashMap();
            long free = fs.getUsableSpace();
            long total = fs.getTotalSpace();
            long used = total - free;
            hm.put("time",df.format(new Date()));
            hm.put("ip",IpTool.getIp());
            hm.put("mount",fs.getMount());
            hm.put("type",fs.getType());
            hm.put("name",fs.getName());
            hm.put("total",convertFileSize(total));
            hm.put("free",convertFileSize(free));
            hm.put("used",convertFileSize(used));
            hm.put("usge",div(used, total, 2));
            diskRet.add(hm);

        }


        rabbitTemplate.convertAndSend(RabbitMQConfig.ITEM_TOPIC_EXCHANGE_DISK,"disk_metrics",diskRet);

    }


    /**
     * 设置内存信息
     */
    private void setMemInfo(GlobalMemory memory) throws Exception{

        InetAddress ia=null;
        ia=ia.getLocalHost();
        String localname=ia.getHostName();
        String localip=ia.getHostAddress();
        HashMap hm = new HashMap();
        hm.put("ip",localip);
        hm.put("time",df.format(new Date()));
        hm.put("total",memory.getTotal());
        hm.put("used",(memory.getTotal() - memory.getAvailable()));
        hm.put("free",memory.getAvailable());
        rabbitTemplate.convertAndSend(RabbitMQConfig.ITEM_TOPIC_EXCHANGE_MEM ,"mem_metrics" ,hm);

    }


    /**
     * 设置Java虚拟机
     */
    private void setJvmInfo() throws Exception{
        FileOutputStream outstr = new FileOutputStream("/tmp/JvmInfo.txt");
            ObjectOutputStream outObj = new ObjectOutputStream(outstr);
            Properties props = System.getProperties();
    }






    /**
     * 字节转换
     *
     * @param size 字节大小
     * @return 转换后值
     */
    public static String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        if (size >= gb) {
            return String.format("%.1f GB" , (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB" , f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB" , f);
        } else {
            return String.format("%d B" , size);
        }
    }

    public static double div(double v1, double v2, int scale)
    {
        if (scale < 0)
        {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        if (b1.compareTo(BigDecimal.ZERO) == 0)
        {
            return BigDecimal.ZERO.doubleValue();
        }
        return b1.divide(b2, scale, RoundingMode.HALF_UP).doubleValue();
    }
}



