/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package minimalcipj;
import java.io.*;
import java.util.*;
import minimalcipj.CIPItemFactory.CIPItem;
/**
 *
 * @author Matt Jamesson scifidryer@gmail.com
 */
public class PacketBuilder {
    public static byte[] REGISTER_SESSION_COMMAND = new byte[] {0x65, 0x00};
    public static byte[] SEND_RR_COMMAND = new byte[] {0x6f, 0x00};
    public static byte[] SEND_UNIT_DATA_COMMAND = new byte[] {0x70, 0x00};
    public static byte[] INTERFACE_HANDLE_CIP = new byte[] {0x00, 0x00, 0x00, 0x00}; 
    //packet structure
    //--------eip header
    byte[] commandCode = new byte[]{}; //2 length
    byte[] commandLength = new byte[]{}; //2 length
    byte[] sessionHandle = new byte[]{}; //4 length
    byte[] status = new byte[]{}; //4 length
    byte[] senderContext = new byte[]{}; //8 length
    byte[] options = new byte[]{}; //4 length
    //--------cip body
    byte[] interfaceHandle = new byte[]{}; //4 length
    byte[] timeout = new byte[]{}; //2 length
    byte[] itemCount = new byte[]{}; //2 length
    ArrayList<CIPItem> cipItems = new ArrayList();
    byte[] itemData = new byte[]{}; //variable length
    byte[] service = new byte[] {}; //1 length
    byte[] requestPathLength = new byte[]{}; //1 length
    byte[] requestPathData = new byte[]{}; //variable length
    byte[] cipData = new byte[]{}; //variable length
    //----------request path
    byte[] classId = new byte[]{};
    byte[] instanceId = new byte[]{};
    byte[] attributeId = new byte[]{};
    public PacketBuilder()
    {
    }
    public void addCIPDataItem(CIPItem item)
    {
        cipItems.add(item);
    }
    public void clearFields()
    {
        commandCode = new byte[]{};
        interfaceHandle = new byte[]{};
        itemCount = new byte[]{};
        clearCIPItems();
        service = new byte[] {};
        requestPathData = new byte[]{};
        cipData = new byte[]{};
        classId = new byte[]{};
        instanceId = new byte[]{};
        attributeId = new byte[]{};
    }
    public void clearCIPItems()
    {
        cipItems.clear();
    }
    public void setCommandCode(byte[] aCommandCode)
    {
        commandCode = aCommandCode;
    }
    public void setSessionHandle(byte[] aSessionHandle)
    {
        sessionHandle = aSessionHandle;
    }
    public void setInterfaceHandle(byte[] aInterfaceHandle)
    {
        interfaceHandle = aInterfaceHandle;
    }
    public void setService(byte[] aService)
    {
        service = aService;
    }
    public void setCIPData(byte[] aCipData)
    {
        cipData = aCipData;
    }
    public void setClassId(byte[] aClassId)
    {
        classId = aClassId;
    }
    public void setAttributeId(byte[] aAttributeId)
    {
        attributeId = aAttributeId;
    }
    public void setInstanceId(byte[] aInstanceId)
    {
        instanceId = aInstanceId;
    }
    public static byte[] intAsBytes(int number)
    {
        return new byte[] {(byte)(number%256), (byte)(number/256)};
    }
    public void setUnimplementedHeaderFields()
    {
        status = new byte[] {0x00, 0x00, 0x00, 0x00};
        senderContext = new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        options = new byte[] {0x00, 0x00, 0x00, 0x00};
    }
    public void setUnimplementedBodyFields()
    {
        timeout = new byte[] {0x01, 0x00};
    }
    public void composePacket()
    {
        setUnimplementedHeaderFields();
        requestPathData = RequestPathFactory.getRequestPath(classId, instanceId, attributeId);
        if (requestPathData.length > 0)
        {
            requestPathLength = new byte[] {(byte)(requestPathData.length/2)};
        }
        composeCIPItems();
        if (interfaceHandle.length + itemCount.length + itemData.length + service.length + requestPathLength.length + requestPathData.length > 0)
        {
            setUnimplementedBodyFields();
        }
        int cipBodyLength = interfaceHandle.length + timeout.length + itemCount.length + itemData.length + service.length + requestPathLength.length + requestPathData.length + cipData.length;
        
        commandLength = PacketBuilder.intAsBytes(cipBodyLength);
    }
    public void composeCIPItems()
    {
        if (cipItems.size() > 0)
        {
            itemCount = PacketBuilder.intAsBytes(cipItems.size());
        }
        Iterator<CIPItem> iterator = cipItems.iterator();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (iterator.hasNext())
        {
            CIPItem currentItem = iterator.next();
            baos.writeBytes(currentItem.getBytes());
        }
        itemData = baos.toByteArray();
    }
    public byte[] getPacketInBytes()
    {
        composePacket();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.writeBytes(commandCode);
        baos.writeBytes(commandLength);
        baos.writeBytes(sessionHandle);
        baos.writeBytes(status);
        baos.writeBytes(senderContext);
        baos.writeBytes(options);
        baos.writeBytes(interfaceHandle);
        baos.writeBytes(timeout);
        baos.writeBytes(itemCount);
        baos.writeBytes(itemData);
        baos.writeBytes(service);
        baos.writeBytes(requestPathLength);
        baos.writeBytes(requestPathData);
        baos.writeBytes(cipData);
        clearFields();
        return baos.toByteArray();
    }
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
    public static byte[] buildGetTagStructurePacket(byte[] sessionHandle, byte[] connectionId, byte[] cipInstance, int seqNo, int start, int length)
    {
        //2 start bytes, 2 00 00 bytes, 2 length bytes
        byte[] startBytes = new byte[] {(byte)(start % 256), (byte)(start/256)};
        byte[] lengthBytes = new byte[] {(byte)(length % 256), (byte)(length/256)};
        byte[] commandData = buildConnectedItemHeader(0x4c, sessionHandle, connectionId, 0x6c, cipInstance, -1, 6, seqNo);
        byte[] header = buildHeader(SEND_UNIT_DATA_COMMAND, sessionHandle, commandData.length + 6);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.writeBytes(header);
        baos.writeBytes(commandData);
        baos.writeBytes(startBytes);
        baos.writeBytes(new byte[] {0x00, 0x00});
        baos.writeBytes(lengthBytes);
        return baos.toByteArray();
    }
    public static byte[] buildGetTagListPacket(byte[] sessionHandle, byte[] connectionId, byte[] cipInstance, int seqNo)
    {
        byte[] data = new byte[] {0x05, 0x00, 0x02, 0x00, 0x07, 0x00, 0x08, 0x00, 0x01, 0x00, 0x0a, 0x00};
        byte[] commandData = buildConnectedItemHeader(0x55, sessionHandle, connectionId, 0x6b, cipInstance, -1, 12, seqNo);
        byte[] header = buildHeader(SEND_UNIT_DATA_COMMAND, sessionHandle, commandData.length + data.length);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.writeBytes(header);
        baos.writeBytes(commandData);
        baos.writeBytes(data);
        return baos.toByteArray();
    }
    public static byte[] buildForwardOpenCommandData()
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.writeBytes(new byte[] {0x00, 0x00, 0x00, 0x00});
        baos.writeBytes(new byte[] {0x0a, 0x00});
        baos.writeBytes(new byte[] {0x02, 0x00});
        baos.writeBytes(new byte[] {0x00, 0x00});
        baos.writeBytes(new byte[] {0x00, 0x00});
        baos.writeBytes(new byte[] {(byte)0xb2, 0x00});
        baos.write(48);
        baos.write(0);
        baos.writeBytes(new byte[] {(byte)0x54, 0x02});
        baos.writeBytes(new byte[] {(byte)0x20, 0x06});
        baos.writeBytes(new byte[] {(byte)0x24, 0x01});
        //priority
        baos.write(6);
        //time out ticks
        baos.write(154);
        //timeout
        baos.writeBytes(new byte[] {0x00, 0x00, 0x00, 0x01});
        baos.writeBytes(new byte[] {0x00, 0x00, 0x00, 0x02});
        baos.writeBytes(new byte[] {0x00, 0x01});
        baos.writeBytes(new byte[] {0x4d, 0x00});
        baos.writeBytes(new byte[] {0x00, 0x00, 0x00, 0x01});
        baos.write(2);
        baos.writeBytes(new byte[] {0x4e, 0x42, 0x54});
        //rpi
        baos.writeBytes(new byte[] {(byte)0x80, (byte)0x84, (byte)0x1e, 0x00});
        baos.writeBytes(new byte[] {(byte)0xf8, 0x43});
        baos.writeBytes(new byte[] {(byte)0x80, (byte)0x84, (byte)0x1e, 0x00});
        baos.writeBytes(new byte[] {(byte)0xf8, 0x43});
        baos.write(0xa3);
        //path length in words
        baos.write(3);
        baos.writeBytes(new byte[] {0x01, 0x00, 0x20, 0x02, 0x24, 0x01});
        return baos.toByteArray();
    }
    public static byte[] buildForwardOpenPacket(byte[] sessionHandle)
    {
        byte[] commandData = buildForwardOpenCommandData();
        byte[] header = buildHeader(SEND_RR_COMMAND, sessionHandle, commandData.length);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.writeBytes(header);
        baos.writeBytes(commandData);
        return baos.toByteArray();
    }
    public static byte[] buildConnectedItemHeader(int command, byte[] sessionHandle, byte[] connectionId, int cipClass, byte[] cipInstance, int cipAttribute, int length, int seqNo)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //interface handle
        baos.writeBytes(new byte[] {0x00, 0x00, 0x00, 0x00});
        //timeout
        baos.writeBytes(new byte[] {0x08, 0x00});
        //item count
        baos.writeBytes(new byte[] {0x02, 0x00});
        //type id
        baos.writeBytes(new byte[] {(byte)0xa1, 0x00});
        //length
        baos.writeBytes(new byte[] {0x04, 0x00});
        baos.writeBytes(connectionId);
        //type id
        baos.writeBytes(new byte[] {(byte)0xb1, 0x00});
        //length
        baos.writeBytes(new byte[] {(byte)(0x0A+length), 0x00});
        //cip sequence
        baos.write(seqNo);
        baos.write(0);
        //command
        baos.write(command);
        //path length
        if (cipAttribute != -1)
        {
            baos.write(0x04);
        }
        else
        {
            baos.write(0x03);
        }
        //class id
        baos.write(0x20);
        //requested class
        baos.write(cipClass);
        //instance id
        baos.write(0x25);
        baos.write(0x00);
        //requested instance
        baos.writeBytes(cipInstance);
        if (cipAttribute != -1)
        {
            //attribute id
            baos.write(0x30);
            //requested instance
            baos.write(cipAttribute);
        }
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
        if (cipAttribute != -1)
        {
            baos.write(0x04);
        }
        else
        {
            baos.write(0x03);
        }
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
        if (cipAttribute != -1)
        {
            //attribute id
            baos.write(0x30);
            //requested instance
            baos.write(cipAttribute);
        }
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
