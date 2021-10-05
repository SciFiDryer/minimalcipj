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
    int paramValue = 0;
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
            int decimalPlaces = buf[1] >> 4;
            //4-7
            paramValue = buf[4] + buf[5] * 256;
            //8-11
            minVal = buf[8] + buf[9] * 256;
            //12-15
            maxVal = buf[12] + buf[13] * 256;
            //16-19
            defaultVal = buf[16] + buf[17] * 256;
            //20-23
            units = new String(Arrays.copyOfRange(buf, 24, 28));
            //28-29
            int multiplier = buf[28] + buf[29] * 256;
            //30-31
            int divisior = buf[30] + buf[31] * 256;
            //32-33
            int base = buf[32] + buf[33] * 256;
            //34-35
            int offset = buf[34] + buf[35] * 256;
            //39-55
            paramName = new String(Arrays.copyOfRange(buf, 39, 56));
            formatter = new CIPDataFormatter(decimalPlaces, multiplier, divisior, base, offset);
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
     * Returns the raw parameter value. This value must be formatted with the CIPDataFormatter object in order to display correctly.
     * @return An integer representing the raw parameter value.
     */
    public int getParamValue()
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
