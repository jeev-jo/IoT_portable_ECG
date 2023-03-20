import websocket
import matplotlib.pyplot as plt
plt.style.use('seaborn-whitegrid')
import numpy as np
 
ws = websocket.WebSocket()
ws.connect("ws://192.168.43.189/ws")
i = 0
x = []
y = []
nrOfMessages = 500
while i<nrOfMessages:
    result = ws.recv()
    print(result)
    x.append(result)
    y.append(i)
    i=i+1
    
ws.close()
fig = plt.figure()
ax = plt.axes()
plt.plot(y,x)
plt.show()
