# minimalcipj
Minimal CIP, Ethernet/IP library in java used by ProtocolWhisperer. GetAttribute and SetAttribute for PowerFlex VFDs and other CIP devices supported.

## Overview

This library allows GetAttribute and SetAttribute requests to network capable CIP devices such as PowerFlex drives. The library is implemented in ProtocolWhisperer to allow logging of parameters.

## Supported objects

Presently the only supported object is class 0x93 DPI Parameter object. Supported attributes are 7 - DPIOnlineReadFull and 9 - Parameter Value.

## Example

Reading the first 400 drive parameters
```
CIPClient client = new CIPClient("192.168.1.10");
try
{
    client.connect();
    for (int i = 1; i < 400; i++)
    {
        DPIOnlineReadFullResponse response = (DPIOnlineReadFullResponse)client.getAttribute(0x93, i, 0x07);
        String readWrite = "RO";
        if (!response.getReadOnly())
        {
            readWrite = "RW";
        }
        System.out.println(readWrite + " " + response.getParamName() + ": " + response.getParamValue() + response.getUnits());
    }
}
catch(Exception e)
{
    e.printStackTrace();
}
```
Writing parameters
```
CIPClient client = new CIPClient("192.168.1.10");
try
{
    client.connect();
    DPIOnlineReadFullResponse response = (DPIOnlineReadFullResponse)client.getAttribute(0x93, 52, 0x07);
    //get the data formatter to correctly format the bytes
    CIPDataFormatter formatter = response.getCIPDataFormatter();
    //set the average kWh Cost to $0.13/hr
    CIPResponse setResponse = client.setAttribute(0x93, 52, 0x09, formatter.displayValueToBytes(0.13));
}
catch(Exception e)
{
    e.printStackTrace();
}
```
## Javadocs

https://scifidryer.github.io/minimalcipj/
