package io.anuke.ucore.scene.ui;

public class TextDialog extends Dialog{
	
	public TextDialog(String title, String... text) {
		super(title);
		addCloseButton();
		
		set(title, text);
	}
	
	public void set(String title, String... text){
		content().clearChildren();
		getTitleLabel().setText(title);
		
		for(String s : text){
			Label label = new Label(s);
			label.setWrap(true);
			content().add(label);
			content().row();
		}
	}
	
	public TextDialog padText(float amount){
		content().pad(amount);
		return this;
	}
}
