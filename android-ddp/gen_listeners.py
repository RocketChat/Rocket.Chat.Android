# -*- coding:utf-8 -*-

a='''
        @Override
        public void onOpen(WebSocket webSocket, Response response) {

        }

        @Override
        public void onFailure(IOException e, Response response) {

        }

        @Override
        public void onMessage(ResponseBody responseBody) throws IOException {

        }

        @Override
        public void onPong(Buffer payload) {

        }

        @Override
        public void onClose(int code, String reason) {

        }
'''.strip().split('@Override')

for m in a[1:]:
    m= "        @Override\n        "+m.strip()
    mn = m.split("\n")[1].strip().split(" ")[2].split("(")[0]

    if mn.startswith("on"):
        d=dict()
        d["classname"]=mn[2:]
        params = [p for p in " ".join(m.split("\n")[1].strip()[:-1].split(" throws ")[0].split(" ")[2:]).strip()[len(mn)+1:-1].split(", ") if p.split(" ")[0]!="WebSocket"]

        d["params"]="".join([", "+p for p in params])
        paramnames = [p.split(" ")[-1] for p in params]

        d["paramdefs"]="\n".join(["            public "+p+";" for p in params])
        d["thisis"]="\n".join(["                this.{param} = {param};".format(param=p) for p in paramnames])

#         print '''
#         public static class {classname} extends Base {{
# {paramdefs}
            
#             public {classname}(WebSocket websocket{params}) {{
#                 super("{classname}", websocket);
# {thisis}
#             }}
#         }}'''.format(**d)

        ######################

        x=m.split("\n")
        x[2]='''            mSubscriber.onNext(new RxWebSocketCallback.{classname}(mWebSocket, {params}));'''.format(classname=mn[2:],params=", ".join(paramnames))

        print "\n".join(x)
