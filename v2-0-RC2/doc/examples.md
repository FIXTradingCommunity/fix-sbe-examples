### Wire format
```
00 00 00 48 eb 50 36 00 63 00 5b 00 00 00 00 00 :   H P6 c [     
00 00 4f 52 44 30 30 30 30 31 41 43 43 54 30 31 :  ORD00001ACCT01
00 00 47 45 4d 34 00 00 00 00 31 c0 1a 31 96 2a :  GEM4    1  1 *
5e b0 15 07 00 00 00 32 1a 85 01 00 00 00 00 00 :^      2        
00 00 00 00 00 00 00 80                         :        
```
### Interpretation
|Wire format|Field ID|Name|Offset|Length|Interpreted value|
|-----------|-------:|----|-----:|-----:|-----------------|
| `00000048` |   | SOFH message length | 0 | 4 | 72 |
| `eb50` |   | SOFH encoding | 4 | 2 | SBE little-endian |
| `3600` |   | SBE root block length | 0 | 2 | 54 |
| `6300` |   | SBE template ID | 2 | 2 | 99 |
| `5b00` |   | SBE schema ID | 4 | 2 | 91 |
| `0000` |   | SBE schema version | 6 | 2 | 0 |
| `0000` |   | No. of groups | 8 | 2 | 0 |
| `0000` |   | No. of var data | 10 | 2 | 0 |
| `4f52443030303031` | 11 | ClOrdId | 0 | 8 | ORD00001 |
| `4143435430310000` | 1 | Account | 8 | 8 | ACCT01 |
| `47454d3400000000` | 55 | Symbol | 16 | 8 | GEM4 |
| `31` | 54 | Side | 24 | 1 | Buy |
| `c01a31962a5eb015` | 60 | TransactTime | 25 | 8 | 2019-07-11T13:43:27.699Z |
| `07000000` | 38 | OrderQty | 33 | 4 | 7 |
| `32` | 40 | OrdType | 37 | 1 | Limit |
| `1a85010000000000` | 44 | Price | 38 | 8 | 99.610 |
| `0000000000000080` | 99 | StopPx | 46 | 8 | null |
### Wire format
```
00 00 00 5c eb 50 2a 00 62 00 5b 00 00 00 01 00 :   \ P* b [     
00 00 4f 30 30 30 30 30 30 31 45 58 45 43 30 30 :  O0000001EXEC00
30 30 46 31 47 45 4d 34 00 00 00 00 de 07 06 ff :00F1GEM4        
ff 31 01 00 00 00 06 00 00 00 75 3e 0c 00 02 00 : 1        u>    
00 00 00 00 1a 85 01 00 00 00 00 00 02 00 00 00 :                
24 85 01 00 00 00 00 00 04 00 00 00             :$           
```
### Interpretation
|Wire format|Field ID|Name|Offset|Length|Interpreted value|
|-----------|-------:|----|-----:|-----:|-----------------|
| `0000005c` |   | SOFH message length | 0 | 4 | 92 |
| `eb50` |   | SOFH encoding | 4 | 2 | SBE little-endian |
| `2a00` |   | SBE root block length | 0 | 2 | 42 |
| `6200` |   | SBE template ID | 2 | 2 | 98 |
| `5b00` |   | SBE schema ID | 4 | 2 | 91 |
| `0000` |   | SBE schema version | 6 | 2 | 0 |
| `0100` |   | No. of groups | 8 | 2 | 1 |
| `0000` |   | No. of var data | 10 | 2 | 0 |
| `4f30303030303031` | 37 | OrderID | 8 | 8 | O0000001 |
| `4558454330303030` | 17 | ExecID | 8 | 8 | EXEC0000 |
| `31` | 39 | OrdStatus | 1 | 1 | PartialFilled |
| `47454d3400000000` | 55 | Symbol | 18 | 8 | GEM4 |
| `de0706ffff` | 200 | MaturityMonthYear | 26 | 5 | year=2014 month=6 |
| `31` | 54 | Side | 1 | 1 | Buy |
| `01000000` | 151 | LeavesQty | 32 | 4 | 1 |
| `06000000` | 14 | CumQty | 36 | 4 | 6 |
| `753e` | 75 | TradeDate | 40 | 2 | 2013-10-11 |
| `0c00` |   | Group block length | 0 | 2 | 12 |
| `0000` |   | NumInGroup | 4 | 2 | 2 |
| `0000` |   | No. of groups | 4 | 2 | 0 |
| `0000` |   | No. of var data | 6 | 2 | 0 |
| `1a85010000000000` | 1364 | FillPx | 0 | 8 | 99.610 |
| `02000000` | 1365 | FillQty | 8 | 4 | 2 |
| `2485010000000000` | 1364 | FillPx | 0 | 8 | 99.620 |
| `04000000` | 1365 | FillQty | 8 | 4 | 4 |
### Wire format
```
00 00 00 44 eb 50 09 00 61 00 5b 00 00 00 00 00 :   D P  a [     
01 00 4f 52 44 30 30 30 30 31 06 27 00 4e 6f 74 :  ORD00001 ' Not
20 61 75 74 68 6f 72 69 7a 65 64 20 74 6f 20 74 : authorized to t
72 61 64 65 20 74 68 61 74 20 69 6e 73 74 72 75 :rade that instru
6d 65 6e 74                                     :ment
```
### Interpretation
|Wire format|Field ID|Name|Offset|Length|Interpreted value|
|-----------|-------:|----|-----:|-----:|-----------------|
| `00000044` |   | SOFH message length | 0 | 4 | 68 |
| `eb50` |   | SOFH encoding | 4 | 2 | SBE little-endian |
| `0900` |   | SBE root block length | 0 | 2 | 9 |
| `6100` |   | SBE template ID | 2 | 2 | 97 |
| `5b00` |   | SBE schema ID | 4 | 2 | 91 |
| `0000` |   | SBE schema version | 6 | 2 | 0 |
| `0000` |   | No. of groups | 8 | 2 | 0 |
| `0100` |   | No. of var data | 10 | 2 | 1 |
| `4f52443030303031` | 379 | BusinesRejectRefId | 0 | 8 | ORD00001 |
| `06` | 380 | BusinessRejectReason | 8 | 1 | NotAuthorized |
| `4e6f74206175...` | 58 | Text | 0 | 39 | Not authorized to trade that instrument |
