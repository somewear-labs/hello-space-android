package com.somewearlabs.hellospace.data.model;

import com.somewearlabs.gen.LocationProto;
import com.somewearlabs.gen.MessageProto;
import com.somewearlabs.gen.PackageProto;

import java.util.Date;

public class ProtoMapper {

    static Message messageFromProto(MessageProto.Message proto) {
        return new Message(
                proto.getEmail(),
                proto.getPhoneNumber(),
                dateFromProto(proto.getTimestamp()),
                proto.getContent());
    }

    static Location locationFromProto(LocationProto.Location proto) {
        return new Location(
                proto.getLatitude(),
                proto.getLongitude(),
                dateFromProto(proto.getTimestamp()));
    }

    public static PackageProto.Package packageFromMessage(Message message) {
        MessageProto.Message proto = MessageProto.Message.newBuilder()
                .setEmail(message.getEmail())
                .setPhoneNumber(message.getPhone())
                .setContent(message.getContent())
                .setTimestamp(timestampProtoFromDate(message.getTimestamp()))
                .build();

        return PackageProto.Package.newBuilder()
                .setType(PackageProto.Package.PackageType.Message)
                .setData(proto.toByteString())
                .build();
    }

    public static PackageProto.Package packageFromLocation(Location location) {
        LocationProto.Location proto = LocationProto.Location.newBuilder()
                .setLatitude(location.getLatitude())
                .setLongitude(location.getLongitude())
                .setTimestamp(timestampProtoFromDate(location.getTimestamp()))
                .build();

        return PackageProto.Package.newBuilder()
                .setType(PackageProto.Package.PackageType.Location)
                .setData(proto.toByteString())
                .build();
    }

    private static long timestampProtoFromDate(Date date) {
        return date.getTime() / 1000;
    }

    private static Date dateFromProto(long proto) {
        return new Date(proto * 1000);
    }
}
