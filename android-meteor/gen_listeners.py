# -*- coding:utf-8 -*-

a='''
        @Override
        public void onStateChanged(WebSocket websocket, WebSocketState newState) throws Exception {

        }

        @Override
        public void onConnected(WebSocket websocket, Map<String,List<String>> headers) throws Exception {

        }

        @Override
        public void onConnectError(WebSocket websocket, WebSocketException cause) throws Exception {

        }

        @Override
        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {

        }

        @Override
        public void onFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

        }

        @Override
        public void onContinuationFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

        }

        @Override
        public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

        }

        @Override
        public void onBinaryFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

        }

        @Override
        public void onCloseFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

        }

        @Override
        public void onPingFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

        }

        @Override
        public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {

        }

        @Override
        public void onTextMessage(WebSocket websocket, String text) throws Exception {

        }

        @Override
        public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {

        }

        @Override
        public void onFrameSent(WebSocket websocket, WebSocketFrame frame) throws Exception {

        }

        @Override
        public void onFrameUnsent(WebSocket websocket, WebSocketFrame frame) throws Exception {

        }

        @Override
        public void onError(WebSocket websocket, WebSocketException cause) throws Exception {

        }

        @Override
        public void onFrameError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {

        }

        @Override
        public void onMessageError(WebSocket websocket, WebSocketException cause, List<WebSocketFrame> frames) throws Exception {

        }

        @Override
        public void onTextMessageError(WebSocket websocket, WebSocketException cause, byte[] data) throws Exception {

        }

        @Override
        public void onSendError(WebSocket websocket, WebSocketException cause, WebSocketFrame frame) throws Exception {

        }

        @Override
        public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {

        }

        @Override
        public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {

        }
        '''.strip().split('@Override')

for m in a[1:]:
    m= "        @Override\n        "+m.strip()
    mn = m.split("\n")[1].strip().split(" ")[2].split("(")[0]
    if mn.startswith("on"):
        d=dict()
        d["classname"]=mn[2:]
        params = " ".join(m.split("\n")[1].strip().split(" throws ")[0].split(" ")[2:])[len(mn)+1:-1].split(", ")[1:]
        d["params"]="".join([", "+p for p in params])
        paramnames = [p.split(" ")[-1] for p in params]

        d["paramdefs"]="\n".join(["            public "+p+";" for p in params])
        d["thisis"]="\n".join(["                this.{param} = {param};".format(param=p) for p in paramnames])
        print '''
        public static class {classname} extends Base {{
{paramdefs}
            
            public {classname}(String eventName, WebSocket websocket{params}) {{
                super(eventName, websocket);
{thisis}
            }}
        }}'''.format(**d)


print "-"*20
for m in a[1:]:
    m= "        @Override\n        "+m.strip()
    mn = m.split("\n")[1].strip().split(" ")[2].split("(")[0]
    if mn.startswith("on"):
        params = " ".join(m.split("\n")[1].strip().split(" throws ")[0].split(" ")[2:])[len(mn)+1:-1].split(", ")[1:]
        paramnames = [p.split(" ")[-1] for p in params]

        x=m.split("\n")
        x[2]='''            mSubscriber.onNext(new RxWebSocketCallback.{classname}("{classname}", websocket, {params}));'''.format(classname=mn[2:],params=", ".join(paramnames))

        print "\n".join(x)
