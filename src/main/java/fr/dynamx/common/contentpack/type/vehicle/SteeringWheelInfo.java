package fr.dynamx.common.contentpack.type.vehicle;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import fr.dynamx.api.contentpack.object.subinfo.ISubInfoTypeOwner;
import fr.dynamx.api.contentpack.object.subinfo.SubInfoType;
import fr.dynamx.api.contentpack.registry.*;
import fr.dynamx.common.contentpack.loader.ModularVehicleInfoBuilder;

/**
 * Info of the steering wheel of a {@link ModularVehicleInfoBuilder}
 */
@RegisteredSubInfoType(name = "steeringwheel", registries = SubInfoTypeRegistries.WHEELED_VEHICLES)
public class SteeringWheelInfo extends SubInfoType<ModularVehicleInfoBuilder> {
    @IPackFilePropertyFixer.PackFilePropertyFixer(registries = SubInfoTypeRegistries.WHEELED_VEHICLES)
    public static final IPackFilePropertyFixer PROPERTY_FIXER = (object, key, value) -> {
        if ("BaseRotation".equals(key))
            return new IPackFilePropertyFixer.FixResult("BaseRotationQuat", true) {
                @Override
                public String newValue(String oldValue) {
                    String[] t = oldValue.split(" ");
                    return "0 0 0 0";//todo fix t[1] + " " + t[2] + " " + t[3] + " " + (Float.parseFloat(t[0]) * 180f / Math.PI); //Convert to degrees
                }
            };
        return null;
    };

    @PackFileProperty(configNames = "PartName", required = false, defaultValue = "SteeringWheel")
    private final String partName = "SteeringWheel";
    @PackFileProperty(configNames = "BaseRotationQuat", required = false, defaultValue = "0 0 0 1")
    private final Quaternion steeringWheelBaseRotation = null;
    @PackFileProperty(configNames = "Position", type = DefinitionType.DynamXDefinitionTypes.VECTOR3F_INVERSED_Y)
    private final Vector3f position = new Vector3f(0.5f, 1.1f, 1);

    public SteeringWheelInfo(ISubInfoTypeOwner<ModularVehicleInfoBuilder> owner) {
        super(owner);
    }

    @Override
    public void appendTo(ModularVehicleInfoBuilder owner) {
        owner.addSubProperty(this);
        getSteeringWheelPosition().multLocal(owner.getScaleModifier());
        owner.addRenderedParts(getPartName());
    }

    public String getPartName() {
        return partName;
    }

    public Quaternion getSteeringWheelBaseRotation() {
        return steeringWheelBaseRotation;
    }

    public Vector3f getSteeringWheelPosition() {
        return position;
    }

    @Override
    public String getName() {
        return "SteeringWheel in " + getOwner().getName();
    }
}
