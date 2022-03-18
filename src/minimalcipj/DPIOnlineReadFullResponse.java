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
import java.util.*;
/**
 *
 * @author Matt Jamesson scifidryer@gmail.com
 */
public class DPIOnlineReadFullResponse extends CIPResponse{
    boolean readOnly = true;
    double paramValue = 0;
    int minVal = 0;
    int maxVal = 0;
    int defaultVal = 0;
    String units = "";
    String paramName = "";
    CIPDataFormatter formatter = null;
    public DPIOnlineReadFullResponse(byte[] aBuf)
    {
        super(aBuf);
        if (buf.length == 56)
        {
            if ((buf[1] & 00000001) == 1)
            {
                readOnly = false;
            }
            int dataType = (buf[0] & 0b00000111) ;
            int decimalPlaces = (buf[1] >> 4) ;
            
            //8-11
            minVal = buf[8] + buf[9] * 256;
            //12-15
            maxVal = buf[12] + buf[13] * 256;
            //16-19
            defaultVal = buf[16] + buf[17] * 256;
            //20-23
            units = new String(Arrays.copyOfRange(buf, 24, 28));
            //28-29
            int multiplier = (buf[28] & 0xff) + (buf[29] & 0xff) * 256;
            //30-31
            int divisior = (buf[30] & 0xff) + (buf[31] & 0xff) * 256;
            //32-33
            int base = (buf[32] & 0xff) + (buf[33] & 0xff) * 256;
            //34-35
            int offset = buf[34] + buf[35] * 256;
            //39-55
            paramName = new String(Arrays.copyOfRange(buf, 39, 56));
            formatter = new CIPDataFormatter(decimalPlaces, multiplier, divisior, base, offset);
            //4-7
            if (dataType == 6)
            {
                java.nio.ByteBuffer bb = java.nio.ByteBuffer.allocate(4);
                bb.put(buf[7]);
                bb.put(buf[6]);
                bb.put(buf[5]);
                bb.put(buf[4]);
                paramValue = bb.getFloat(0);
            }
            else
            {
                paramValue = (buf[4] & 0xff) + ((buf[5] & 0xff) * 256) + ((buf[6] & 0xff) * 65536) + ((buf[7] & 0xff) * 16777216);
                paramValue = formatter.getDisplayVal((int)paramValue);
            }
            
        }
    }
    /**
     * Returns the CIPDataFormatter object.
     * @return The data formatter object.
     */
    public CIPDataFormatter getCIPDataFormatter()
    {
        return formatter;
    }
    /**
     * Returns the formatted parameter value.
     * @return A double representing the formatted parameter value.
     */
    public double getParamValue()
    {
        return paramValue;
    }
    /**
     * Returns the parameter name as reported by the device.
     * @return The parameter name.
     */
    public String getParamName()
    {
        return paramName;
    }
    /**
     * Returns the units as reported by the device.
     * @return String Units.
     */
    public String getUnits()
    {
        return units;
    }
    public boolean getReadOnly()
    {
        return readOnly;
    }
}
