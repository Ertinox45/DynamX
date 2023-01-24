package fr.dynamx.common.contentpack.parts;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import fr.aym.acslib.api.services.error.ErrorLevel;
import fr.dynamx.api.contentpack.object.subinfo.ISubInfoType;
import fr.dynamx.api.contentpack.registry.*;
import fr.dynamx.api.entities.modules.ModuleListBuilder;
import fr.dynamx.common.contentpack.type.vehicle.ModularVehicleInfo;
import fr.dynamx.common.entities.BaseVehicleEntity;
import fr.dynamx.common.entities.modules.VehicleLightsModule;
import fr.dynamx.utils.errors.DynamXErrorManager;
import lombok.Getter;

import javax.annotation.Nullable;

/**
 * Shortand to create a {@link PartLightSource} with only one {@link LightObject}
 */
@RegisteredSubInfoType(name = "light", registries = {SubInfoTypeRegistries.WHEELED_VEHICLES, SubInfoTypeRegistries.HELICOPTER}, strictName = false)
public class SimplePartLightSource extends LightObject implements ISubInfoType<ModularVehicleInfo> {
    @IPackFilePropertyFixer.PackFilePropertyFixer(registries = SubInfoTypeRegistries.WHEELED_VEHICLES)
    public static final IPackFilePropertyFixer PROPERTY_FIXER = (object, key, value) -> {
        if ("ShapePosition".equals(key))
            return new IPackFilePropertyFixer.FixResult("Position", true);
        return null;
    };

    private final ModularVehicleInfo owner;
    private final String name;

    @Getter
    @PackFileProperty(configNames = "PartName")
    protected String partName;
    @Getter
    @PackFileProperty(configNames = "BaseMaterial", required = false)
    protected String baseMaterial = "default";
    @Getter
    @PackFileProperty(configNames = "Position", type = DefinitionType.DynamXDefinitionTypes.VECTOR3F_INVERSED_Y, description = "common.position", required = false)
    protected Vector3f position;
    @Getter
    @PackFileProperty(configNames = "Rotation", required = false, defaultValue = "1 0 0 0")
    protected Quaternion rotation = new Quaternion();

    public SimplePartLightSource(ModularVehicleInfo owner, String name) {
        this.owner = owner;
        this.name = name;
    }

    @Override
    public void appendTo(ModularVehicleInfo owner) {
        hashLightId();
        PartLightSource existing = owner.getLightSource(partName);
        if(existing != null)
            DynamXErrorManager.addPackError(getPackName(), "deprecated_light_format", ErrorLevel.LOW, owner.getFullName(), "Light named " + name);
        boolean add = false;
        if (existing == null) {
            existing = new PartLightSource(owner, this.name);
            existing.partName = partName;
            existing.baseMaterial = baseMaterial;
            existing.position = position;
            existing.rotation = rotation;
            add = true;
        }
        existing.addLightSource(this);
        if (add)
            existing.appendTo(owner);
    }

    @Nullable
    @Override
    public ModularVehicleInfo getOwner() {
        return owner;
    }

    @Override
    public void addModules(BaseVehicleEntity<?> entity, ModuleListBuilder modules) {
        if (!modules.hasModuleOfClass(VehicleLightsModule.class)) {
            modules.add(new VehicleLightsModule(entity));
        }
    }

    @Override
    public String getName() {
        return "LightSource_" + name;
    }

    @Override
    public String getPackName() {
        return owner.getPackName();
    }
}