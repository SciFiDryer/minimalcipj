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
public class TagListResponse extends CIPResponse{
    byte[] nextInstance = null;
    boolean partialFlag = false;
    ArrayList<ControllerTag> tags = new ArrayList<ControllerTag>();
    public TagListResponse(boolean aPartialFlag, byte[] aBuf)
    {
        super(aBuf);
        partialFlag = aPartialFlag;
        int position = 1;
        while (position < buf.length)
        {
            byte[] workingMetaData = Arrays.copyOfRange(buf, position, position+23);
            position = position + 23;
            if (position < buf.length)
            {
                byte[] workingTagName = Arrays.copyOfRange(buf, position, position+workingMetaData[21]);
                nextInstance = new byte[] {(byte)(workingMetaData[1]+1), workingMetaData[2]};
                tags.add(new ControllerTag(workingMetaData, new String(workingTagName)));
                position = position + workingMetaData[21];
            }
        }
    }
    public byte[] getNextInstance()
    {
        return nextInstance;
    }
    public boolean isPartial()
    {
        return partialFlag;
    }
    public ArrayList<ControllerTag> getTags()
    {
        return tags;
    }
}
