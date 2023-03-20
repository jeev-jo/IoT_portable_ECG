import websocket
 
ws = websocket.WebSocket()
ws.connect("ws://192.168.43.189/ws")
#i = 0
#nrOfMessages = 200
while (1):#i<nrOfMessages:
    result = ws.recv()
    print(result)
    #i=i+1
#ws.close()
