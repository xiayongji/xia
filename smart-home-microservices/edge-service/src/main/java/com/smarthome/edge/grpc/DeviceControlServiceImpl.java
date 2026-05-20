package com.smarthome.edge.grpc;

import com.smarthome.edge.entity.DeviceStatus;
import com.smarthome.edge.EdgeGatewayService;
import com.smarthome.edge.LocalDeviceManager;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class DeviceControlServiceImpl extends DeviceControlServiceGrpc.DeviceControlServiceImplBase {

    private final EdgeGatewayService edgeGatewayService;
    private final LocalDeviceManager localDeviceManager;

    @Override
    public void executeCommand(DeviceRequest request, StreamObserver<DeviceResponse> responseObserver) {
        log.info("收到 gRPC 命令请求: deviceId={}, command={}", request.getDeviceId(), request.getCommand());

        com.smarthome.edge.entity.CommandResult result = edgeGatewayService.handleControlCommand(
                request.getDeviceId(),
                request.getCommand()
        );

        DeviceResponse response = DeviceResponse.newBuilder()
                .setDeviceId(result.getDeviceId())
                .setSuccess(result.isSuccess())
                .setMessage(result.getMessage())
                .setStatus(result.getResponse())
                .setTimestamp(System.currentTimeMillis())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getDeviceStatus(DeviceStatusRequest request, StreamObserver<DeviceStatusResponse> responseObserver) {
        log.info("收到 gRPC 状态查询请求: deviceIds={}", request.getDeviceIdsList());

        List<DeviceStatus> statusList = new ArrayList<>();
        for (String deviceId : request.getDeviceIdsList()) {
            DeviceStatus status = edgeGatewayService.getDeviceStatusFromDatabase(deviceId);
            if (status == null) {
                status = localDeviceManager.getLatestDeviceStatus(deviceId);
            }
            if (status != null) {
                statusList.add(status);
            }
        }

        List<com.smarthome.edge.grpc.DeviceStatus> grpcStatusList = new ArrayList<>();
        for (DeviceStatus status : statusList) {
            grpcStatusList.add(com.smarthome.edge.grpc.DeviceStatus.newBuilder()
                    .setDeviceId(status.getDeviceId())
                    .setStatus(status.getStatus())
                    .setDeviceType(status.getDeviceType())
                    .setPower(status.getPower() != null ? status.getPower() : 0.0)
                    .setLastUpdate(status.getLastUpdate() != null ? status.getLastUpdate().toInstant(ZoneOffset.UTC).toEpochMilli() : System.currentTimeMillis())
                    .build());
        }

        DeviceStatusResponse response = DeviceStatusResponse.newBuilder()
                .addAllStatusList(grpcStatusList)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void syncDeviceState(SyncRequest request, StreamObserver<SyncResponse> responseObserver) {
        log.info("收到 gRPC 同步请求: edgeId={}, lastSyncTime={}", request.getEdgeId(), request.getLastSyncTime());

        boolean success = edgeGatewayService.syncWithCloud();

        SyncResponse response = SyncResponse.newBuilder()
                .setSuccess(success)
                .setMessage(success ? "同步成功" : "同步失败")
                .setSyncTime(System.currentTimeMillis())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getAllDeviceStatuses(Empty request, StreamObserver<DeviceStatusResponse> responseObserver) {
        log.info("收到 gRPC 获取所有设备状态请求");

        List<DeviceStatus> allStatuses = edgeGatewayService.getAllDeviceStatuses();

        List<com.smarthome.edge.grpc.DeviceStatus> grpcStatusList = new ArrayList<>();
        for (DeviceStatus status : allStatuses) {
            grpcStatusList.add(com.smarthome.edge.grpc.DeviceStatus.newBuilder()
                    .setDeviceId(status.getDeviceId())
                    .setStatus(status.getStatus())
                    .setDeviceType(status.getDeviceType())
                    .setPower(status.getPower() != null ? status.getPower() : 0.0)
                    .setLastUpdate(status.getLastUpdate() != null ? status.getLastUpdate().toInstant(ZoneOffset.UTC).toEpochMilli() : System.currentTimeMillis())
                    .build());
        }

        DeviceStatusResponse response = DeviceStatusResponse.newBuilder()
                .addAllStatusList(grpcStatusList)
                .build();

        log.info("返回所有设备状态 - 设备数量: {}", grpcStatusList.size());

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
