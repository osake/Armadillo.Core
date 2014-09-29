package Armadillo.Communication.Impl.ReqResp;

import java.io.Closeable;
import java.io.IOException;

public class ResponseServerTaskWrapper implements Closeable {
	
	        public ResponseServerTask ResponseServerTask;
	        
	        public ResponseServerTaskWrapper(ResponseServerTask responseServerTask)
	        {
	            ResponseServerTask = responseServerTask;
	        }

	        public void Dispose()
	        {
	            ResponseServerTask = null;
	        }

			@Override
			public void close() throws IOException {
				Dispose();
			}
}
