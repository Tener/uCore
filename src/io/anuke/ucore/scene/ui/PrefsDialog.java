package io.anuke.ucore.scene.ui;

import io.anuke.ucore.core.Settings;
import io.anuke.ucore.scene.ui.layout.Table;

public class PrefsDialog extends Dialog{

	public PrefsDialog(String title) {
		super(title);
	}
	
	void addSlider(String name, String title, int def, int min, int max,StringProcessor s){
		addSlider(name, title, def, min, max, 1, s);
	}
	
	void addSlider(String name, String title, int def, int min, int max, int step, StringProcessor s){
		Table table = getContentTable();
		Slider slider = new Slider(min, max, 1f, false);
		Settings.defaults(name, def);
		
		slider.setValue(Settings.getInt(name));
		
		Label label = new Label(name);
		slider.changed(()->{
			label.setText(name + ": " + s.get((int)slider.getValue()));
			Settings.putInt(name, (int)slider.getValue());
			Settings.save();
		});
		
		slider.change();
		table.add(label).minWidth(330);
		table.add(slider);
		table.addButton("Reset", ()->{
			slider.setValue(def);
			slider.change();
		});
		table.row();
	}
	
	void addCheck(String name, String title, boolean def){
		Table table = getContentTable();
		CheckBox box = new CheckBox(title);
		Settings.defaults(name, def);
		
		box.setChecked(Settings.getBool(name));
		
		box.changed(()->{
			Settings.putBool(name, box.isChecked);
			Settings.save();
		});
		
		table.add(box).minWidth(330);
		table.add();
		table.addButton("Reset", ()->{
			box.setChecked(def);
			box.change();
		});
		table.row();
	}
	
	interface StringProcessor{
		String get(int i);
	}

}