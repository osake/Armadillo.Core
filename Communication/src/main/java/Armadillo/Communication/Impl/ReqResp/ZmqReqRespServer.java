package Armadillo.Communication.Impl.ReqResp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.math3.util.Precision;
import org.joda.time.DateTime;

import Armadillo.Core.Config;
import Armadillo.Core.Console;
import Armadillo.Core.HCException;
import Armadillo.Core.Logger;
import Armadillo.Core.NetworkHelper;
import Armadillo.Core.ObjectWrapper;
import Armadillo.Communication.Impl.CommunicationConstants;
import Armadillo.Communication.Impl.SimpleUiSocket;
import Armadillo.Communication.Impl.Topic.TopicConstants;
import Armadillo.Communication.Impl.Topic.TopicPublisherCache;
import Armadillo.Communication.zmq.zmq.Ctx;
import Armadillo.Communication.zmq.zmq.Msg;
import Armadillo.Communication.zmq.zmq.SocketBase;
import Armadillo.Communication.zmq.zmq.ZMQ;
import Armadillo.Core.Concurrent.ThreadWorker;
import Armadillo.Core.Serialization.ISerializerReader;
import Armadillo.Core.Serialization.ISerializerWriter;
import Armadillo.Core.Serialization.Serializer;
import Armadillo.Core.Text.StringHelper;

public class ZmqReqRespServer extends ReqRespServer {

	public static ConcurrentHashMap<String, ResponseServerTask> MapRequestToTask;

	private static boolean m_blnIsConnected;
	private static final Object m_connectLockObj = new Object();
	private static int m_intConnectionsLoaded;
	private static final Object m_connectionsLoadedLock = new Object();
	public static final Object m_RequestResponseLock = new Object();
	private static final ThreadWorker<ObjectWrapper> m_pingWhoIsWrapper;
	private ZmqReqRespServerThreadWorker m_baseConnection;

	static {
		
    	final String strServerName = Config.getStringStatic(
    			"TopicServerName",
    			SimpleUiSocket.class);
		
		MapRequestToTask = new ConcurrentHashMap<String, ResponseServerTask>();
		m_pingWhoIsWrapper = new ThreadWorker<ObjectWrapper>() {
			@Override
			public void runTask(ObjectWrapper item) {
				PingWhoIs(strServerName);
			}
		};
		// m_pingWhoIsWrapper.OnExecute += PingWhoIs;
		m_pingWhoIsWrapper.work();
	}

	@Override
	protected void Connect(
			String strServerName, 
			int intPort, 
			int intConnections) 
	{
		if (!m_blnIsConnected) 
		{
			synchronized (m_connectLockObj) 
			{
				if (!m_blnIsConnected) 
				{
					ZmqReqRespServerAck zmqReqRespServerAck = new ZmqReqRespServerAck(
							strServerName);
					List<ZmqReqRespServerThreadWorker> threads = new ArrayList<ZmqReqRespServerThreadWorker>();
					if (!strServerName.equals("local")) 
					{
						// m_intPort = intPort;
						// m_intConnections = intConnections;
						for (int i = 0; i < intConnections; i++) {
							threads.add(new ZmqReqRespServerThreadWorker(
									strServerName, intPort, i,
									zmqReqRespServerAck));
						}
						//
						// load base connection
						//
						m_baseConnection = new ZmqReqRespServerThreadWorker(
								strServerName, intPort, -1, zmqReqRespServerAck);
						threads.add(m_baseConnection);
					}

					while (m_intConnectionsLoaded < intConnections + 1) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							Logger.log(e);
						}
					}
					m_blnIsConnected = true;
				}
			}
		}
	}

	private static void PingWhoIs(
			String strServerName) {
		while (true) {
			
			
			try {
				Set<Entry<String, ResponseServerTask>> mapRequestToTaskArr = MapRequestToTask
						.entrySet();
				if (MapRequestToTask != null) {
					
					for(Entry<String, ResponseServerTask> kvp : mapRequestToTaskArr){
						
						ResponseServerTask responseServerTask = kvp.getValue();
						if (responseServerTask != null) {
							
							if (responseServerTask.BlnWait
								&& responseServerTask.IsClientConnected()) {
								
								WhoIsWrapper whoIsWrapper = responseServerTask.WhoIsWrapper;
								if (whoIsWrapper != null) {
									//
									// ping request in process
									//
									TopicPublisherCache.GetPublisher(
											strServerName,
											TopicConstants.SUBSCRIBER_HEART_BEAT_PORT).SendMessage(
												kvp.getKey(), 
												EnumReqResp.WhoIsPingTopic.toString(), 
												true);
									
									whoIsWrapper.PingWhoIs(
											responseServerTask.Socket,
											responseServerTask.SocketLock,
											responseServerTask.WhoIs);
								}
							}
						}
					}
				}
			} catch (Exception ex) {
				Logger.log(ex);
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				Logger.log(e);
			}
		}
	}

	public static void DoConnect(String strServerName, int intPort,
			ZmqReqRespServerAck zmqReqRespServerAck) {
		Ctx context = ZMQ.zmq_init(1);
		// using (var context = new Context())
		try {
			@SuppressWarnings("deprecation")
			SocketBase socket = ZMQ.zmq_socket(context, ZMQ.ZMQ_XREP);
			// using (SocketBase socket = context.SocketBase(SocketType.XREP))
			try {
				String strIp = NetworkHelper.GetIpAddr(strServerName);
				String strAddr = "tcp://" + strIp + ":" + intPort;
				// socket.HWM = CoreConstants.HWM;
				ZMQ.zmq_setsockopt(socket, ZMQ.ZMQ_RCVHWM,
						CommunicationConstants.HWM);
				socket.bind(strAddr);
				synchronized (m_connectionsLoadedLock) {
					m_intConnectionsLoaded++;
				}
				String strMessage = ZmqReqRespServer.class.getName()
						+ " loaded addr: " + strAddr;
				Console.writeLine(strMessage);
				Logger.log(strMessage);
				Object socketLock = new Object();

				while (true) {
					try {
						if (!OnRecv(socket, socketLock, zmqReqRespServerAck)) {
							//
							// slow down
							//
							Thread.sleep(30000);
						}
						Thread.sleep(10);
					} catch (Exception ex) {
						Logger.log(ex);
						//
						// slow down
						//
						Thread.sleep(30000);
					}
				}
			} finally {
				ZMQ.zmq_close(socket);
			}
		} catch (Exception ex) {
			Logger.log(ex);
		} finally {
			ZMQ.zmq_term(context);
		}
	}

	private static boolean OnRecv(SocketBase socket, Object socketLock,
			ZmqReqRespServerAck zmqReqRespServerAck) {
		try {
			//
			// get request
			//
			byte[] bytesWhoIs = null;
			byte[] bytes = null;
			synchronized (socketLock) {
				Msg msg = socket.recv(0, 10);
				if(msg != null){
					bytesWhoIs = msg.data();
					boolean blnRcvMore = ZMQ
							.zmq_getsockopt(socket, ZMQ.ZMQ_RCVMORE) == 1;
					if (bytesWhoIs != null && blnRcvMore) {
						//
						// recieve multipart message here
						//
						bytes = RecvMultiPart(socket, 60 * 1000);
					}
				}
			}
			if (bytesWhoIs != null && bytes != null && bytes.length > 0) {
				if (bytes.length == 1 && bytes[0] == 1) {
					//
					// this is a client ping, just do a ping reponse
					//
					WhoIsWrapper.SendPingMsg(socket, bytesWhoIs, socketLock);
				} else {
					String strMessage = "ZMQReqRespServer is processing request [" +
							DateTime.now() + "]...";
					Logger.log(strMessage);
					Console.writeLine(strMessage);
					ProcessRequest(socket, socketLock, bytes, bytesWhoIs,
							zmqReqRespServerAck);
				}
			}
		} catch (Exception ex) {
			Logger.log(ex);
			return false;
		}
		return true;
	}

	private static byte[] RecvMultiPart(SocketBase socket, int intTimeOut) {
		byte[] bytes = socket.recv(0, intTimeOut).data();
		boolean blnRcvMore = ZMQ.zmq_getsockopt(socket, ZMQ.ZMQ_RCVMORE) == 1;

		boolean blnIsMultiMsg = bytes != null && blnRcvMore;
		List<Byte> byteArrList = null;
		boolean blnIsValidMessage = true;
		boolean blnFoundFirstMessage = false;
		if (blnIsMultiMsg) {
			byteArrList = new ArrayList<Byte>();
			ISerializerReader serializerReader = Serializer.GetReader(bytes);
			blnFoundFirstMessage = serializerReader.ReadBoolean();
			if (blnFoundFirstMessage) {
				byte[] currByteArr = serializerReader.ReadByteArray();
				for (int i = 0; i < currByteArr.length; i++) {
					byteArrList.add(currByteArr[i]);
				}
			}
		}

		boolean blnFoundLastMesssage = false;
		blnRcvMore = ZMQ.zmq_getsockopt(socket, ZMQ.ZMQ_RCVMORE) == 1;
		while (bytes != null && blnRcvMore && byteArrList != null) // keep
																	// receiving
																	// even if
																	// it is not
																	// a valid
																	// message
		{
			byte[] currBytes = socket.recv(0, 5 * intTimeOut).data();
			if (currBytes != null) {
				ISerializerReader serializerReader = Serializer
						.GetReader(currBytes);
				serializerReader.ReadBoolean(); // dummy first message flag
				if (blnFoundFirstMessage) {
					currBytes = serializerReader.ReadByteArray();
					for (int i = 0; i < currBytes.length; i++) {
						byteArrList.add(currBytes[i]);
					}
				}
				blnFoundLastMesssage = serializerReader.ReadBoolean();
				if (blnFoundLastMesssage) {
					break;
				}
			}
		}

		if (blnIsMultiMsg && (!blnFoundFirstMessage || !blnFoundLastMesssage)) {
			blnIsValidMessage = false;
		}

		if (bytes != null && byteArrList != null && byteArrList.size() > 0
				&& blnIsValidMessage) {
			bytes = new byte[byteArrList.size()];
			for (int i = 0; i < byteArrList.size(); i++) {
				bytes[i] = byteArrList.get(i);
			}
		}

		if (bytes != null && blnIsValidMessage) {
			if (blnIsMultiMsg) {
				double dblMb = Precision.round(((bytes.length / 1024f) / 1024f), 2);
				String strMessage = "Debug => "
						+ ZmqReqRespServer.class.getName()
						+ " received very large message [" + dblMb + "] mb";
				Console.writeLine(strMessage);
				Logger.log(strMessage);
			}
			return bytes;
		}
		return null;
	}

	private static void ProcessRequest(SocketBase socket, Object socketLock,
			byte[] bytes, byte[] bytesWhoIs,
			ZmqReqRespServerAck zmqReqRespServerAck) {
		try {
			while (!m_blnIsConnected || !IsInitialized) {
				Thread.sleep(1000);
				String strMessage = ZmqReqRespServer.class.getName()
						+ " is waiting to be connected...";
				Console.writeLine(strMessage);
				Logger.log(strMessage);
			}

			ISerializerReader serializer = Serializer.GetReader(bytes);
			String strRequestId = serializer.ReadString();
			synchronized (m_RequestResponseLock) {
				ResponseServerTask responseServerTask;
				if (MapRequestToTask.containsKey(strRequestId)) // &&
				{
					responseServerTask = MapRequestToTask.get(strRequestId);
					responseServerTask.WhoIs = bytesWhoIs;
					ReqRespServerHeartBeat
							.OnTopicCallback(responseServerTask.Request.RequestorName);
					String strMessage = "RequestResponse task [" + strRequestId
							+ "] has been replaced";
					Console.writeLine(strMessage);
					Logger.log(strMessage);
				} else {
					byte[] byteArr = serializer.ReadByteArray();

					if (byteArr == null || byteArr.length == 0) {
						throw new HCException("Invalid array size. Request ["
								+ strRequestId + "]");
					}

					//
					// this is a new request
					//
					RequestDataMessage request = new RequestDataMessage();
					Serializer.deserialize(byteArr, request);
					request.Error = ""; // reset error, in case of any
					ReqRespServerHeartBeat
							.OnTopicCallback(request.RequestorName);
					responseServerTask = new ResponseServerTask(strRequestId,
							request, socket, socketLock, bytesWhoIs,
							zmqReqRespServerAck);
					MapRequestToTask.put(strRequestId, responseServerTask);
					responseServerTask.Run();
				}
			}
		} catch (Exception ex) {
			Logger.log(ex);
		}
	}

	public static void SendResponse(SocketBase socket, String strRequestId,
			RequestDataMessage response, Object socketLock, byte[] bytesWhoIs) {
		if (response == null) {
			return;
		}
		Console.writeLine("ZmqReqRespSever is trying to send result [" + DateTime.now() + "]");
		synchronized (socketLock) {
			socket.send(new Msg(bytesWhoIs), ZMQ.ZMQ_SNDMORE);

			if (!StringHelper.IsNullOrEmpty(response.Error)) {
				SendSingleMessage(socket, response, strRequestId);
			} else if (response.IsAsync) {
				//
				// send response in multiple parts
				//
				List<RequestDataMessage> responseList = ReqRespHelper
						.GetListOfResponses(response);
				for (int i = 0; i < responseList.size() - 1; i++) {
					RequestDataMessage dataMessage = responseList.get(i);
					ISerializerWriter writer = Serializer.GetWriter();
					writer.Write(strRequestId);
					writer.Write(dataMessage.getByteArr());
					byte[] bytes = writer.GetBytes();
					SendBytes(bytes, socket, true);
				}
				if (responseList.size() > 0) {
					SendSingleMessage(socket,
							responseList.get(responseList.size() - 1),
							strRequestId);
				} else {
					SendSingleMessage(socket, response, strRequestId);
				}
			} else {
				SendSingleMessage(socket, response, strRequestId);
			}
		}
		Console.writeLine("Sent result " + DateTime.now());
	}

	private static void SendSingleMessage(SocketBase socket,
			RequestDataMessage response, String strRequestId) {
		try {
			ISerializerWriter writer = Serializer.GetWriter();
			writer.Write(strRequestId);
			writer.Write(response.getByteArr());
			byte[] bytes = writer.GetBytes();
			SendBytes(bytes, socket, false);
		} catch (Exception ex) {
			Logger.log(ex);
		}
	}

	private static void SendBytes(byte[] bytes, SocketBase socket,
			boolean blnMultipleMessages) {
		if (bytes.length <= Serializer.BYTES_LIMIT) {
			ISerializerWriter serializerWriter = Serializer.GetWriter();
			serializerWriter.Write(true); // check if it is the first message
			serializerWriter.Write(bytes);
			serializerWriter.Write(true); // check if it is the last message
			serializerWriter.Write(blnMultipleMessages);
			SendBytes(serializerWriter.GetBytes(), blnMultipleMessages, socket);
		} else {
			List<byte[]> byteList = Serializer.GetByteArrList(bytes);
			int intByteListSize = byteList.size();
			for (int i = 0; i < intByteListSize; i++) {
				ISerializerWriter serializerWriter = Serializer.GetWriter();
				serializerWriter.Write(i == 0); // check if it is the first
												// message
				serializerWriter.Write(byteList.get(i));
				serializerWriter.Write(i == intByteListSize - 1); // check if it
																	// is the
																	// last
																	// message
				serializerWriter.Write(blnMultipleMessages);
				SendBytes(serializerWriter.GetBytes(), blnMultipleMessages
						|| i < intByteListSize - 1, socket);
			}
			double dblMb = Precision.round(((bytes.length / 1024f) / 1024f), 2);
			String strMessage = "Debug => " + ZmqReqRespServer.class.getName()
					+ " sent very large message [" + byteList.size() + "]. ["
					+ dblMb + "] mb";
			Console.writeLine(strMessage);
			Logger.log(strMessage);
		}
	}

	private static void SendBytes(byte[] bytes, boolean blnSendMore,
			SocketBase socket) {
		boolean blnSuccess = false;
		int intCounter = 0;
		while (!blnSuccess) {
			boolean status = false;

			try {
				status = SendBytesUnlocked(bytes, blnSendMore, socket);
				blnSuccess = status;
				if (!blnSuccess) {
					String strMessage = ZmqReqRespServer.class.getName()
							+ " could not send message [" + status + "]["
							+ intCounter + "]. Resending...";
					Logger.log(strMessage);
					Console.writeLine(strMessage);
					Thread.sleep(5000);
				}
			} catch (Exception ex) {
				String strMessage = ZmqReqRespServer.class.getName()
						+ " could not send message [" + status + "]["
						+ intCounter + "]. Resending...";
				Logger.log(strMessage);
				Console.writeLine(strMessage);
				Logger.log(ex);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					Logger.log(e);
				}
			}
			intCounter++;
		}
	}

	private static boolean SendBytesUnlocked(
        byte[] bytes,
        boolean blnSendMore,
        SocketBase socket)
    {
		ZMQ.zmq_setsockopt(socket, ZMQ.ZMQ_RCVHWM,
				CommunicationConstants.HWM);
		
        boolean status;
        if (blnSendMore)
        {
        	Msg msg = new Msg(bytes);
        	msg.set_flags(Msg.more);
            status = socket.send(
            		msg, 
            		ZMQ.ZMQ_SNDMORE);
        }
        else
        {
            status = socket.send(new Msg(bytes), ZMQ.ZMQ_DONTWAIT);
        }
        return status;
    }

}
