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
import java.io.*;
/**
 *
 * @author Matt Jamesson scifidryer@gmail.com
 */
public class CIPItemFactory {
    public static CIPItem getConnectedAddressItem(byte[] connectedAddress)
    {
        return new CIPItem(new byte[] {(byte)0xa1, 0x00}, connectedAddress.length, connectedAddress);
    }
    public static CIPItem getConnectedDataItem(int length, int seqNo)
    {
        return new CIPItem(new byte[] {(byte)0xb1, 0x00}, length+2, PacketBuilder.intAsBytes(seqNo));
    }
    public static CIPItem getNullAddressItem()
    {
        return new CIPItem(new byte[] {0x00, 0x00}, 0, new byte[]{});
    }
    public static CIPItem getUnconnectedDataItem(int length)
    {
        return new CIPItem(new byte[] {(byte)0xb2, 0x00}, 10+length, new byte[]{});
    }
    static class CIPItem
    {
        byte[] typeId = null;
        byte[] length = null;
        byte[] data = null;
        private CIPItem(byte[] aTypeId, int aLength, byte[] aData)
        {
            typeId = aTypeId;
            length = PacketBuilder.intAsBytes(aLength);
            data = aData;
        }
        public byte[] getBytes()
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.writeBytes(typeId);
            baos.writeBytes(length);
            baos.writeBytes(data);
            return baos.toByteArray();
        }
    }
}
