package GUI2;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import javax.swing.JFrame;

import server.AutocorrectEngines;
import API.Wrapper;
import API.YummlyAPIWrapper;
import GUI.GUIScene;
import UserInfo.Account;
import UserInfo.Kitchen;
import UserInfo.KitchenName;
import client.Client;

public class GUI2Frame extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private Client _client;
	private JFXPanel _panel;
	private Account _account;
	private GUIScene _kitchenScene, _homeScene, _searchScene, _recipeScene;
	private AutocorrectEngines _engines;
	private Map<KitchenName,Kitchen> _kitchens;
	private Wrapper _api;
	private Controller _controller;
	
	public GUI2Frame(Client client, Account account, final Map<KitchenName,Kitchen> kitchens, AutocorrectEngines engines) {
		super("Cooking with Friends!");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	System.out.println("CLOSEEEINGING WINDOWNNNNNNNNNN");
				_client.close();
		    }
		});
		
		this.setSize(1280, 960);
    	this.setVisible(true);
    	
    	_panel = new JFXPanel();
    	this.add(_panel);
    	this.setSize(1280, 960);
    	this.setVisible(true);
    	_panel.setPreferredSize(new java.awt.Dimension(1280,960));
    	
    	_client = client;
    	_account = account;   	
    	_engines = engines;
    	_kitchens = kitchens;
    	_api = new YummlyAPIWrapper();
    	
    	
    	Platform.runLater(new Runnable() {
    		@Override
    		public void run() {
    
		    	Pane page;
				try {
					URL location = getClass().getResource("CookingWithFriends.fxml");

					FXMLLoader fxmlLoader = new FXMLLoader();
					fxmlLoader.setLocation(location);
					fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());

					Parent p = (Parent) fxmlLoader.load(location.openStream());
					
			        _controller = (Controller) fxmlLoader.getController();

			        Scene scene = new Scene(p);
			        
			        _controller.setUp(_client, _account, _kitchens, _engines);
			        _panel.setScene(scene);
				} catch (IOException e) {
					System.out.println("ewrjhoewrjewr");
					e.printStackTrace();
				}
    	
    		}
		});
	}
	
	public void updateKitchen(){
		System.out.println("CALLED UPDATE KITCHEN");
		Platform.runLater(new Runnable() {
    		@Override
    		public void run() {
    			_controller.reDisplayKitchen();
    		}
		});
	}
	
	public void updateSearch(){
		System.out.println("CALLED UPDATE KITCHEN");
		Platform.runLater(new Runnable() {
    		@Override
    		public void run() {
    			_controller.populateSearchIngredients();
    		}
		});
	}
	
	public void updateKitchenDropDown(){
		Platform.runLater(new Runnable() {
    		@Override
    		public void run() {
    			_controller.populateKitchenSelector();
    		}
		});
	}
	
}
