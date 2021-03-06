package GUI;

import java.io.IOException;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import javax.swing.JFrame;

import GUI2.Utils;
import client.Client;
 
public class LoginWindow extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Client _client;
	private Text _actiontarget;
	private final JFXPanel _panel;
	private boolean _newAcct;
    
    public LoginWindow(Client client){
    	super("Cooking with Friends -- Login");
    	Platform.setImplicitExit(false);
    	_client = client;
    	_newAcct = false;
    	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	
    	_panel = new JFXPanel();
    	this.add(_panel);
    	this.setSize(550,400);
    	this.setVisible(true);
    	_panel.setPreferredSize(new java.awt.Dimension(550,400));
    	
    	Platform.runLater(new Runnable() {
    		@Override
    		public void run() {
    			loadLogin();
    		}
    	});
    }

    public void loadLogin() {
    	_panel.setScene(login());
    }
    
    public void loadAccount(){
    	_panel.setScene(newAccount());
    }
    
    /**
     * Generates a login page scene.
     * @return
     */
    private Scene login() {
    	_newAcct = false;
        GridPane grid = new GridPane();
        grid.setStyle(Style.BACKGROUND);
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
         
        _actiontarget = new Text();
        grid.add(_actiontarget, 1, 6);
        
        Scene scene = new Scene(grid, 300, 275);
        
        Text scenetitle = new Text("Welcome to Cooking with Friends");
        scenetitle.setStyle(Style.LOGIN_HEADER);
        grid.add(scenetitle, 0, 0, 2, 1);

        Text userName = new Text("E-mail:");
        userName.setStyle(Style.SECTION_HEADER);
        grid.add(userName, 0, 1);

        final TextField userTextField = new TextField();
        grid.add(userTextField, 1, 1);

       	Text pw = new Text("Password:");
        pw.setStyle(Style.SECTION_HEADER);
        grid.add(pw, 0, 2);

        final PasswordField pwBox = new PasswordField();
        grid.add(pwBox, 1, 2);
        
        
        Button btn = new Button("Sign in");
        buttonStyle(btn);
        Button newAcct = new Button("Create Account");
        buttonStyle(newAcct);
        Button passBtn = new Button("Forgot Password");
        buttonStyle(passBtn);
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(newAcct);
        hbBtn.getChildren().add(btn);
        hbBtn.getChildren().add(passBtn);
        grid.add(hbBtn, 1, 4);
        passBtn.setOnAction(new EventHandler<ActionEvent>() {
        	
        	@Override
        	public void handle(ActionEvent e){
        		String email = userTextField.getText().trim();
        		if(email.length()>Utils.MAX_FIELD_LEN){
        			_actiontarget.setText("Your email must not exceed " + Utils.MAX_FIELD_LEN + " characters long.");
        		}
        		if(email != null || email.trim().length()==0){
        			if(Utils.isValidEmailStructure(email)){
        				//String pass = String.valueOf(Utils.generateRandomPassword());
        				_client.updatePassword(email);
            			//String message = "Your new password is: " + pass;
            			//_client.changePassword(email, pass);
                		//Sender.send(email, message);
        			}
        			else{
        				_actiontarget.setFill(Color.WHITE);
                		_actiontarget.setText("You must enter a valid email.");
        			}
        			
        		}
        		else {
        			_actiontarget.setFill(Color.WHITE);
            		_actiontarget.setText("You must enter a username.");
        		}
        		//_actiontarget.setFill(Color.WHITE);
        		//_actiontarget.setText("We have sent you an email with a new password.");
        	}
        });

        btn.setOnAction(new EventHandler<ActionEvent>() {
        	 
            @Override
            public void handle(ActionEvent e) {
                _actiontarget.setFill(Color.WHITE);
                if(userTextField.getText().length()>0)
                	_actiontarget.setText("Your email must not exceed " + Utils.MAX_FIELD_LEN + " characters long.");
                if(pwBox.getText().length()>Utils.MAX_FIELD_LEN)
                	_actiontarget.setText("Your password must not exceed " + Utils.MAX_FIELD_LEN + " characters long.");
                
                if(userTextField.getText().length()==0 || pwBox.getText().length()==0){
            		_actiontarget.setText("You must input a username and password");
            	}
            	else{
            		try {
            			//If valid input, send to server to get account.
            			_actiontarget.setText("");
    					_client.checkPassword(userTextField.getText(), pwBox.getText());
    				} catch (IOException e1) {
    					e1.printStackTrace();
    				} catch (Exception e1) {
						e1.printStackTrace();
					}
            	}
            }
        });
        
        newAcct.setOnAction(new EventHandler<ActionEvent>() {
        	
        	@Override
        	public void handle(ActionEvent e){
        		loadAccount();
        	}
        });
        return scene;
    }
    
    

    private Scene newAccount(){
    	_newAcct = true;
    	GridPane grid = new GridPane();
    	grid.setStyle(Style.BACKGROUND);
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
         
        _actiontarget = new Text();
        grid.add(_actiontarget, 0, 6, 2, 1);
        
        Scene scene = new Scene(grid, 300, 275);
        
        Text scenetitle = new Text("Create your Account!");
        scenetitle.setStyle(Style.LOGIN_HEADER);
        grid.add(scenetitle, 0, 0, 2, 1);

        Text userName = new Text("Email:");
        userName.setStyle(Style.SECTION_HEADER);
        grid.add(userName, 0, 1);

        final TextField userTextField = new TextField();
        grid.add(userTextField, 1, 1);

        Text pw = new Text("Password:");
        pw.setStyle(Style.SECTION_HEADER);
        grid.add(pw, 0, 2);

        final PasswordField pwBox = new PasswordField();
        grid.add(pwBox, 1, 2);
        
        Text pw2 = new Text("Confirm Password:");
        pw2.setStyle(Style.SECTION_HEADER);
        grid.add(pw2, 0, 3);
        
        final PasswordField pwBox2 = new PasswordField();
        grid.add(pwBox2, 1, 3);
        
        Button cancel = new Button("Cancel");
        buttonStyle(cancel);
        //cancel.setStyle(Style.BUTTON);
        Button newAcct = new Button("Create Account");
        buttonStyle(newAcct);
        //newAcct.setStyle(Style.BUTTON);
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(cancel);
        hbBtn.getChildren().add(newAcct);
        grid.add(hbBtn, 1, 4);
        
        cancel.setOnAction(new EventHandler<ActionEvent>() {
        	
        	@Override
        	public void handle(ActionEvent e){
                loadLogin();
        	}
        });
        
        newAcct.setOnAction(new EventHandler<ActionEvent>() {
        	 
            @Override
            public void handle(ActionEvent e) {
                _actiontarget.setFill(Color.WHITE);
                if(userTextField.getText().length()>Utils.MAX_FIELD_LEN){
                	_actiontarget.setText("Your email must not exceed " + Utils.MAX_FIELD_LEN + " characters long.");
                }
                if(pwBox.getText().length()>Utils.MAX_FIELD_LEN){
                	_actiontarget.setText("Your password must not exceed " + Utils.MAX_FIELD_LEN + " characters long.");
                }
                if(userTextField.getText().length()==0 || pwBox.getText().length()==0){
            		_actiontarget.setText("You must input a username and password");
            	} 
                else if(!pwBox.getText().equals(pwBox2.getText())){
            		_actiontarget.setText("Passwords don't match!");
            	}
                else if(!Utils.isValidPassword(pwBox.getText())){
                	_actiontarget.setText("Your password must be between 6 and " + Utils.MAX_FIELD_LEN + " characters long.");
                }
            	else{
            		try {
            			_actiontarget.setText("");
            			if(Utils.isValidEmailStructure(userTextField.getText())){
            				System.out.println("CHECKING PASSWORD");
            				_client.checkPassword(userTextField.getText(), pwBox.getText());
            			}
            			else
            				_actiontarget.setText("You must enter a valid email address.");
    					
    				} catch (IOException e1) {
    					e1.printStackTrace();
    				} catch (Exception e1) {
						e1.printStackTrace();
					}
            	}
            }
        });
        
        return scene;
    }
    
    public void displayIncorrect(String error){
    		_actiontarget.setText(error);
    		_actiontarget.setFill(Color.WHITE);
    }
    
    public boolean isNewAccount(){
    	return _newAcct;
    }
    

    /**
     * Taken from:http://stackoverflow.com/questions/13074459/javafx-2-and-css-pseudo-classes-setting-hover-attributes-in-setstyle-method
     * @param node
     */
    private void buttonStyle(Node node) {
        node.styleProperty().bind(
          Bindings
            .when(node.hoverProperty())
              .then(
                new SimpleStringProperty(Style.HOVER)
              )
              .otherwise(
                new SimpleStringProperty(Style.BUTTON)
              )
        );
      }
}
