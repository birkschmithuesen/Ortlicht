import processing.core.PApplet;
import processing.core.PVector;

public class ManualSphere implements runnableLedEffect {
	PApplet papplet;
	String name = "Sphere";
	String id;
	PVector[] ledPositions;
	LedColor[] bufferLedColors;
	float outerRadius, innerRadius, thickness, expansion;
	LedColor theColor;
	RemoteControlledColorParameter remoteColor;
	RemoteControlledFloatParameter remoteBlendOut;
	RemoteControlledFloatParameter remoteExpansion;
        RemoteControlledFloatParameter remoteThickness;
	PVector center = new PVector(0, 0, 0);
	LedColor.LedAlphaMode blendMode = LedColor.LedAlphaMode.NORMAL;

	ManualSphere(String _id, PVector[] _ledPositions, float _thickness) {
		id = _id;
		ledPositions = _ledPositions;
		bufferLedColors = LedColor.createColorArray(ledPositions.length);
		thickness = _thickness;
		
		remoteColor = new RemoteControlledColorParameter("/Spheres/"+name+"_"+id+"/color/", 0f, 0.f, 1f);
		remoteBlendOut = new RemoteControlledFloatParameter("/Spheres/"+name+"_"+id + "/blendOut", 1.f, 0.f, 1.f);
		remoteExpansion = new RemoteControlledFloatParameter("/Spheres/"+name+"_"+id + "/expansion", 0.0f, 0.f, 1.f);
                remoteThickness = new RemoteControlledFloatParameter("/Spheres/"+name+"_"+id + "/thickness", 0.2f, 0.f, 1.f);
	}

	public LedColor[] drawMe() {
		expansion=remoteExpansion.getValue();
                thickness=remoteThickness.getValue();
		outerRadius = papplet.map(expansion, 0.f, 1.f, 0f, Ortlicht.sculptureRadius+0.2f);
		innerRadius = outerRadius - thickness;
		LedSphereDrawer.drawSphere(ledPositions, bufferLedColors, center, outerRadius, innerRadius,
				remoteColor.getColor(), blendMode, remoteBlendOut.getValue());
		return bufferLedColors;
	}

	public String getName() {
		return name;
	}

}