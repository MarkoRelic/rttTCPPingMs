# rttTCPPingMs
## Calculate RTT in ms with TCP ping

###### To start Catcher, in bin direktory run this command:
```
> java TCPPing -c -bind 192.168.0.1 -port 9900
```
###### Output should look like:
```
PingCatcher created! (bind address: 192.168.0.1, port: 9900)
Waiting for a pitcher ...
```

###### To start Pitchera, in bin run this command:
```
> java TCPPing -p -port 9900 -mps 900 -size 3000 compCatcher 
```
###### Output should look like: (compCatcher in this case is DESKTOP-VSGI23D):
```
PingPitcher created! (hostname:  DESKTOP-VSGI23D, port: 9900, mps: 900, size: 3000)
Connected to Catcher:  DESKTOP-VSGI23D/192.168.8.108:9900
|     time |    msgs |  msgs/s |  avgABA |  maxABA |   avgAB |   avgBA |
| 21:55:04 |    1022 |    1022 |    0.16 |   16.00 |    0.08 |    0.08 |
| 21:55:05 |    1993 |     971 |    0.16 |   16.00 |    0.08 |    0.08 |
| 21:55:06 |    2996 |    1003 |    0.06 |   16.00 |    0.03 |    0.03 |
| 21:55:07 |    3995 |     999 |    0.01 |   16.00 |    0.00 |    0.00 |
| 21:55:08 |    4996 |    1001 |    0.14 |   16.00 |    0.07 |    0.07 |
| 21:55:09 |    5998 |    1002 |    0.03 |   16.00 |    0.01 |    0.01 |
| 21:55:10 |    6989 |     991 |    0.05 |   16.00 |    0.03 |    0.03 |
...
```
###### After ending Pitcher (Ctrl+c) output should look like:
```
Messages sent successfully: 14842
Messages sent failed: 0

Exiting Pitcher, Goodbye!
```

###### java version "12.0.2" 2019-07-16 is used

