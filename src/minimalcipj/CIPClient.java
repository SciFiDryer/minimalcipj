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
        clientSocket.close();
        clientSocket = null;
        is = null;
        os = null;
    }
    private void openSession() throws Exception
    {
        byte[] buf = new byte[256];
        os.write(PacketBuilder.buildRegisterSessionPacket());
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
        
        os.write(PacketBuilder.buildGetAttributePacket(sessionHandle, cipClass, cipInstance, cipAttribute));
        return processResponse(cipClass, cipInstance, cipAttribute);
        
    }
    private CIPResponse processResponse(int cipClass, int cipInstance, int cipAttribute) throws Exception
    {
        byte[] buf = new byte[256];
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
        if (!(buf[0] == 0x6f && buf[1] == 0x00))
        {
            throw new CIPException("Send RR data not received");
        }
        if (buf[42] != 0x00)
        {
            throw new CIPException("Unsuccessful CIP transaction");
        }
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
}
