/*
 * Copyright 2021 Matt Jamesson <scifidryer@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package minimalcipj;
import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;
import java.util.concurrent.*;
/**
 *
 * @author Matt Jamesson scifidryer@gmail.com
 */

public class CIPClient {
    String clientHost = null;
    Socket clientSocket = null;
    InputStream is = null;
    OutputStream os = null;
    byte[] sessionHandle = null;
    PacketBuilder builder = new PacketBuilder();
    int seqNo = 1;
    byte[] connectionId = null;
    ArrayList<ControllerTag> tags = new ArrayList<ControllerTag>();
    /**
    * Constructs a CIPClient object that will connect to the supplied host
    * @param host IP or host of the CIP device to connect to
    */
    public CIPClient(String host)
    {
        clientHost = host;
    }
    /**
    * Connects the CIPClient object to the host.
    * @throws java.lang.Exception if IO or processing errors occur.
    */
    public void connect() throws Exception
    {
        clientSocket = new Socket(clientHost, 44818);
        clientSocket.setSoTimeout(3000);
        is = clientSocket.getInputStream();
        os = clientSocket.getOutputStream();
        openSession();
    }
    /**
    * Disconnects the CIPClient from the host.
    * @throws java.io.IOException if IO or processing errors occur.
    */
    public void disconnect() throws IOException
    {
        if (clientSocket != null)
        {
            clientSocket.close();
        }
        clientSocket = null;
        is = null;
        os = null;
    }
    private void openSession() throws Exception
    {
        byte[] buf = new byte[256];
        builder.setCommandCode(PacketBuilder.REGISTER_SESSION_COMMAND);
        builder.setSessionHandle(new byte[] {0x00, 0x00, 0x00, 0x00});
        builder.setCIPData(new byte[] {0x01, 0x00, 0x00, 0x00});
        os.write(builder.getPacketInBytes());
        int len = is.read(buf);
        if (len > 0)
        {
            buf = Arrays.copyOf(buf, len);
            processSessionPacket(buf);
        }
        else
        {
            throw new CIPException("Failed to recieve session packet");
        }
    }
    private void processSessionPacket(byte[] buf) throws CIPException
    {
        if (buf[0] == 0x65 && buf[1] == 0x00 && buf.length >= 8)
        {
            sessionHandle = Arrays.copyOfRange(buf, 4, 8);
            builder.setSessionHandle(sessionHandle);
        }
        else
        {
            throw new CIPException("Invalid session handle");
        }
    }
    /**
    * Executes a GetAttributeSingle on the connected host.
    * @param cipClass The class of the GetAttribute request. The only supported class is 0x93.
    * @param cipInstance The instance of the GetAttribute request. Instance is the parameter number on VFDs.
    * @param cipAttribute The attribute of the GetAttribute request. Supported attributes are: <ul><li>0x07 DPI Online Read Full - returns DPIOnlineReadFullResponse</li><li>0x09 Parameter value - returns CIPResponse</li></ul>Classes/attributes outside the supported list will return a raw CIPResponse. The CIPResponse object will contain the payload of the received bytes.
    * @return CIPResponse
    * @throws java.lang.Exception if IO or processing errors occur.
    */
    public CIPResponse getAttribute(int cipClass, int cipInstance, int cipAttribute) throws Exception
    {
        builder.setCIPData(new byte[]{});
        builder.setCommandCode(PacketBuilder.SEND_RR_COMMAND);
        builder.setService(new byte[]{0x0e});
        builder.setClassId(new byte[]{(byte)cipClass});
        builder.setInstanceId(PacketBuilder.intAsBytes(cipInstance));
        builder.setAttributeId(new byte[] {(byte)cipAttribute});
        builder.setInterfaceHandle(PacketBuilder.INTERFACE_HANDLE_CIP);
        builder.clearCIPItems();
        builder.addCIPDataItem(CIPItemFactory.getNullAddressItem());
        builder.addCIPDataItem(CIPItemFactory.getUnconnectedDataItem(0));
        os.write(builder.getPacketInBytes());
        return processResponse(cipClass, cipInstance, cipAttribute);
        
    }
    private CIPResponse processResponse(int cipClass, int cipInstance, int cipAttribute) throws Exception
    {
        byte[] buf = new byte[1024];
        int len = 0;
        try
        {
            len = is.read(buf);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        buf = Arrays.copyOf(buf, len);
        //check sesion handle
        if (len >= 7)
        {
            byte[] sessionRecv = Arrays.copyOfRange(buf, 4, 8);
            if (!Arrays.equals(sessionRecv, sessionHandle))
            {
                throw new CIPException("Invalid session handle received");
            }
        }
        else
        {
            throw new CIPException("Bad length in response packet");
        }
        if (buf[8] != 0x00 || buf[9] != 0x00 || buf[10] != 0x00 || buf[11] != 0x00)
        {
            throw new CIPException("Unsuccessful CIP transaction");
        }
        //got send rr data
        if (buf[0] == 0x6f && buf[1] == 0x00)
        {
            buf = Arrays.copyOfRange(buf, 44, len);
            //DPI parameter object
            if (cipClass == 0x93)
            {
                //DPI online read
                if (cipAttribute == 0x07)
                {
                    return new DPIOnlineReadFullResponse(buf);
                }
                //DPI parameter value
                if (cipAttribute == 0x09)
                {
                    return new CIPResponse(buf);
                }
            }
            //connection manager
            if (cipClass == 0x06)
            {
                return new ForwardOpenResponse(buf);
            }
            
        }
        //got send unit data
        else if (buf[0] == 0x70 && buf[1] == 0x00)
        {
            boolean successFlag = false;
            boolean partialFlag = false;
            if (buf[48] == 0x06 && buf[49] == 0x00)
            {
                partialFlag = true;
            }
            if (buf[48] == 0x00 && buf[49] == 0x00)
            {
                successFlag = true;
            }
            if (!partialFlag && !successFlag)
            {
                throw new CIPException("Bad CIP response");
            }
            buf = Arrays.copyOfRange(buf, 48, len);
            if (cipClass == 0x6b)
            {
                return new TagListResponse(partialFlag, buf);
            }
            if (cipClass == 0x6c)
            {
                return new UDTTagResponse(buf);
            }
        }
        return new CIPResponse(buf);
    }
    /**
    * Executes a SetAttributeSingle on the connected host.
    * @param cipClass The class of the SetAttribute request. The only supported class is 0x93.
    * @param cipInstance The instance of the SetAttribute request. Instance is the parameter number on VFDs.
    * @param cipAttribute The attribute of the SetAttribute request.
    * @param data The value to set the parameter to expressed in bytes.
    * @return CIPResponse
    * @throws java.lang.Exception if IO or processing errors occur.
    */
    public CIPResponse setAttribute(int cipClass, int cipInstance, int cipAttribute, byte[] data) throws Exception
    {
        
        os.write(PacketBuilder.buildSetAttributePacket(sessionHandle, cipClass, cipInstance, cipAttribute, data));
        return processResponse(cipClass, cipInstance, cipAttribute);
    }
    public void forwardOpen() throws Exception
    {
        builder.setCommandCode(PacketBuilder.SEND_RR_COMMAND);
        builder.setInterfaceHandle(PacketBuilder.INTERFACE_HANDLE_CIP);
        builder.addCIPDataItem(CIPItemFactory.getNullAddressItem());
        builder.addCIPDataItem(CIPItemFactory.getUnconnectedDataItem(40));
        builder.setService(new byte[]{(byte)0x54});
        builder.setClassId(new byte[]{0x06});
        builder.setInstanceId(new byte[]{0x01, 0x00});
        byte[] data = new byte[] {0x06,
            (byte)0x9a, //time out ticks
            0x00, 0x00, 0x00, 0x01, //connection id
            0x00, 0x00, 0x00, 0x01, //connection id
            0x00, 0x00, //serial number
            0x4d, 0x00, //vendor id
            0x00, 0x00, 0x00, 0x01, //serial number
            0x02, //timeout multiplier
            0x01, 0x24, 0x01, //reserved
            (byte)0x80, (byte)0x84, 0x1e, 0x00, //rpi
            (byte)0xf8, 0x43, //parameters
            (byte)0x80, (byte)0x84, 0x1e, 0x00, //rpi
            (byte)0xf8, 0x43, //parameters
            (byte)0xa3, //transport type
            0x03, //connection path size in words
            0x01, 0x00, //backplane port
            0x20, 0x02, //message router class
            0x24, 0x01 //instance 1
        };
        builder.setCIPData(data);
        os.write(builder.getPacketInBytes());
        ForwardOpenResponse response = (ForwardOpenResponse)processResponse(0x06, 0, 0);
        connectionId = response.getConnectionId();
    }
    public void getTagList() throws Exception
    {
        if (connectionId == null)
        {
            forwardOpen();
        }
        prepareGetTagListPacket(new byte[] {0x00, 0x00});
        os.write(builder.getPacketInBytes());
        TagListResponse tagResponse = (TagListResponse)processResponse(0x6b, 0x00, 0x00);
        addTags(tagResponse.getTags());
        while (tagResponse.isPartial())
        {
            seqNo++;
            prepareGetTagListPacket(tagResponse.getNextInstance());
            os.write(builder.getPacketInBytes());
            tagResponse = (TagListResponse)processResponse(0x6b, 0x00, 0x00);
            addTags(tagResponse.getTags());
        }
        seqNo++;
    }
    public void getTagStructure(String tagName) throws Exception
    {
        ControllerTag tag = findTagByName(tagName);
        builder.setCommandCode(PacketBuilder.SEND_UNIT_DATA_COMMAND);
        builder.setService(new byte[] {0x01});
        builder.setInterfaceHandle(PacketBuilder.INTERFACE_HANDLE_CIP);
        builder.addCIPDataItem(CIPItemFactory.getConnectedAddressItem(connectionId));
        builder.addCIPDataItem(CIPItemFactory.getConnectedDataItem(8, seqNo));
        builder.setClassId(new byte[] {0x6c});
        builder.setInstanceId(tag.getInstanceId());
        os.write(builder.getPacketInBytes());
        UDTTagResponse response = (UDTTagResponse)processResponse(0x6c, 0x00, 0x00);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.writeBytes(new byte[] {0x00, 0x00});
        baos.writeBytes(new byte[] {0x00, 0x00});
        baos.writeBytes(PacketBuilder.intAsBytes(response.getTagStructureLength()));
        byte[] data = baos.toByteArray();
        seqNo++;
        builder.setCommandCode(PacketBuilder.SEND_UNIT_DATA_COMMAND);
        builder.setService(new byte[] {0x4c});
        builder.setInterfaceHandle(PacketBuilder.INTERFACE_HANDLE_CIP);
        builder.addCIPDataItem(CIPItemFactory.getConnectedAddressItem(connectionId));
        builder.addCIPDataItem(CIPItemFactory.getConnectedDataItem(data.length+8, seqNo));
        builder.setClassId(new byte[] {0x6c});
        builder.setInstanceId(tag.getInstanceId());
        builder.setCIPData(data);
        os.write(builder.getPacketInBytes());
    }
    public void addTags(ArrayList<ControllerTag> aTags)
    {
        for (int i = 0; i < aTags.size(); i++)
        {
            tags.add(aTags.get(i));
        }
    }
    public void prepareGetTagListPacket(byte[] instanceId)
    {
        byte[] data = new byte[] {0x05, 0x00, 0x02, 0x00, 0x07, 0x00, 0x08, 0x00, 0x01, 0x00, 0x0a, 0x00};
        builder.setCommandCode(PacketBuilder.SEND_UNIT_DATA_COMMAND);
        builder.setService(new byte[] {0x55});
        builder.setInterfaceHandle(PacketBuilder.INTERFACE_HANDLE_CIP);
        builder.addCIPDataItem(CIPItemFactory.getConnectedAddressItem(connectionId));
        builder.addCIPDataItem(CIPItemFactory.getConnectedDataItem(data.length+8, seqNo));
        builder.setClassId(new byte[] {0x6b});
        builder.setInstanceId(instanceId);
        builder.setCIPData(data);
    }
    public ControllerTag findTagByName(String name)
    {
        for (int i = 0; i < tags.size(); i++)
        {
            ControllerTag currentTag = tags.get(i);
            if (currentTag.getTagName().contains(name))
            {
                return currentTag;
            }
        }
        return null;
    }
}
