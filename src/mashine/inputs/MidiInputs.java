/**
 *  Handle Midi inputs
 *
 *	@author procsynth - Antoine Pintout
 *	@since  13-02-2016`
 */

package mashine.inputs;

import java.util.HashMap;

import javax.sound.midi.MidiMessage;

import mashine.inputs.midi_devices.*;
import mashine.MaShine;
import mashine.Do;
import themidibus.MidiBus;

public class MidiInputs extends InputSource implements Learnable{

	private HashMap<String,MidiBus> buses;
	private MidiDevice[] devicesTypes = {new KorgNanoKontrol2(), new BehringerDC1(), new GenericMidiDevice()};
	
	private String lastState;
	private String lastRange;
	private boolean init = false;

	public MidiInputs () {
		super();
		buses = new HashMap<String,MidiBus>();
		rescanDevices();
	}

	public void midiMessage(MidiMessage message, long timestamp, String busName){

		int command =   (int)(message.getMessage()[0] & 0xF0);
		//int channel =   (int)(message.getMessage()[0] & 0x0F);
		int keyNumber = (int)(message.getMessage()[1] & 0xFF);
		int value =     (int)(message.getMessage()[2] & 0xFF);

		for(int i = 0; i < devicesTypes.length; i++){
			if(busName.contains(devicesTypes[i].getDeviceName())){
				String name = "midi."+ busName +"."+ devicesTypes[i].getInputName(command, keyNumber, value);
				Boolean state = devicesTypes[i].getState(command, keyNumber, value);
				Double range = devicesTypes[i].getRange(command, keyNumber, value);

				if(state != null){
					states.put(name + (state ? ".on" : ".off"), true);
					lastState = name + (state ? ".on" : ".off");
				}
				if(range != null){
					ranges.put(name, range/127);
					lastRange = name;
				}
				break;
			}
		}
	}

	public void tick(){
		if(!init){
			init = true;
			MaShine.inputs.registerAction("mashine.inputs.reload_midi", new Do(){public void x(){rescanDevices();}});
		}
	}

	public void clear(){
		for(String s : states.keySet()){
			states.put(s, false);
		}
		lastRange = null;
		lastState = null;
	}

	private void rescanDevices(){
		buses.clear();
		MidiBus.findMidiDevices();
		String[] inputNames = MidiBus.availableInputs();
		String[] outputNames = MidiBus.availableOutputs();
		for(int i = 0; i < inputNames.length; i++){
			String name = inputNames[i];
			int hwIndex = name.indexOf("hw:");
			if(hwIndex != -1){
				String hwAddress = name.substring(hwIndex, hwIndex+4);
				for(int o = 0; o < outputNames.length; o++){
					if(outputNames[o].indexOf(hwAddress) != -1){
						name = name.substring(0, name.indexOf(" "));
						buses.put(name, new MidiBus(this, inputNames[i], outputNames[o], name));
					}
				}
			}
		}
	}

	public String getLastRange(){

		return lastRange;
	}
	public String getLastState(){
		return lastState;
	}

}