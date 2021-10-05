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

/**
 *
 * @author Matt Jamesson scifidryer@gmail.com
 */
public class CIPDataFormatter {
    int decimalPlaces = 0;
    int multiplier = 0;
    int divisior = 0;
    int base = 0;
    int offset = 0;
    
    public CIPDataFormatter(int aDecimalPlaces, int aMultiplier, int aDivisior, int aBase, int aOffset)
    {
        decimalPlaces = aDecimalPlaces;
        multiplier = aMultiplier;
        divisior = aDivisior;
        base = aBase;
        offset = aOffset;
    }
    /**
     * Returns a double representing the formatted value of the parameter.
     * @param paramValue The raw parameter value.
     * @return The formatted parameter value.
     */
    public double getDisplayVal(int paramValue)
    {
        return ((paramValue+offset)*multiplier*base) / (divisior*Math.pow(10, decimalPlaces));
    }
    /**
     * Returns a double representing the formatted value of the parameter.
     * @param buf The raw parameter value.
     * @return The formatted parameter value.
     */
    public double getDisplayVal(byte[] buf)
    {
        int paramValue = buf[0] + buf[1] * 256;
        return getDisplayVal(paramValue);
    }
    /**
     * Returns bytes to use SetAttribute from a displayValue.
     * @param displayValue The double displayValue to convert to bytes.
     * @return byte[] representing the displayValue usable in SetAttribute.
     */
    public byte[] displayValueToBytes(double displayValue)
    {
        int paramValue = (int)(displayValue*divisior*Math.pow(10, decimalPlaces))/((multiplier*base)-offset);
        return new byte[] {(byte)(paramValue%256), (byte)(paramValue/256)};
    }
}
