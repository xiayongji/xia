package com.smarthome.device.adapter;

import com.smarthome.device.entity.Device;

/**
 * 设备适配器接口
 * 采用适配器模式设计多协议适配层
 */
public interface DeviceAdapter {
    
    /**
     * 连接设备
     * @param device 设备信息
     * @return 连接是否成功
     */
    boolean connect(Device device);
    
    /**
     * 断开设备连接
     * @param device 设备信息
     */
    void disconnect(Device device);
    
    /**
     * 发送命令到设备
     * @param device 设备信息
     * @param command 命令内容
     * @return 命令是否发送成功
     */
    boolean sendCommand(Device device, String command);
    
    /**
     * 获取设备状态
     * @param device 设备信息
     * @return 设备状态信息
     */
    String getStatus(Device device);
    
    /**
     * 检查设备是否在线
     * @param device 设备信息
     * @return 设备是否在线
     */
    boolean isOnline(Device device);
    
    /**
     * 获取适配器支持的协议类型
     * @return 协议类型
     */
    String getProtocol();
}