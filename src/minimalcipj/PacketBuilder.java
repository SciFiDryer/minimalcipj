/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package minimalcipj;
import java.io.*;
/**
 *
 * @author Matt Jamesson scifidryer@gmail.com
 */
public class PacketBuilder {
    public static byte[] REGISTER_SESSION_COMMAND = new byte[] {0x65, 0x00};
    public static byte[] SEND_RR_COMMAND = new byte[] {0x6f, 0x00};
    public static byte[] buildRegisterSessionPacket()
    {
        byte[] commandData = new byte[] {0x01, 0x00, 0x00, 0x00};
        byte[] header = buildHeader(REGISTER_SESSION_COMMAND, new byte[] {0x00, 0x00, 0x00, 0x00}, commandData.length);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.writeBytes(header);
        baos.writeBytes(commandData);
        return baos.toByteArray();
    }
    public static byte[] buildHeader(byte[] commandCode, byte[] sessionHandle, int commandLength)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.writeBytes(commandCode);
        baos.write(commandLength);
        baos.write(0);
        baos.writeBytes(sessionHandle);
        for (int i = 0; i < 16; i++)
        {
            baos.write(0);
        }
        return baos.toByteArray();
    }
    public static byte[] buildGetAttributePacket(byte[] sessionHandle, int cipClass, int cipInstance, int cipAttribute)
    {
        byte[] commandData = buildCommandHeader(0x0e, sessionHandle, cipClass, cipInstance, cipAttribute, 0);
        byte[] header = buildHeader(SEND_RR_COMMAND, sessionHandle, commandData.length);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.writeBytes(header);
        baos.writeBytes(commandData);
        return baos.toByteArray();
    }
    public static byte[] buildCommandHeader(int command, byte[] sessionHandle, int cipClass, int cipInstance, int cipAttribute, int length)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //interface handle
        baos.writeBytes(new byte[] {0x00, 0x00, 0x00, 0x00});
        //timeout
        baos.writeBytes(new byte[] {0x08, 0x00});
        //item count
        baos.writeBytes(new byte[] {0x02, 0x00});
        //type id
        baos.writeBytes(new byte[] {0x00, 0x00});
        //length
        baos.writeBytes(new byte[] {0x00, 0x00});
        //type id
        baos.writeBytes(new byte[] {(byte)0xb2, 0x00});
        //length
        baos.writeBytes(new byte[] {(byte)(0x0A+length), 0x00});
        //command
        baos.write(command);
        //path length
        baos.write(0x04);
        //class id
        baos.write(0x20);
        //requested class
        baos.write(cipClass);
        //instance id
        baos.write(0x25);
        baos.write(0x00);
        //requested instance
        baos.write(cipInstance % 256);
        baos.write(cipInstance/256);
        //attribute id
        baos.write(0x30);
        //requested instance
        baos.write(cipAttribute);
        return baos.toByteArray();
    }
    public static byte[] buildGetAttributeScatteredPacket(byte[] sessionHandle, int cipClass, byte[] params)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //interface handle
        baos.writeBytes(new byte[] {0x00, 0x00, 0x00, 0x00});
        //timeout
        baos.writeBytes(new byte[] {0x08, 0x00});
        //item count
        baos.writeBytes(new byte[] {0x02, 0x00});
        //type id
        baos.writeBytes(new byte[] {0x00, 0x00});
        //length
        baos.writeBytes(new byte[] {0x00, 0x00});
        //type id
        baos.writeBytes(new byte[] {(byte)0xb2, 0x00});
        //length
        baos.writeBytes(new byte[] {(byte)(params.length + 6), 0x00});
        //get attribute scattered
        baos.write(0x4b);
        //path length
        baos.write(0x02);
        //class id
        baos.write(0x20);
        //requested class
        baos.write(cipClass);
        //instance id
        baos.write(0x24);
        //requested instance
        baos.write(0x00);
        baos.writeBytes(params);
        byte[] commandData = baos.toByteArray();
        byte[] header = buildHeader(SEND_RR_COMMAND, sessionHandle, commandData.length);
        baos = new ByteArrayOutputStream();
        baos.writeBytes(header);
        baos.writeBytes(commandData);
        return baos.toByteArray();
    }
    public static byte[] buildSetAttributePacket(byte[] sessionHandle, int cipClass, int cipInstance, int cipAttribute, byte[] data)
    {
        byte[] commandData = buildCommandHeader(0x10, sessionHandle, cipClass, cipInstance, cipAttribute, data.length);
        byte[] header = buildHeader(SEND_RR_COMMAND, sessionHandle, commandData.length + data.length);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.writeBytes(header);
        baos.writeBytes(commandData);
        baos.writeBytes(data);
        return baos.toByteArray();
    }
}
