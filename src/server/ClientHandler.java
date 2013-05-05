package server;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;

import API.Wrapper;
import ClientServerRequests.AccountRequest;
import ClientServerRequests.InvitationRequest;
import ClientServerRequests.KitchenRequest;
import ClientServerRequests.NewAccountRequest;
import ClientServerRequests.NewKitchenRequest;
import ClientServerRequests.Request;
import ClientServerRequests.RequestReturn;
import ClientServerRequests.StoreAccountRequest;
import ClientServerRequests.StoreKitchenRequest;
import ClientServerRequests.UpdateKitchenRequest;
import Database.DBHelper;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

/**
 * Encapsulate IO for the given client.
 * Runs an input loop and adds task to taskPool
 * based on the request. 
 * Sends RequestReturn object to handler.
 * Modified ClientHandler from lab4
 */
public class ClientHandler extends Thread {
	private ClientPool _pool;
	private Socket _client;
	private ObjectInputStream _objectIn;
	private ObjectOutputStream _objectOut;
	private ExecutorService _taskPool;
	private String _clientID;
	private DBHelper _helper;
	private KitchenPool _activeKitchens;
	private boolean _running;
	private AutocorrectEngines _autocorrect;
	
	/**
	 * Thread for the client. Handles input and launches requests.
	 */
	public ClientHandler(ClientPool pool, Socket client, ExecutorService taskPool, KitchenPool kitchens, DBHelper helper, AutocorrectEngines autocorrect) throws IOException {
		if (pool == null || client == null) {
			throw new IllegalArgumentException("Cannot accept null arguments.");
		}
		_autocorrect = autocorrect;
		_helper = helper;
		_pool = pool;
		_client = client;
		_taskPool = taskPool;
		_activeKitchens = kitchens;
		_objectIn = new ObjectInputStream(client.getInputStream());
		_objectOut = new ObjectOutputStream(client.getOutputStream()) {
			@Override
			public void close() {
				try {
					super.close();
				} catch (IOException e) {
					//TODO??
				}
			}
		};
	}
	
	/**
	 * Receive data from the client. Input is in the form 
	 * of a string. Launch request accordingly.
	 */
	public void run(){
		try {
				_running = true;
				Request request;
				int type;
				while(_running && _client.isConnected()) {
					if((request = (Request) _objectIn.readObject()) != null){
						type = request.getType();
						System.out.println("recieved request type: " + type);
						switch (type){
							case 1:  //verify account
								System.out.println("SHOULD CHECK PASSWORD");
								checkPassword(request);
								break;
							case 2:  //getKitchen
								getKitchen(request);
								break;
							//case 3 -- 10, and 17 are update kitchens (handled by default
							case 11: //store Account
								System.out.println("client handler recieved store acount request!");
								storeAccount(request);
								break;
							case 12: //close client
								kill();
								break;
							case 13: //create new account	
								createNewUser(request);
								break;
							case 14: //create new Kitchen
								createNewKitchen(request);	
								break;
							case 15: //invite to kitchen
								invite(request);
								break;
							case 16: //DECLINE INVITATION
								System.out.println("DECLINED INVITATION IMPLEMENT!!!");
								break;
							//CHANGE THE PASSWORD.
							case 18:
								changePassword(request);
								break;
							//IS VALID USER NAME.	
							case 19:
								System.out.println("CASE 19 USER IN DATABASE");
								userInDatabase(request);
								break;
							default:
								updateKitchen(request);
								break;
						}
					}

			}
		} catch (Exception e) {
				try {
					kill();
				} catch (IOException e1) {
					//try again
					try {
						kill();
					} catch (IOException e2) {
						//Ignore
					}
				}
		} 
	}


	/**
	 * Send a RequestReturn to the client via the socket
	 */
	public synchronized void send(RequestReturn toReturn) {
		System.out.println("SENDDDDDDDD");
		if(toReturn != null){
			System.out.println("SERVER SENDING TO CLIENT: sending request of type: " + toReturn.getType());
			
			try {
				_objectOut.writeObject(toReturn);
				_objectOut.flush();
				_objectOut.reset();
			} catch (IOException e) {
//				try {
//					kill();
//				} catch (IOException e1) {
//					e1.printStackTrace();
//				}
			}
		}
	}

	/**
	 * Close this socket and its related streams.
	 */
	public void kill() throws IOException {
		System.out.println("killing myself (client handler of clinet " + _clientID + ")" );
		_objectIn.close();
		try{
			_objectOut.close();
		} catch(SocketException e){
			
		}
		_activeKitchens.removeUser(_clientID);
		_pool.remove(this);
		_client.close();
	}	
	
	
	public void checkPassword(Request request){
		if(_helper.checkUsernamePassword(request.getUsername(), request.getPassword())){
			System.out.println("executing task");
			_taskPool.execute(new AccountRequest(this, request.getUsername(), _helper, _activeKitchens, _autocorrect));
		}
		else{
			RequestReturn toReturn = new RequestReturn(1);
			toReturn.setCorrect(false);
			send(toReturn);
		}
	}

	private void changePassword(Request request){
		System.out.println("CHANGE PASSWORD IN CLIENT HANDLER");
		_helper.changePassword(request.getUsername(), request.getPassword());
	}
	
	public void getKitchen(Request request){
		_taskPool.execute(new KitchenRequest(this, request.getKitchenID(), _activeKitchens));
	}
	
	public void updateKitchen(Request request){
		System.out.println("upadting kitchen!");
		_taskPool.execute(new UpdateKitchenRequest(_activeKitchens, request));
	}
	
	public void createNewUser(Request request){
		_taskPool.execute(new NewAccountRequest(this, request, _helper));
	}
	
	private void createNewKitchen(Request request) {
		_taskPool.execute(new NewKitchenRequest(this, request, _helper, _activeKitchens, request.getAccount()));
	}

	private void storeAccount(Request request) {
		_taskPool.execute(new StoreAccountRequest(request.getAccount(), _helper, _activeKitchens, request));
	}
	
	private void storeKitchen(Request request) {
		_taskPool.execute(new StoreKitchenRequest(request.getKitchen(), _helper));
	}
	
	private void invite(Request request) {
		System.out.println("OOO an invite!");
		_taskPool.execute(new InvitationRequest(this, _pool, _helper, request.getInvitation(), _activeKitchens));
		
		// TODO Auto-generated method stub
		
	}
	
	public void userInDatabase(Request request){
		
		boolean inDB = _helper.inDatabase(request.getUsername());
		System.out.println("USER IN DATABASE CLIENT HANDLER: " + inDB);
		//If it's a unique user ie if it's not already in the data base.
		RequestReturn req = new RequestReturn(4);
		req.setUserInDatabase(inDB);
		send(req);
	}
	
	
    private static String toString( Serializable o ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject(o);
        oos.close();
        return new String( Base64.encode( baos.toByteArray() ) );
    }
	
	public String getID(){
		return _clientID;
	}
	
	public void setID(String id){
		_clientID = id;
		_pool.addID(id, this);
	}
	
}